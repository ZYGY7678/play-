package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.LinearLayout;

import com.example.contacts.util.Palette;

public class FocusableRow extends LinearLayout {
    private final Paint bar=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int dp;

    public FocusableRow(Context c) {
        super(c);
        dp=(int)(c.getResources().getDisplayMetrics().density+0.5f);
        setFocusable(true);
        setFocusableInTouchMode(false);
        setWillNotDraw(false);
        setPadding(8*dp,5*dp,8*dp,5*dp);
        setMinimumHeight(72*dp);
        setBackground(buildStates(c));
        setOnFocusChangeListener(new View.OnFocusChangeListener(){
            public void onFocusChange(View v,boolean focused){
                if(com.example.contacts.util.AppPrefs.bool(getContext(),"animations",true)){
                    animate().scaleX(focused?1.03f:1f).scaleY(focused?1.03f:1f).setDuration(120).start();
                } else {
                    setScaleX(focused?1.0f:1f);setScaleY(1f);
                }
                invalidate();
            }
        });
    }

    private StateListDrawable buildStates(Context c) {
        int a=Palette.accent(c);
        GradientDrawable focus=new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.argb(217,Color.red(a),Color.green(a),Color.blue(a)),
                           Color.argb(140,Color.red(a),Color.green(a),Color.blue(a))});
        focus.setCornerRadius(10*dp);focus.setStroke(2*dp,a);
        GradientDrawable pressed=new GradientDrawable();pressed.setColor(Color.argb(130,Color.red(a),Color.green(a),Color.blue(a)));pressed.setCornerRadius(10*dp);
        GradientDrawable normal=new GradientDrawable();normal.setColor(Palette.row(c));normal.setCornerRadius(10*dp);
        StateListDrawable s=new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_pressed},pressed);
        s.addState(new int[]{android.R.attr.state_focused},focus);
        s.addState(new int[]{android.R.attr.state_selected},focus);
        s.addState(new int[]{},normal);
        return s;
    }

    protected void onDraw(Canvas c){
        super.onDraw(c);
        if(isFocused()){
            bar.setColor(Palette.accent(getContext()));
            c.drawRect(0,0,6*dp,getHeight(),bar);
        }
    }
}