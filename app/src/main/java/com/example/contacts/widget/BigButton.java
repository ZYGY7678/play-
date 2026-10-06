package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.TextView;

public class BigButton extends TextView {
    public BigButton(Context c, String label) {
        super(c);
        setText(label);
        setTextSize(17);
        setGravity(Gravity.CENTER);
        setTextColor(Color.WHITE);
        setFocusable(true);
        setFocusableInTouchMode(false);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Color.rgb(0x1B,0x1F,0x26));
        g.setCornerRadius(12 * c.getResources().getDisplayMetrics().density);
        g.setStroke((int)(2*c.getResources().getDisplayMetrics().density), Color.rgb(0x3D,0xA5,0xFF));
        setBackground(g);
    }
}
