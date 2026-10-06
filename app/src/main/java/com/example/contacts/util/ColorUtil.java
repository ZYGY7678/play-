package com.example.contacts.util;

import android.graphics.Color;

public final class ColorUtil {
    private ColorUtil() {}
    public static final int BG = Color.rgb(0x12,0x14,0x18);
    public static final int ROW = Color.rgb(0x1B,0x1F,0x26);
    public static final int BAR = Color.rgb(0x0D,0x0F,0x13);
    public static final int TEXT = Color.rgb(0xF2,0xF4,0xF7);
    public static final int SECONDARY = Color.rgb(0x9A,0xA3,0xAF);
    public static final int ACCENT = Color.rgb(0x3D,0xA5,0xFF);
    public static final int GREEN = Color.rgb(0x2E,0xD4,0x7A);
    public static final int RED = Color.rgb(0xFF,0x5A,0x5F);
    public static final int YELLOW = Color.rgb(0xFF,0xC8,0x57);

    public static int stableColor(String s) {
        int h = s == null ? 0 : s.hashCode();
        float hue = (h & 0xFF) * 360f / 255f;
        return Color.HSVToColor(new float[]{hue, 0.55f, 0.85f});
    }
}
