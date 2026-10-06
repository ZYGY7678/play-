package com.example.contacts.util;

import java.util.concurrent.TimeUnit;

public final class TimeUtil {
    private TimeUtil() {}
    public static String relative(long when) {
        long d = Math.max(0, System.currentTimeMillis() - when);
        long mins = TimeUnit.MILLISECONDS.toMinutes(d);
        if (mins < 1) return "עכשיו";
        if (mins < 60) return "לפני " + mins + " דק'";
        long h = mins / 60;
        if (h < 24) return "לפני " + h + " ש'";
        if (h < 48) return "אתמול";
        return "לפני " + (h / 24) + " ימים";
    }
}
