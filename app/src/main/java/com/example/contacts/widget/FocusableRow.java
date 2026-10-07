package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.example.contacts.R;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;

public class FocusableRow extends LinearLayout {
    private final Paint marker = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float dp;
    private final int gap;

    public FocusableRow(Context c) {
        super(c);
        dp = c.getResources().getDisplayMetrics().density;
        gap = Ui.dim(c, R.dimen.row_gap);
        setFocusable(true);
        setFocusableInTouchMode(false);
        setWillNotDraw(false);
        setGravity(Gravity.CENTER_VERTICAL);
        setMinimumHeight(Ui.dim(c, R.dimen.row_height));
        setBackground(Ui.rowStates(c, 3));
        setPadding((int) (12 * dp), gap + (int) (5 * dp), (int) (12 * dp), gap + (int) (5 * dp));
        marker.setStyle(Paint.Style.FILL);
        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean focused) {
                if (AppPrefs.bool(getContext(), "animations", true)) {
                    animate().scaleX(focused ? 1.015f : 1f).scaleY(focused ? 1.015f : 1f).setDuration(110).start();
                } else {
                    setScaleX(1f);
                    setScaleY(1f);
                }
                invalidate();
            }
        });
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        if (isFocused() || isSelected()) {
            marker.setColor(Palette.accent(getContext()));
            float inset = 3 * dp + 2 * dp;
            float w = 5 * dp;
            float top = inset + 6 * dp;
            float bottom = getHeight() - inset - 6 * dp;
            if (bottom <= top) return;
            boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            float left = rtl ? getWidth() - inset - w : inset;
            rect.set(left, top, left + w, bottom);
            c.drawRoundRect(rect, 3 * dp, 3 * dp, marker);
        }
    }
}