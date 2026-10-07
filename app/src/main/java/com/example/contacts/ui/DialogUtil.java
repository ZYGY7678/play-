package com.example.contacts.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.FocusableRow;

/** Action-sheet dialog: numbered rows, keys 1-9 pick directly, BACK closes. */
public final class DialogUtil {
    private DialogUtil() {}

    public interface Choice { void onChoice(int which); }

    public static Dialog actions(final Context c, final String title, final String[] items, final Choice cb) {
        final Dialog d = Ui.dialog(c);
        LinearLayout root = Ui.panel(c, title);

        final ListView list = new ListView(c);
        list.setDivider(null);
        list.setSelector(new ColorDrawable(Color.TRANSPARENT));
        list.setItemsCanFocus(true);
        list.setVerticalScrollBarEnabled(false);
        list.setAdapter(new BaseAdapter() {
            public int getCount() { return items.length; }
            public Object getItem(int p) { return items[p]; }
            public long getItemId(int p) { return p; }
            public View getView(final int p, View v, ViewGroup par) {
                FocusableRow r = new FocusableRow(c);
                r.setMinimumHeight(Ui.dp(c, 50));
                TextView num = Ui.center(c, p < 9 ? String.valueOf(p + 1) : "", 14 * Palette.scale(c), Palette.accent(c));
                Ui.bold(num);
                num.setBackground(Ui.rounded(ColorUtil.alpha(Palette.accent(c), 40), 0, 0, 14, c));
                r.addView(num, new LinearLayout.LayoutParams(Ui.dp(c, 28), Ui.dp(c, 28)));
                TextView t = Ui.text(c, items[p], 18 * Palette.scale(c), Palette.text(c));
                t.setGravity(Gravity.CENTER_VERTICAL);
                t.setSingleLine(true);
                t.setEllipsize(android.text.TextUtils.TruncateAt.END);
                t.setPadding(Ui.dp(c, 12), 0, 0, 0);
                r.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
                r.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { d.dismiss(); cb.onChoice(p); }
                });
                return r;
            }
        });

        int rowH = Ui.dp(c, 56);
        int maxH = (int) (c.getResources().getDisplayMetrics().heightPixels * 0.62f);
        int want = Math.min(items.length * rowH + Ui.dp(c, 6), maxH);
        root.addView(list, new LinearLayout.LayoutParams(-1, want));

        Ui.show(d, root);
        list.requestFocus();
        list.setSelection(0);
        list.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int key, KeyEvent e) {
                if (e.getAction() == KeyEvent.ACTION_UP && key >= KeyEvent.KEYCODE_1 && key <= KeyEvent.KEYCODE_9) {
                    int i = key - KeyEvent.KEYCODE_1;
                    if (i < items.length) { d.dismiss(); cb.onChoice(i); return true; }
                }
                if (e.getAction() == KeyEvent.ACTION_UP
                        && (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_ENDCALL)) {
                    d.dismiss();
                    return true;
                }
                return false;
            }
        });
        return d;
    }
}