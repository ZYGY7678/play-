package com.example.contacts.util;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public final class HebrewUtil {
    private HebrewUtil() {}

    public static String first(String s) {
        if (s == null || s.trim().length() == 0) return "#";
        return s.trim().substring(0,1).toUpperCase(Locale.getDefault());
    }

    public static Comparator<String> collator(final boolean byFamily) {
        final Collator c = Collator.getInstance(new Locale("he","IL"));
        c.setStrength(Collator.PRIMARY);
        return new Comparator<String>() {
            public int compare(String a, String b) {
                return c.compare(a == null ? "" : a, b == null ? "" : b);
            }
        };
    }
}
