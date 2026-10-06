package com.example.contacts.util;
import android.telephony.PhoneNumberUtils;
public final class PhoneFormatter{
 private PhoneFormatter(){}
 public static String format(String s){if(s==null)return "";String n=PhoneNumberUtils.formatNumber(s);return n==null?s:n;}
 public static String normalize(String s){return s==null?"":s.replaceAll("[^0-9]","");}
 public static String last4(String s){String d=normalize(s);return d.length()<=4?d:d.substring(d.length()-4);}
 public static String last9(String s){String d=normalize(s);return d.length()<=9?d.substring(0):d.substring(d.length()-9);}
 public static boolean same(String a,String b){return last9(a).equals(last9(b));}
 public static String ltr(String s){return "\u200E"+(s==null?"":s);}
}