package com.example.contacts.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    private Ui() {}
    public static int dp(Context c,float v){return (int)(v*c.getResources().getDisplayMetrics().density+0.5f);}
    public static TextView text(Context c,String s,float sp,int color){
        TextView t=new TextView(c);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);return t;
    }
    public static TextView bar(Context c,String s,float sp){
        TextView t=text(c,s,sp,ColorUtil.TEXT);t.setGravity(Gravity.CENTER);t.setFocusable(true);t.setFocusableInTouchMode(false);return t;
    }
    public static GradientDrawable bg(int color,float radius,int strokeColor,int stroke){
        GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);if(stroke>0)g.setStroke(stroke,strokeColor);return g;
    }
    public static LinearLayout.LayoutParams params(int w,int h){return new LinearLayout.LayoutParams(w,h);}
}
