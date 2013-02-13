/**
 * Neociclo Accord, Open Source B2Bi Middleware
 * Copyright (C) 2005-2009 Neociclo, http://www.neociclo.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * $Id$
 */
package org.neociclo.odetteftp.util;

import static org.junit.Assert.*;
import static org.neociclo.odetteftp.util.OftpTestUtil.getResourceFile;
import static org.neociclo.odetteftp.util.SecurityUtil.*;

import java.io.File;
import java.io.FilenameFilter;
import java.security.KeyStore;
import java.security.PrivateKey;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class SecurityUtilTest {

    private static final String TEST_FILE_PATH = "data/BR0307108.REM";

    private static final String KS_PATH = "keystores/client-bogus.p12";

    private static final char[] KS_PWD = "neociclo".toCharArray();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.neociclo.odetteftp.util.SecurityUtil#openKeyStore(java.io.File, char[])}.
     */
    @Test
    public void testOpenKeyStore() throws Exception {

        File dir = OftpTestUtil.getResourceFile(KS_PATH).getParentFile();
        File[] certificates = dir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return (!name.startsWith("."));
            }
            
        });

        for (File f : certificates) {
            System.out.println(f);
            KeyStore ks;
            try {
                ks = openKeyStore(f, KS_PWD);
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
            assertNotNull("KeyStore not loaded: " + f.getAbsolutePath(), ks);
        }

    }

    @Test
    public void testGetPrivateKey() throws Exception {

        File path = OftpTestUtil.getResourceFile(KS_PATH);
        KeyStore ks = openKeyStore(path, KS_PWD);
        PrivateKey key = getPrivateKey(ks, KS_PWD);
        assertNotNull("No private-key were found.", key);
    }

    @Test
    public void testComputeFileHash() throws Exception {
        File data = getResourceFile(TEST_FILE_PATH);
        byte[] hash = computeFileHash(data, DEFAULT_OFTP_HASH_ALGORITHM);
        assertNotNull("Failed to compute file hash: " + DEFAULT_OFTP_HASH_ALGORITHM, hash);
        assertEquals(DEFAULT_OFTP_HASH_ALGORITHM + " : bad digest were produced.", 20, hash.length);
    }

}
