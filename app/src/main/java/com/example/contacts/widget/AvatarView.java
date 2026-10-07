package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.widget.ImageView;

import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;

public class AvatarView extends ImageView {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint letter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clip = new Path();
    private String initial = "?";
    private boolean showRing = false;

    public AvatarView(Context c) {
        super(c);
        setScaleType(ScaleType.CENTER_CROP);
        setFocusable(false);
        setClickable(false);
        fill.setColor(ColorUtil.stableColor(null));
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeWidth(Ui.dp(c, 3));
        letter.setColor(0xFFFFFFFF);
        letter.setTextAlign(Paint.Align.CENTER);
        letter.setFakeBoldText(true);
    }

    public void setName(String name) {
        initial = Ui.initial(name);
        fill.setColor(ColorUtil.stableColor(name));
        invalidate();
    }

    public void setRing(boolean on) {
        showRing = on;
        invalidate();
    }

    @Override public void setImageBitmap(Bitmap b) {
        if (b == null) setImageDrawable(null);
        else super.setImageBitmap(b);
    }

    @Override protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float r = Math.min(w, h) / 2f, cx = w / 2f, cy = h / 2f;
        clip.reset();
        clip.addCircle(cx, cy, r, Path.Direction.CW);
        c.save();
        c.clipPath(clip);
        if (getDrawable() == null) {
            c.drawCircle(cx, cy, r, fill);
            letter.setTextSize(r * 0.95f);
            Paint.FontMetrics fm = letter.getFontMetrics();
            c.drawText(initial, cx, cy - (fm.ascent + fm.descent) / 2f, letter);
        } else {
            super.onDraw(c);
        }
        c.restore();
        if (showRing) {
            ring.setColor(Palette.accent(getContext()));
            c.drawCircle(cx, cy, r - ring.getStrokeWidth() / 2f, ring);
        }
    }
}