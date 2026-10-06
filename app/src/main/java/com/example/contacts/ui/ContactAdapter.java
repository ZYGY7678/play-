package com.example.contacts.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.contacts.data.ContactModel;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.widget.FocusableRow;

import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private final Context context;
    private List<ContactModel> data;
    public int actionMode = 0;

    public ContactAdapter(Context c, List<ContactModel> d) { context=c; data=d; }
    public void setData(List<ContactModel> d) { data=d; notifyDataSetChanged(); }
    public ContactModel getItem(int p) { return data.get(p); }
    public int getCount() { return data == null ? 0 : data.size(); }
    public long getItemId(int p) { return data.get(p).id; }

    public View getView(int pos, View convert, ViewGroup parent) {
        Holder h;
        FocusableRow row;
        if (convert instanceof FocusableRow) {
            row=(FocusableRow)convert;
            h=(Holder)row.getTag();
        } else {
            row=new FocusableRow(context);
            h=new Holder(context);
            row.setTag(h);
            row.addView(h.avatar, new LinearLayout.LayoutParams(54,54));
            row.addView(h.textBox, new LinearLayout.LayoutParams(0,-1,1f));
            row.addView(h.action, new LinearLayout.LayoutParams(42,54));
        }
        ContactModel c=data.get(pos);
        String name=c.name.length()==0 ? "ללא שם" : c.name;
        h.name.setText(name);
        h.name.setTextSize(20);
        h.name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        h.name.setTextColor(ColorUtil.TEXT);
        String second=c.number.length()>0 ? PhoneFormatter.format(c.number) : (c.type.length()>0?c.type:"אין מספר");
        h.number.setText(second);
        h.number.setTextSize(15);
        h.number.setTextColor(ColorUtil.SECONDARY);
        h.star.setText(c.favorite ? "★" : "");
        h.star.setTextColor(ColorUtil.YELLOW);
        h.star.setTextSize(19);
        h.avatar.setText(name.substring(0,1).toUpperCase());
        h.avatar.setTextColor(Color.WHITE);
        h.avatar.setTextSize(21);
        h.avatar.setGravity(Gravity.CENTER);
        h.avatar.setBackgroundColor(ColorUtil.stableColor(name));
        if (actionMode == 1) h.action.setText("☎");
        else if (actionMode == 2) h.action.setText("✉");
        else if (actionMode == 3) h.action.setText("ⓘ");
        else h.action.setText("›");
        h.action.setTextColor(actionMode==1?ColorUtil.GREEN:ColorUtil.ACCENT);
        h.action.setTextSize(25);
        h.action.setGravity(Gravity.CENTER);
        return row;
    }

    private static class Holder {
        TextView avatar, name, number, star, action;
        LinearLayout textBox;
        Holder(Context c) {
            avatar=new TextView(c); name=new TextView(c); number=new TextView(c);
            star=new TextView(c); action=new TextView(c); textBox=new LinearLayout(c);
            textBox.setOrientation(LinearLayout.VERTICAL);
            LinearLayout line=new LinearLayout(c);line.setGravity(Gravity.CENTER_VERTICAL);
            line.addView(name,new LinearLayout.LayoutParams(0,36,1f));
            line.addView(star,new LinearLayout.LayoutParams(32,36));
            textBox.addView(line,new LinearLayout.LayoutParams(-1,38));
            textBox.addView(number,new LinearLayout.LayoutParams(-1,24));
        }
    }
}
