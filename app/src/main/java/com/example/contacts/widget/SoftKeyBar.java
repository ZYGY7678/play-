package com.example.contacts.widget;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.contacts.R;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;
public class SoftKeyBar extends LinearLayout {
 public final Key left,center,right; private final int line;
 public SoftKeyBar(Context c,String l,String m,String r){super(c);setOrientation(VERTICAL);setBackgroundColor(Palette.bar(c));line=Palette.divider(c);View top=new View(c);top.setBackgroundColor(line);addView(top,new LayoutParams(-1,Math.max(1,Ui.dp(c,1))));LinearLayout row=new LinearLayout(c);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(Ui.dp(c,6),Ui.dp(c,2),Ui.dp(c,6),Ui.dp(c,2));left=new Key(c,l,false);center=new Key(c,m,true);right=new Key(c,r,false);int h=Ui.dim(c,R.dimen.softkey_height)-Ui.dp(c,6);row.addView(left,new LayoutParams(0,h,1));row.addView(center,new LayoutParams(0,h,1));row.addView(right,new LayoutParams(0,h,1));addView(row,new LayoutParams(-1,-2));}
 public static class Key extends TextView {private int normal;public Key(Context c,String label,boolean primary){super(c);setText(label);setGravity(Gravity.CENTER);setSingleLine(true);setTextSize(14*Palette.scale(c));setTypeface(Typeface.DEFAULT,primary?Typeface.BOLD:Typeface.NORMAL);setFocusable(true);setFocusableInTouchMode(false);normal=Palette.text(c);setTextColor(normal);setBackground(Ui.keyStates(c,12,2,true));setPadding(Ui.dp(c,6),0,Ui.dp(c,6),0);}public void setNormalColor(int color){normal=color;refresh();}private void refresh(){setTextColor(isFocused()?Palette.onAccent(getContext()):normal);}@Override protected void onFocusChanged(boolean gain,int direction,Rect previous){super.onFocusChanged(gain,direction,previous);refresh();}}
}