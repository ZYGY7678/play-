package com.example.contacts.util;

import android.graphics.Color;

/** Small color helpers shared by the design system. */
public final class ColorUtil {
    private ColorUtil() {}

    /** Deterministic avatar color for a name – dark enough for white initials. */
    public static int stableColor(String s) {
        int h = s == null ? 0 : s.hashCode();
        float hue = (h & 0xFF) * 360f / 255f;
        return Color.HSVToColor(new float[]{hue, 0.50f, 0.62f});
    }

    /** Linear blend: t=0 returns base, t=1 returns over. */
    public static int mix(int base, int over, float t) {
        float k = Math.max(0f, Math.min(1f, t));
        int r = (int) (Color.red(base) * (1 - k) + Color.red(over) * k);
        int g = (int) (Color.green(base) * (1 - k) + Color.green(over) * k);
        int b = (int) (Color.blue(base) * (1 - k) + Color.blue(over) * k);
        return Color.rgb(r, g, b);
    }

    /** Same color with a new alpha (0-255). */
    public static int alpha(int color, int a) {
        return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color));
    }
}