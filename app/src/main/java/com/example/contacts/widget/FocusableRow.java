package com.example.contacts.widget;
import android.content.Context;import android.graphics.Canvas;import android.graphics.Paint;import android.view.View;import android.widget.LinearLayout;import com.example.contacts.util.Palette;
public class FocusableRow extends LinearLayout{
 private final Paint p=new Paint(1);private final int dp;
 public FocusableRow(Context c){super(c);dp=(int)(c.getResources().getDisplayMetrics().density+0.5f);setFocusable(true);setFocusableInTouchMode(false);setWillNotDraw(false);setPadding(8*dp,5*dp,8*dp,5*dp);setBackgroundResource(com.example.contacts.R.drawable.row_selector);setOnFocusChangeListener(new View.OnFocusChangeListener(){public void onFocusChange(View v,boolean b){animate().scaleX(b?1.03f:1f).scaleY(b?1.03f:1f).setDuration(120).start();invalidate();}});}
 protected void onDraw(Canvas c){super.onDraw(c);if(isFocused()){p.setColor(Palette.accent(getContext()));c.drawRect(0,0,6*dp,getHeight(),p);}}
}