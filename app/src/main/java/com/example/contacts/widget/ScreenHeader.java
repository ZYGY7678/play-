package com.example.contacts.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.contacts.R;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;

/** Simple screen title bar with a hairline underneath. */
public class ScreenHeader extends LinearLayout {
    public final TextView title;

    public ScreenHeader(Context c, String text) {
        super(c);
        setOrientation(VERTICAL);
        setBackgroundColor(Palette.bar(c));
        title = Ui.bold(Ui.center(c, text, 21 * Palette.scale(c), Palette.text(c)));
        title.setSingleLine(true);
        title.setPadding(Ui.dp(c, 16), 0, Ui.dp(c, 16), 0);
        addView(title, new LayoutParams(-1, Ui.dim(c, R.dimen.header_height)));
        View line = new View(c);
        line.setBackgroundColor(Palette.divider(c));
        addView(line, new LayoutParams(-1, Math.max(1, Ui.dp(c, 1))));
    }
}