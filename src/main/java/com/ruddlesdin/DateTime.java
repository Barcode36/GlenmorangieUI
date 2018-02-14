package com.ruddlesdin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by p_ruddlesdin on 22/03/2017.
 */
public class DateTime {

    public DateTime() {

    }

    public String getCurrentTimeStamp(String format) {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Date now = new Date();
        DateFormat df = new SimpleDateFormat(format);
        df.setTimeZone(tz);
        String currentTime = df.format(now);
        return currentTime;
    }
}
