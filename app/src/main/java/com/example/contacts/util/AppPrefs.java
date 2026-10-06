package com.example.contacts.util;
import android.content.Context;
import android.content.SharedPreferences;
public final class AppPrefs{
 private AppPrefs(){}
 public static SharedPreferences get(Context c){return c.getSharedPreferences("keycontacts",Context.MODE_PRIVATE);}
 public static boolean bool(Context c,String k,boolean d){return get(c).getBoolean(k,d);}
 public static String str(Context c,String k,String d){return get(c).getString(k,d);}
 public static int i(Context c,String k,int d){return get(c).getInt(k,d);}
}