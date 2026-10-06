package com.example.contacts.util;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
public final class Ui{
 private Ui(){}
 public static TextView text(Context c,String s,float sp,int color){TextView t=new TextView(c);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 public static TextView center(Context c,String s,float sp,int color){TextView t=text(c,s,sp,color);t.setGravity(Gravity.CENTER);return t;}
 public static TextView bar(Context c,String s,float sp){TextView t=center(c,s,sp,Palette.text(c));t.setFocusable(true);t.setFocusableInTouchMode(false);t.setBackgroundResource(com.example.contacts.R.drawable.row_selector);return t;}
 public static int dp(Context c,int v){return (int)(v*c.getResources().getDisplayMetrics().density+0.5f);}
 public static LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w,h);}
 public static GradientDrawable rounded(int color,int strokeColor,int stroke,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(radius);if(stroke>0)g.setStroke(stroke,strokeColor);return g;}
}