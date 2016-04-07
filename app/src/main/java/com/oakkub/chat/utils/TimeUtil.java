package com.oakkub.chat.utils;

import android.content.Context;

import com.oakkub.chat.R;
import com.oakkub.chat.managers.Contextor;
import com.oakkub.chat.models.TimeOffset;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by OaKKuB on 12/4/2015.
 */
public class TimeUtil {

    private static final String[] formats = new String[] {
        "MMM, dd yyyy",
        "MMM, dd",
        "EEEE",
        "HH:mm",
        "dd MMMM",
        "mm",
        "HH"
    };

    public static String getOnlineTime(long currentTime, long specificTime) {
        Context context = Contextor.getInstance().getContext();
        long resultTime = currentTime - specificTime;

        String time;
        TimeOffset timeOffset = new TimeOffset(resultTime);

        if (timeOffset.days > 0) {
            time = context.getString(R.string.online_n_day, timeOffset.days);
        } else if (timeOffset.hours > 0) {
            time = context.getString(R.string.online_n_hour, timeOffset.hours);
        } else if (timeOffset.minutes > 0) {
            time = context.getString(R.string.online_n_minute, timeOffset.minutes);
        } else {
            time = context.getString(R.string.online_n_minute, 1);
        }

        return time;
    }

    public static String readableTime(Context context, long timeInMillis) {
        GregorianCalendar target = getCalendar(timeInMillis);
        GregorianCalendar present = new GregorianCalendar();

        int targetYear = target.get(Calendar.YEAR);
        int targetDayOfYear = target.get(Calendar.DAY_OF_YEAR);

        int presentYear = target.get(Calendar.YEAR);
        int presentDayOfYear = present.get(Calendar.DAY_OF_YEAR);

        String readableTime;

        int yearOffset = presentYear - targetYear;
        int dayOfYearOffset = presentDayOfYear - targetDayOfYear;

        if (yearOffset >= 1) {

            readableTime = new SimpleDateFormat(formats[0]).format(target.getTime());

        } else if (dayOfYearOffset > 7) {

            readableTime = new SimpleDateFormat(formats[1]).format(target.getTime());

        } else if (dayOfYearOffset > 1) {

            readableTime = new SimpleDateFormat(formats[2]).format(target.getTime());

        } else if (dayOfYearOffset == 1) {

            readableTime = context.getString(R.string.yesterday);

        } else {

            readableTime = new SimpleDateFormat(formats[3]).format(target.getTime());
        }

        return readableTime;
    }

    public static String getReadableDate(long timeInMillis) {
        GregorianCalendar target = getCalendar(timeInMillis);
        GregorianCalendar present = new GregorianCalendar();

        int targetYear = target.get(Calendar.YEAR);
        int presentYear = present.get(Calendar.YEAR);

        if (targetYear == presentYear) {
            return new SimpleDateFormat(formats[4]).format(target.getTime());
        } else {
            return new SimpleDateFormat(formats[0]).format(target.getTime());
        }
    }

    public static String getOnlyTime(long timeInMillis) {
        GregorianCalendar calendar = getCalendar(timeInMillis);
        return new SimpleDateFormat(formats[3]).format(calendar.getTime());
    }

    public static boolean isLeftDayGreaterThanRight(long timeInMillisLeft, long timeInMillisRight) {
        GregorianCalendar left = getCalendar(timeInMillisLeft);
        GregorianCalendar right = getCalendar(timeInMillisRight);

        int leftYear = left.get(Calendar.YEAR);
        int rightYear = right.get(Calendar.YEAR);
        int yearOffset = leftYear - rightYear;

        int leftDayOfYear = left.get(Calendar.DAY_OF_YEAR);
        int rightDayOfYear = right.get(Calendar.DAY_OF_YEAR);

        if (yearOffset == 0) return leftDayOfYear > rightDayOfYear;
        else return yearOffset > 0;
    }

    public static String timeInMillisToDate(String datePattern, long timeInMillis, boolean yearInBuddhist) {
        GregorianCalendar calendar = getCalendar(timeInMillis);

        if (yearInBuddhist) {
            calendar.add(Calendar.YEAR, 543);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern, Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    public static GregorianCalendar getCalendar(long timeInMillis) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timeInMillis);

        return calendar;
    }

}
