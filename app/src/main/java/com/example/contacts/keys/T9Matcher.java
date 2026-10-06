package com.example.contacts.keys;

import com.example.contacts.data.ContactModel;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class T9Matcher {
    private static final String[] EN = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
    private static final String[] HE = {"", "", "אבג", "דהו", "זחט", "יכל", "מנס", "עפצ", "קרש", "ת"};
    private T9Matcher() {}

    public static String buildT9(String name, boolean hebrew) {
        String[] map = hebrew ? HE : EN;
        StringBuilder b = new StringBuilder();
        if (name == null) return "";
        String lower = name.toLowerCase(Locale.US);
        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            int d = findDigit(ch, map);
            b.append(d >= 0 ? (char)('0' + d) : ' ');
        }
        return b.toString().trim().replace("  ", " ");
    }

    private static int findDigit(char c, String[] map) {
        for (int d = 2; d <= 9; d++) {
            if (map[d].indexOf(c) >= 0) return d;
        }
        return -1;
    }

    public static boolean matches(ContactModel c, String digits, boolean hebrew) {
        if (digits == null || digits.length() == 0) return true;
        String q = digits.trim();
        if (normalize(c.number).contains(q)) return true;
        String t9 = buildT9(c.name, hebrew).replace(" ", "");
        if (t9.contains(q)) return true;
        String[] words = c.name.trim().split("\\s+");
        for (String w : words) {
            String x = buildT9(w, hebrew).replace(" ", "");
            if (x.startsWith(q)) return true;
        }
        return false;
    }

    public static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("[^0-9+]", "");
    }
}
