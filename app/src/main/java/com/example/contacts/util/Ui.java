package com.example.contacts.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Factory methods and drawables of the design system. */
public final class Ui {
    private Ui() {}

    // ---------------------------------------------------------------- units
    public static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int dim(Context c, int resId) {
        return c.getResources().getDimensionPixelSize(resId);
    }

    // ----------------------------------------------------------------- text
    public static TextView text(Context c, String s, float sp, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    public static TextView center(Context c, String s, float sp, int color) {
        TextView t = text(c, s, sp, color);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    public static TextView bold(TextView t) {
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    /** Big two-line empty state: bold title + secondary sub line. */
    public static TextView empty(Context c, String title, String sub) {
        SpannableStringBuilder sb = new SpannableStringBuilder(title);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new RelativeSizeSpan(1.2f), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sub != null && sub.length() > 0) sb.append("\n").append(sub);
        TextView t = center(c, "", 15 * Palette.scale(c), Palette.secondary(c));
        t.setText(sb);
        t.setLineSpacing(0f, 1.2f);
        t.setPadding(dp(c, 24), 0, dp(c, 24), 0);
        return t;
    }

    // ------------------------------------------------------------ drawables
    public static GradientDrawable rounded(int color, int strokeColor, int stroke, int radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        if (stroke > 0) g.setStroke(dp(c, stroke), strokeColor);
        return g;
    }

    public static GradientDrawable gradient(int start, int end, int strokeColor, int stroke, int radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{start, end});
        g.setCornerRadius(dp(c, radiusDp));
        if (stroke > 0) g.setStroke(dp(c, stroke), strokeColor);
        return g;
    }

    /** Card background used by list rows: surface + hairline, accent tint when focused/selected. */
    public static StateListDrawable rowStates(Context c, int insetDp) {
        int a = Palette.accent(c);
        GradientDrawable normal = rounded(Palette.row(c), Palette.divider(c), 1, 12, c);
        GradientDrawable focus = rounded(Palette.focusFill(c), a, 2, 12, c);
        GradientDrawable pressed = rounded(ColorUtil.mix(Palette.row(c), a, 0.40f), a, 2, 12, c);
        StateListDrawable s = new StateListDrawable();
        int in = dp(c, insetDp);
        s.addState(new int[]{android.R.attr.state_pressed}, new InsetDrawable(pressed, in));
        s.addState(new int[]{android.R.attr.state_focused}, new InsetDrawable(focus, in));
        s.addState(new int[]{android.R.attr.state_selected}, new InsetDrawable(focus, in));
        s.addState(new int[]{}, new InsetDrawable(normal, in));
        return s;
    }

    /** Transparent key/chip background that fills when focused. solid=true uses the full accent. */
    public static StateListDrawable keyStates(Context c, int radiusDp, int insetDp, boolean solid) {
        int a = Palette.accent(c);
        GradientDrawable normal = rounded(Color.TRANSPARENT, 0, 0, radiusDp, c);
        GradientDrawable focus = solid ? rounded(a, a, 0, radiusDp, c) : rounded(Palette.focusFill(c), a, 2, radiusDp, c);
        GradientDrawable pressed = rounded(ColorUtil.mix(Palette.bar(c), a, 0.55f), a, 0, radiusDp, c);
        StateListDrawable s = new StateListDrawable();
        int in = dp(c, insetDp);
        s.addState(new int[]{android.R.attr.state_pressed}, new InsetDrawable(pressed, in));
        s.addState(new int[]{android.R.attr.state_focused}, new InsetDrawable(focus, in));
        s.addState(new int[]{android.R.attr.state_selected}, new InsetDrawable(focus, in));
        s.addState(new int[]{}, new InsetDrawable(normal, in));
        return s;
    }

    /** Focusable centered label (used for compact buttons in dialogs). */
    public static TextView bar(Context c, String s, float sp) {
        TextView t = center(c, s, sp * Palette.scale(c), Palette.text(c));
        t.setFocusable(true);
        t.setFocusableInTouchMode(false);
        t.setBackground(keyStates(c, 10, 2, false));
        t.setPadding(dp(c, 12), 0, dp(c, 12), 0);
        return t;
    }

    /** Pill-shaped focusable chip with a visible resting state. */
    public static TextView chip(Context c, String s, float sp) {
        TextView t = center(c, s, sp * Palette.scale(c), Palette.text(c));
        t.setFocusable(true);
        t.setFocusableInTouchMode(false);
        StateListDrawable st = keyStates(c, 14, 2, false);
        t.setBackground(st);
        t.setPadding(dp(c, 10), 0, dp(c, 10), 0);
        return t;
    }

    public static View divider(Context c, int color, int heightDp) {
        View v = new View(c);
        v.setBackgroundColor(color);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.max(1, dp(c, heightDp))));
        return v;
    }

    public static LinearLayout.LayoutParams h(Context c, int dp) {
        return new LinearLayout.LayoutParams(-1, dp(c, dp));
    }

    public static LinearLayout.LayoutParams w(Context c, int dp) {
        return new LinearLayout.LayoutParams(dp(c, dp), -1);
    }

    /** Layout params with dp margins, e.g. lp(c,-1,52,0,8,0,0). */
    public static LinearLayout.LayoutParams lp(Context c, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                w > 0 ? dp(c, w) : w, h > 0 ? dp(c, h) : h);
        p.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        return p;
    }

    public static String initial(String name) {
        if (name == null) return "?";
        String n = name.trim();
        if (n.length() == 0) return "?";
        return n.substring(0, 1).toUpperCase(java.util.Locale.getDefault());
    }

    // -------------------------------------------------------------- dialogs
    /** Frameless dialog with a transparent window so our own card shows through. */
    public static Dialog dialog(Context c) {
        Dialog d = new Dialog(c);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return d;
    }

    /** Rounded card with a centered title – content is added below it. */
    public static LinearLayout panel(Context c, String title) {
        LinearLayout r = new LinearLayout(c);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setBackground(rounded(Palette.bar(c), Palette.divider(c), 1, 16, c));
        r.setPadding(dp(c, 10), dp(c, 8), dp(c, 10), dp(c, 10));
        TextView h = center(c, title, 19 * Palette.scale(c), Palette.text(c));
        bold(h);
        h.setSingleLine(true);
        h.setEllipsize(android.text.TextUtils.TruncateAt.END);
        r.addView(h, new LinearLayout.LayoutParams(-1, dp(c, 44)));
        r.addView(divider(c, Palette.divider(c), 1));
        return r;
    }

    public static void show(Dialog d, View content) {
        Context c = d.getContext();
        d.setContentView(content);
        Window w = d.getWindow();
        if (w != null) {
            int sw = c.getResources().getDisplayMetrics().widthPixels;
            w.setLayout(sw - 2 * dp(c, 10), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        d.show();
    }
}