package com.example.contacts.call;

public final class CallStateHolder {
    private CallStateHolder() {}
    public static String number="";
    public static String direction="unknown";
    public static long startTime=0L;
    public static boolean isMuted=false;
    public static boolean isSpeaker=false;
    public static boolean active=false;
    public static boolean incoming=false;
}
