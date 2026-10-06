package com.example.contacts.ui;

import android.content.Context;
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

public class FavoriteAdapter extends BaseAdapter {
    private final Context context; private List<ContactModel> data;
    public FavoriteAdapter(Context c,List<ContactModel>d){context=c;data=d;}
    public void setData(List<ContactModel>d){data=d;notifyDataSetChanged();}
    public int getCount(){return data==null?0:data.size();}
    public ContactModel getItem(int p){return data.get(p);}
    public long getItemId(int p){return data.get(p).id;}
    public View getView(int p,View v,ViewGroup parent){
        FocusableRow row=(v instanceof FocusableRow)?(FocusableRow)v:new FocusableRow(context);
        if(row.getChildCount()==0){
            TextView a=new TextView(context);a.setTag("avatar");
            TextView n=new TextView(context);n.setTag("name");n.setTextSize(17);n.setTextColor(ColorUtil.TEXT);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);n.setGravity(Gravity.CENTER);
            row.setGravity(Gravity.CENTER);row.setOrientation(LinearLayout.VERTICAL);row.addView(a,new LinearLayout.LayoutParams(-1,58));row.addView(n,new LinearLayout.LayoutParams(-1,34));
        }
        TextView a=(TextView)row.findViewWithTag("avatar"),n=(TextView)row.findViewWithTag("name");
        ContactModel c=data.get(p);String name=c.name.length()==0?"ללא שם":c.name;
        a.setText(name.substring(0,1));a.setTextSize(25);a.setGravity(Gravity.CENTER);a.setTextColor(Color.WHITE);a.setBackgroundColor(ColorUtil.stableColor(name));
        n.setText(name+"\n"+PhoneFormatter.format(c.number));return row;
    }
}
