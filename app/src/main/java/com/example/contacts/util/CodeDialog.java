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

public final class CodeDialog {
    private CodeDialog() {}
    public interface Done { void onDone(); }

    public static void setCode(final Context c, final Done done) {
        final Dialog d = new Dialog(c);
        LinearLayout root = base(c, "קוד הגנה");
        final EditText input = input(c);
        root.addView(input, new LinearLayout.LayoutParams(-1, 58));
        TextView save = Ui.bar(c, "שמור", 15);
        root.addView(save, new LinearLayout.LayoutParams(-1, 50));
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
        d.setContentView(root);
        d.show();
        input.requestFocus();
    }

    public static void check(final Context c, final Done done) {
        if (!AppPrefs.bool(c, "guard_enabled", false)) {
            if (done != null) done.onDone();
            return;
        }
        final Dialog d = new Dialog(c);
        LinearLayout root = base(c, "הקלד קוד הגנה");
        final EditText input = input(c);
        root.addView(input, new LinearLayout.LayoutParams(-1, 58));
        TextView ok = Ui.bar(c, "אישור", 15);
        root.addView(ok, new LinearLayout.LayoutParams(-1, 50));
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
        d.setContentView(root);
        d.show();
        input.requestFocus();
        input.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int key, KeyEvent e) {
                if (e.getAction() == KeyEvent.ACTION_UP && key == KeyEvent.KEYCODE_ENTER) {
                    action.onClick(v); return true;
                }
                if (e.getAction() == KeyEvent.ACTION_UP && key == KeyEvent.KEYCODE_BACK) {
                    d.dismiss(); return true;
                }
                return false;
            }
        });
    }

    private static LinearLayout base(Context c, String title) {
        LinearLayout r = new LinearLayout(c);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(18, 12, 18, 12);
        r.setBackgroundColor(Palette.bar(c));
        TextView h = Ui.bar(c, title, 20);
        r.addView(h, new LinearLayout.LayoutParams(-1, 48));
        return r;
    }

    private static EditText input(Context c) {
        EditText e = new EditText(c);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        e.setSingleLine(true);
        e.setTextSize(22);
        e.setTextColor(Palette.text(c));
        e.setGravity(Gravity.CENTER);
        return e;
    }
}