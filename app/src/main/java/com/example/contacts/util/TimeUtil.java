package com.example.contacts.util;
public final class TimeUtil{
 private TimeUtil(){}
 public static String relative(long when){long d=Math.max(0,System.currentTimeMillis()-when),m=d/60000L;if(m<1)return "עכשיו";if(m<60)return "לפני "+m+" דק'";long h=m/60;if(h<24)return "לפני "+h+" ש'";if(h<48)return "אתמול";return "לפני "+h/24+" ימים";}
}