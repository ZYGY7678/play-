package com.example.contacts.util;
import android.content.Context;
import android.graphics.Color;
public final class Palette{
 private Palette(){}
 public static final int DARK_BG=Color.rgb(0x12,0x14,0x18),DARK_ROW=Color.rgb(0x1B,0x1F,0x26),DARK_BAR=Color.rgb(0x0D,0x0F,0x13),DARK_TEXT=Color.rgb(0xF2,0xF4,0xF7),DARK_SECONDARY=Color.rgb(0x9A,0xA3,0xAF),DARK_ACCENT=Color.rgb(0x3D,0xA5,0xFF),GREEN=Color.rgb(0x2E,0xD4,0x7A),RED=Color.rgb(0xFF,0x5A,0x5F),YELLOW=Color.rgb(0xFF,0xC8,0x57);
 public static int bg(Context c){String t=AppPrefs.str(c,"theme","כהה");return "בהירה".equals(t)?Color.rgb(0xF6,0xF8,0xFA):("ניגודיות גבוהה".equals(t)?Color.BLACK:DARK_BG);}
 public static int row(Context c){String t=AppPrefs.str(c,"theme","כהה");return "בהירה".equals(t)?Color.WHITE:DARK_ROW;}
 public static int bar(Context c){return "בהירה".equals(AppPrefs.str(c,"theme","כהה"))?Color.rgb(0xEE,0xF1,0xF5):DARK_BAR;}
 public static int text(Context c){return "בהירה".equals(AppPrefs.str(c,"theme","כהה"))?Color.rgb(0x14,0x18,0x1F):Color.WHITE;}
 public static int secondary(Context c){return "בהירה".equals(AppPrefs.str(c,"theme","כהה"))?Color.rgb(0x5B,0x65,0x73):DARK_SECONDARY;}
 public static int accent(Context c){String t=AppPrefs.str(c,"theme","כהה");return "ניגודיות גבוהה".equals(t)?Color.rgb(0xFF,0xE6,0x00):("בהירה".equals(t)?Color.rgb(0x0A,0x74,0xDA):DARK_ACCENT);}
 public static float scale(Context c){String s=AppPrefs.str(c,"font_size","רגיל");return "גדול".equals(s)?1.15f:("ענק".equals(s)?1.4f:1f);}
}