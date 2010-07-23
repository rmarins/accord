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
package org.neociclo.odetteftp.test;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

/**
 * @author Rafael Marins
 * @version $Rev$ $Date$
 */
public class ServerOftpletTest {

    private static final String MAILBOX_FILE_NAME = "20090122123901-234$partnerone$partnertwo$thedocumentname.file";

    @Test
    public void testGetMailboxFileDateTime() {

        Calendar c = Calendar.getInstance();
        c.set(2009, Calendar.JANUARY, 22, 12, 39, 01);
        c.set(Calendar.MILLISECOND, 234);
        Date expected = c.getTime(); 

        Date actual = ServerOftplet.getMailboxFileDateTime(MAILBOX_FILE_NAME);

        assertEquals("Invalid date & time parsed from mailbox filename.", expected, actual);
    }

    @Test
    public void testGetMailboxFileOriginator() {

        String expected = "partnerone";
        String actual = ServerOftplet.getMailboxFileOriginator(MAILBOX_FILE_NAME);

        assertEquals("Invalid originator parsed from mailbox filename", expected, actual);
    }

    @Test
    public void testGetMailboxFileDestination() {

        String expected = "partnertwo";
        String actual = ServerOftplet.getMailboxFileDestination(MAILBOX_FILE_NAME);

        assertEquals("Invalid destination parsed from mailbox filename", expected, actual);
    }

    @Test
    public void testGetMailboxFileDatasetName() {

        String expected = "thedocumentname";
        String actual = ServerOftplet.getMailboxFileDatasetName(MAILBOX_FILE_NAME);

        assertEquals("Invalid DatasetName parsed from mailbox filename", expected, actual);
    }

}
