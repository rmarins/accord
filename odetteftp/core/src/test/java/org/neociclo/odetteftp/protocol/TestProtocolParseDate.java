package org.neociclo.odetteftp.protocol;

import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;
import org.neociclo.odetteftp.protocol.v13.OdetteFtpVer13Handler;
import org.neociclo.odetteftp.protocol.v14.OdetteFtpVer14Handler;
import org.neociclo.odetteftp.protocol.v20.OdetteFtpVer20Handler;

public class TestProtocolParseDate extends TestCase {

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
        assertEquals(calendar.get(Calendar.SECOND), 11);
        assertEquals(calendar.get(Calendar.MILLISECOND), 10);
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
        assertEquals(calendar.get(Calendar.SECOND), 11);
        assertEquals(calendar.get(Calendar.MILLISECOND), 10);
    }

}
