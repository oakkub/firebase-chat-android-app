package com.oakkub.chat.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by OaKKuB on 12/4/2015.
 */
public class TimeUtil {

    public static String timeInMillisToDate(String datePattern, long timeInMillis, boolean yearInBuddhist) {
        GregorianCalendar calendar = getCalendar(timeInMillis);

        if (yearInBuddhist) {
            calendar.add(Calendar.YEAR, 543);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private static GregorianCalendar getCalendar(long timeInMillis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timeInMillis);

        return calendar;
    }

}
