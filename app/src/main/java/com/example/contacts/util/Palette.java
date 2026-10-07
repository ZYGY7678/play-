package com.example.contacts.util;

import android.content.Context;
import android.graphics.Color;

/**
 * Runtime design tokens. Three themes (names are the values stored in settings):
 * dark (default), light, high contrast. Every screen takes its colors from here.
 */
public final class Palette {
    private Palette() {}

    public static final String THEME_LIGHT = "בהירה";
    public static final String THEME_CONTRAST = "ניגודיות גבוהה";

    public static final int GREEN = Color.rgb(0x34, 0xD2, 0x7B);
    public static final int RED = Color.rgb(0xFF, 0x5C, 0x61);
    public static final int YELLOW = Color.rgb(0xFF, 0xC8, 0x57);

    private static String theme(Context c) { return AppPrefs.str(c, "theme", "כהה"); }
    public static boolean light(Context c) { return THEME_LIGHT.equals(theme(c)); }
    public static boolean contrast(Context c) { return THEME_CONTRAST.equals(theme(c)); }

    /** Screen background. */
    public static int bg(Context c) {
        if (light(c)) return Color.rgb(0xF2, 0xF4, 0xF7);
        if (contrast(c)) return Color.BLACK;
        return Color.rgb(0x0E, 0x11, 0x16);
    }

    /** Card / row surface. */
    public static int row(Context c) {
        if (light(c)) return Color.WHITE;
        if (contrast(c)) return Color.rgb(0x10, 0x10, 0x10);
        return Color.rgb(0x17, 0x1B, 0x22);
    }

    /** Raised surface (chips, inputs, hero cards). */
    public static int surfaceHigh(Context c) {
        if (light(c)) return Color.rgb(0xE3, 0xEA, 0xF3);
        if (contrast(c)) return Color.rgb(0x26, 0x26, 0x26);
        return Color.rgb(0x21, 0x27, 0x33);
    }

    /** Header / soft-key bar. */
    public static int bar(Context c) {
        if (light(c)) return Color.WHITE;
        if (contrast(c)) return Color.BLACK;
        return Color.rgb(0x09, 0x0B, 0x0F);
    }

    public static int divider(Context c) {
        if (light(c)) return Color.rgb(0xD8, 0xDE, 0xE6);
        if (contrast(c)) return Color.rgb(0x80, 0x80, 0x80);
        return Color.rgb(0x25, 0x2B, 0x35);
    }

    public static int text(Context c) {
        if (light(c)) return Color.rgb(0x12, 0x16, 0x1C);
        if (contrast(c)) return Color.WHITE;
        return Color.rgb(0xF4, 0xF6, 0xF8);
    }

    public static int secondary(Context c) {
        if (light(c)) return Color.rgb(0x5B, 0x65, 0x73);
        if (contrast(c)) return Color.rgb(0xDD, 0xDD, 0xDD);
        return Color.rgb(0x98, 0xA2, 0xB3);
    }

    public static int accent(Context c) {
        if (light(c)) return Color.rgb(0x0A, 0x74, 0xDA);
        if (contrast(c)) return Color.rgb(0xFF, 0xE6, 0x00);
        return Color.rgb(0x3D, 0xA5, 0xFF);
    }

    /** Text/icon color to use on top of a solid accent fill. */
    public static int onAccent(Context c) {
        if (light(c)) return Color.WHITE;
        if (contrast(c)) return Color.BLACK;
        return Color.rgb(0x06, 0x12, 0x1F);
    }

    /** Fill used for the focused / selected state of rows and keys. */
    public static int focusFill(Context c) {
        float t = contrast(c) ? 0.30f : (light(c) ? 0.16f : 0.24f);
        return ColorUtil.mix(row(c), accent(c), t);
    }

    public static float scale(Context c) {
        String s = AppPrefs.str(c, "font_size", "רגיל");
        return "גדול".equals(s) ? 1.15f : ("ענק".equals(s) ? 1.4f : 1f);
    }
}