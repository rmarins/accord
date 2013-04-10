/**
 * The Accord Project, http://accordproject.org
 * Copyright (C) 2005-2013 Rafael Marins, http://rafaelmarins.com
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
 */
package org.neociclo.odetteftp.protocol;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.neociclo.odetteftp.protocol.v13.OdetteFtpVer13Handler;
import org.neociclo.odetteftp.protocol.v14.OdetteFtpVer14Handler;
import org.neociclo.odetteftp.protocol.v20.OdetteFtpVer20Handler;

/**
 * @author Rafael Marins
 */
public class TestProtocolParseDate {

    @Test
    public void testVer13() {
        OdetteFtpVer13Handler handler = new OdetteFtpVer13Handler();

        Date date = handler.parseDateTime("101010", "101010");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(calendar.get(Calendar.YEAR), 2010);
        assertEquals(calendar.get(Calendar.MONTH), Calendar.OCTOBER);
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 10);

        assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 10);
        assertEquals(calendar.get(Calendar.MINUTE), 10);
        assertEquals(calendar.get(Calendar.SECOND), 10);
    }

    @Test
    public void testVer14() {
        OdetteFtpVer14Handler handler = new OdetteFtpVer14Handler();

        Date date = handler.parseDateTime("19101010", "1010101010");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(calendar.get(Calendar.YEAR), 1910);
        assertEquals(calendar.get(Calendar.MONTH), Calendar.OCTOBER);
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 10);

        assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 10);
        assertEquals(calendar.get(Calendar.MINUTE), 10);
        assertEquals(calendar.get(Calendar.SECOND), 10);
        assertEquals(calendar.get(Calendar.MILLISECOND), 00);
    }

    @Test
    public void testVer20() {
        OdetteFtpVer20Handler handler = new OdetteFtpVer20Handler();

        Date date = handler.parseDateTime("19101010", "1010101010");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals(calendar.get(Calendar.YEAR), 1910);
        assertEquals(calendar.get(Calendar.MONTH), Calendar.OCTOBER);
        assertEquals(calendar.get(Calendar.DAY_OF_MONTH), 10);

        assertEquals(calendar.get(Calendar.HOUR_OF_DAY), 10);
        assertEquals(calendar.get(Calendar.MINUTE), 10);
        assertEquals(calendar.get(Calendar.SECOND), 10);
        assertEquals(calendar.get(Calendar.MILLISECOND), 00);
    }

}
