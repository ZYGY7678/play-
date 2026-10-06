package com.example.contacts.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.provider.CallLog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.contacts.data.CallLogRepository;
import com.example.contacts.util.Palette;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.TimeUtil;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.FocusableRow;
import com.example.contacts.widget.IconView;

import java.util.List;

public class CallLogAdapter extends BaseAdapter {
    private final Context c;
    private List<CallLogRepository.Entry> d;
    public CallLogAdapter(Context c,List<CallLogRepository.Entry>d){this.c=c;this.d=d;}
    public void setData(List<CallLogRepository.Entry>d){this.d=d;notifyDataSetChanged();}
    public int getCount(){return d==null?0:d.size();}
    public CallLogRepository.Entry getItem(int p){return d.get(p);}
    public long getItemId(int p){return p;}

    public View getView(int p,View v,ViewGroup parent){
        FocusableRow r=v instanceof FocusableRow?(FocusableRow)v:new FocusableRow(c);
        Holder h=r.getTag() instanceof Holder?(Holder)r.getTag():new Holder(c);
        if(r.getTag()==null){r.setTag(h);r.addView(h.icon,new LinearLayout.LayoutParams(Ui.dp(c,42),-1));r.addView(h.box,new LinearLayout.LayoutParams(0,-1,1));r.addView(h.time,new LinearLayout.LayoutParams(Ui.dp(c,95),-1));}
        CallLogRepository.Entry e=d.get(p);
        int icon=e.type==CallLog.Calls.MISSED_TYPE?IconView.MISSED:(e.type==CallLog.Calls.INCOMING_TYPE?IconView.IN:IconView.OUT);
        h.icon.setType(icon);
        h.name.setText(e.name.length()>0?e.name:"לא מזוהה");
        h.name.setTextSize(18*Palette.scale(c));h.name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.name.setTextColor(Palette.text(c));h.name.setSingleLine(true);h.name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        h.num.setText((e.count>1?e.count+" × ":"")+PhoneFormatter.ltr(PhoneFormatter.format(e.number)));
        h.num.setTextSize(14*Palette.scale(c));h.num.setTextColor(Palette.secondary(c));h.num.setSingleLine(true);
        h.time.setText(TimeUtil.relative(e.date));h.time.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);h.time.setTextColor(Palette.secondary(c));h.time.setTextSize(13);
        return r;
    }
    static class Holder{
        IconView icon;TextView name,num,time;LinearLayout box;
        Holder(Context c){icon=new IconView(c);name=new TextView(c);num=new TextView(c);time=new TextView(c);box=new LinearLayout(c);box.setOrientation(LinearLayout.VERTICAL);box.addView(name,new LinearLayout.LayoutParams(-1,Ui.dp(c,40)));box.addView(num,new LinearLayout.LayoutParams(-1,Ui.dp(c,25)));}
    }
}