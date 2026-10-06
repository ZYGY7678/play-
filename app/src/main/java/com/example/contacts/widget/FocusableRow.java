package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;

public class FocusableRow extends LinearLayout {
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int dp;
    public FocusableRow(Context c) {
        super(c);
        dp = (int)(c.getResources().getDisplayMetrics().density + 0.5f);
        setFocusable(true);
        setFocusableInTouchMode(false);
        setBackgroundResource(com.example.contacts.R.drawable.row_selector);
        setWillNotDraw(false);
        setOrientation(HORIZONTAL);
        setPadding(8*dp, 6*dp, 8*dp, 6*dp);
        setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                animate().scaleX(hasFocus ? 1.03f : 1f).scaleY(hasFocus ? 1.03f : 1f).setDuration(120).start();
                invalidate();
            }
        });
    }
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (isFocused()) {
            barPaint.setColor(Color.rgb(0x3D,0xA5,0xFF));
            c.drawRect(0,0,6*dp,getHeight(),barPaint);
        }
    }
}
