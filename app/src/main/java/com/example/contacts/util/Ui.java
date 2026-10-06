package com.example.contacts.util;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    private Ui() {}

    public static int dp(Context c,int v) {
        return (int)(v*c.getResources().getDisplayMetrics().density+0.5f);
    }

    public static TextView text(Context c,String s,float sp,int color) {
        TextView t=new TextView(c);
        t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    public static TextView center(Context c,String s,float sp,int color) {
        TextView t=text(c,s,sp,color);t.setGravity(Gravity.CENTER);return t;
    }

    public static TextView bar(Context c,String s,float sp) {
        TextView t=center(c,s,sp,Palette.text(c));
        t.setFocusable(true);t.setFocusableInTouchMode(false);
        t.setBackgroundResource(com.example.contacts.R.drawable.row_selector);
        return t;
    }

    public static GradientDrawable rounded(int color,int strokeColor,int stroke,int radiusDp,Context c) {
        GradientDrawable g=new GradientDrawable();
        g.setColor(color);g.setCornerRadius(dp(c,radiusDp));
        if(stroke>0)g.setStroke(dp(c,stroke),strokeColor);
        return g;
    }

    public static GradientDrawable gradient(int start,int end,int strokeColor,int stroke,int radiusDp,Context c) {
        GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{start,end});
        g.setCornerRadius(dp(c,radiusDp));
        if(stroke>0)g.setStroke(dp(c,stroke),strokeColor);
        return g;
    }

    public static View divider(Context c,int color,int heightDp) {
        View v=new View(c);v.setBackgroundColor(color);
        return v;
    }

    public static LinearLayout.LayoutParams h(Context c,int dp) {
        return new LinearLayout.LayoutParams(-1,dp(c,dp));
    }

    public static LinearLayout.LayoutParams w(Context c,int dp) {
        return new LinearLayout.LayoutParams(dp(c,dp),-1);
    }
}