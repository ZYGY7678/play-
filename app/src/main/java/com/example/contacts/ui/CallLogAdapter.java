package com.example.contacts.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.provider.CallLog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.contacts.data.CallLogRepository;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.TimeUtil;
import com.example.contacts.widget.FocusableRow;

import java.util.List;

public class CallLogAdapter extends BaseAdapter {
    private final Context context;
    private List<CallLogRepository.Entry> data;
    public CallLogAdapter(Context c,List<CallLogRepository.Entry>d){context=c;data=d;}
    public void setData(List<CallLogRepository.Entry>d){data=d;notifyDataSetChanged();}
    public CallLogRepository.Entry getItem(int p){return data.get(p);}
    public int getCount(){return data==null?0:data.size();}
    public long getItemId(int p){return p;}
    public View getView(int p,View v,ViewGroup parent){
        Holder h;
        FocusableRow row;
        if(v instanceof FocusableRow){row=(FocusableRow)v;h=(Holder)row.getTag();}
        else{row=new FocusableRow(context);h=new Holder(context);row.setTag(h);row.addView(h.icon,new LinearLayout.LayoutParams(44,-1));row.addView(h.text,new LinearLayout.LayoutParams(0,-2,1f));row.addView(h.time,new LinearLayout.LayoutParams(100,-1));}
        CallLogRepository.Entry e=data.get(p);
        String symbol=e.type==CallLog.Calls.MISSED_TYPE?"●":(e.type==CallLog.Calls.INCOMING_TYPE?"←":"→");
        h.icon.setText(symbol);
        h.icon.setTextSize(25);
        h.icon.setGravity(Gravity.CENTER);
        h.icon.setTextColor(e.type==CallLog.Calls.MISSED_TYPE?ColorUtil.RED:ColorUtil.ACCENT);
        h.name.setText(e.name.length()==0?PhoneFormatter.format(e.number):e.name);
        h.name.setTextSize(18);h.name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.name.setTextColor(ColorUtil.TEXT);
        h.num.setText((e.count>1?e.count+" ×  ":"")+PhoneFormatter.format(e.number));
        h.num.setTextSize(14);h.num.setTextColor(ColorUtil.SECONDARY);
        h.time.setText(TimeUtil.relative(e.date));h.time.setTextSize(13);h.time.setTextColor(ColorUtil.SECONDARY);h.time.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
        return row;
    }
    static class Holder{
        TextView icon,name,num,time; LinearLayout text;
        Holder(Context c){icon=new TextView(c); text=new LinearLayout(c); text.setOrientation(LinearLayout.VERTICAL); name=new TextView(c); num=new TextView(c); text.addView(name); text.addView(num); time=new TextView(c);}
    }
}
