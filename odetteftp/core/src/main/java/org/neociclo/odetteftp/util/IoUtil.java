/**
 *  Odette FTP API Library - Neociclo ACCORD
 *  Copyright (c) 2005-2009 Neociclo, http://www.neociclo.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.neociclo.odetteftp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class IoUtil {

    /**
     * Default buffer size of 32Kb for I/O use in this library.
     */
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    private static final Logger LOGGER = LoggerFactory.getLogger(IoUtil.class);

    public static void buildDirIfNecessary(File path) {
        if (!path.exists())
            path.mkdirs();
    }

    public static String getFilenameExtension(String filename) {
        if (filename == null)
            return null;
        String ext = null;
        int pos = filename.lastIndexOf('.');
        if (pos != -1)
            ext = filename.substring(pos + 1);
        return ext;
    }

    public static void copy(File src, File dest) throws IOException {

        LOGGER.trace("copy() - source file: {}, exists: {}", src.getAbsolutePath(), src.exists());
        ReadableByteChannel in = Channels.newChannel(new FileInputStream(src));

        LOGGER.trace("copy() - dest file: {}, exists: {}", dest.getAbsolutePath(), dest.exists());
        WritableByteChannel out = Channels.newChannel(new FileOutputStream(dest));

        LOGGER.trace("copy() Copying...");
        copyChannel(in, out);

        LOGGER.trace("copy() Closing files...");
        in.close();
        out.close();

    }

    private static void copyChannel(ReadableByteChannel source, WritableByteChannel dest) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);

        while (source.read(buffer) != -1) {
            // prepare the buffer to be drained
            buffer.flip();

            // write to the channel; may block
            dest.write(buffer);

            // if partial transfer, shift remainder down
            // if buffer is empty, same as doing clear()
            buffer.compact();
        }

        // EOF will leave buffer in fill state
        buffer.flip();

        // make sure that the buffer is fully drained
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }

    }

    public static boolean delete(File f) {
    	boolean deleted = true;
        if (f != null && f.exists()) {
            LOGGER.trace("delete() Deleting file: {}", f);
            System.gc();
            System.runFinalization();
            deleted = f.delete();
        } else {
            LOGGER.trace("delete() File doesn't exist: {}", f);
        }
        return deleted;
    }

    public static void move(File src, File dest) throws IOException {
        copy(src, dest);
        delete(src);
    }

    public static FileOutputStream openOutputStream(File file) throws FileNotFoundException {
        FileOutputStream stream = new FileOutputStream(file);
        return stream;
    }

    public static FileInputStream openInputStream(File file) throws FileNotFoundException {
        FileInputStream stream = new FileInputStream(file);
        return stream;
    }

    public static void copyStream(InputStream src, OutputStream dest) throws IOException {

        ReadableByteChannel in = Channels.newChannel(src);
        WritableByteChannel out = Channels.newChannel(dest);

        copyChannel(in, out);

        in.close();
        out.close();
        
    }

    public static String getBaseFilename(String name) {

        if (name == null || "".equals(name))
            return name;
        
        int extPos = name.lastIndexOf('.');

        if (extPos > 0) {
            return name.substring(0, extPos);
        } else {
            return name;
        }

    }

    public static void deleteDirectory(File dir) {

        if (dir == null || !dir.exists()) {
            return;
        }

        File[] children = dir.listFiles();
        for (File f : children) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                delete(f);
            }
        }

        delete(dir);

    }

	public static boolean existsFile(File file) {
		return file.exists();
	}

}
