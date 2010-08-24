/**
 *  Neociclo Accord - Open Source B2B Integration Suite
 *  Copyright (C) 2005-2008 Neociclo, http://www.neociclo.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  $Id$
 */
package org.neociclo.accord.camel.odette.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.camel.Converter;
import org.neociclo.odetteftp.protocol.DefaultVirtualFile;
import org.neociclo.odetteftp.protocol.VirtualFile;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
@Converter
public class OdetteFtpConverter {

    private OdetteFtpConverter() { }

    @Converter
    public static InputStream toInputStream(VirtualFile vf) {
        DefaultVirtualFile vFile = (DefaultVirtualFile) vf;
        try {
            return new FileInputStream(vFile.getFile());
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Converter
    public static File toFile(VirtualFile vf) {
        DefaultVirtualFile vFile = (DefaultVirtualFile) vf;
        return vFile.getFile();
    }

}
