package com.example.contacts.util;

import android.telephony.PhoneNumberUtils;

public final class PhoneFormatter {
    private PhoneFormatter() {}

    public static String format(String s) {
        if (s == null) return "";
        String n = PhoneNumberUtils.formatNumber(s);
        return n == null ? s : n;
    }

    public static String normalizeLast9(String s) {
        if (s == null) return "";
        String d = s.replaceAll("[^0-9]", "");
        if (d.length() <= 9) return d;
        return d.substring(d.length() - 9);
    }

    public static boolean sameNumber(String a, String b) {
        return normalizeLast9(a).equals(normalizeLast9(b));
    }
}
