package com.example.contacts.util;

import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** PIN dialogs used by the kids lock. */
public final class CodeDialog {
    private CodeDialog() {}
    public interface Done { void onDone(); }

    public static void setCode(final Context c, final Done done) {
        final Dialog d = Ui.dialog(c);
        LinearLayout root = Ui.panel(c, "קוד הגנה");
        root.addView(hint(c, "בחר קוד של 4 ספרות לפחות"), Ui.lp(c, -1, -2, 0, 8, 0, 4));
        final EditText input = input(c);
        root.addView(input, Ui.lp(c, -1, 56, 8, 4, 8, 8));
        TextView save = Ui.bar(c, "שמור", 16);
        save.setBackground(Ui.keyStates(c, 12, 2, true));
        root.addView(save, Ui.lp(c, -1, 48, 8, 0, 8, 0));
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String s = input.getText().toString();
                if (s.length() < 4) {
                    Toast.makeText(c, "יש להזין 4 ספרות לפחות", Toast.LENGTH_SHORT).show();
                    return;
                }
                AppPrefs.put(c, "guard_code", s);
                AppPrefs.put(c, "guard_enabled", true);
                d.dismiss();
                if (done != null) done.onDone();
            }
        });
        Ui.show(d, root);
        input.requestFocus();
    }

    public static void check(final Context c, final Done done) {
        if (!AppPrefs.bool(c, "guard_enabled", false)) {
            if (done != null) done.onDone();
            return;
        }
        final Dialog d = Ui.dialog(c);
        LinearLayout root = Ui.panel(c, "הקלד קוד הגנה");
        final EditText input = input(c);
        root.addView(input, Ui.lp(c, -1, 56, 8, 12, 8, 8));
        TextView ok = Ui.bar(c, "אישור", 16);
        ok.setBackground(Ui.keyStates(c, 12, 2, true));
        root.addView(ok, Ui.lp(c, -1, 48, 8, 0, 8, 0));
        final View.OnClickListener action = new View.OnClickListener() {
            public void onClick(View v) {
                if (AppPrefs.str(c, "guard_code", "1234").equals(input.getText().toString())) {
                    d.dismiss();
                    if (done != null) done.onDone();
                } else {
                    Toast.makeText(c, "קוד שגוי", Toast.LENGTH_SHORT).show();
                }
            }
        };
        ok.setOnClickListener(action);
        Ui.show(d, root);
        input.requestFocus();
        input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int key, KeyEvent e) {
                if (e.getAction() == KeyEvent.ACTION_UP && key == KeyEvent.KEYCODE_ENTER) {
                    action.onClick(v);
                    return true;
                }
                if (e.getAction() == KeyEvent.ACTION_UP && key == KeyEvent.KEYCODE_BACK) {
                    d.dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    private static TextView hint(Context c, String s) {
        TextView t = Ui.center(c, s, 13 * Palette.scale(c), Palette.secondary(c));
        return t;
    }

    private static EditText input(Context c) {
        EditText e = new EditText(c);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        e.setSingleLine(true);
        e.setTextSize(24 * Palette.scale(c));
        e.setTextColor(Palette.text(c));
        e.setGravity(Gravity.CENTER);
        e.setBackground(Ui.keyStates(c, 12, 0, false));
        e.setPadding(Ui.dp(c, 12), 0, Ui.dp(c, 12), 0);
        return e;
    }
}