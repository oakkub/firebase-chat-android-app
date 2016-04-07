package com.oakkub.chat.models;

import java.util.concurrent.TimeUnit;

/**
 * Created by OaKKuB on 3/24/2016.
 */
public class TimeOffset {

    public final long seconds;
    public final long minutes;
    public final long hours;
    public final long days;

    public TimeOffset(long resultTime) {
        seconds = TimeUnit.MILLISECONDS.toSeconds(resultTime);
        minutes = TimeUnit.MILLISECONDS.toMinutes(resultTime);
        hours = TimeUnit.MILLISECONDS.toHours(resultTime);
        days = TimeUnit.MILLISECONDS.toDays(resultTime);
    }

}
