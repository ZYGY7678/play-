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
import com.example.contacts.widget.FocusableRow;
import java.util.List;

public class CallLogAdapter extends BaseAdapter {
 private Context c; private List<CallLogRepository.Entry> d;
 public CallLogAdapter(Context c,List<CallLogRepository.Entry>d){this.c=c;this.d=d;}
 public void setData(List<CallLogRepository.Entry>d){this.d=d;notifyDataSetChanged();}
 public int getCount(){return d==null?0:d.size();}
 public CallLogRepository.Entry getItem(int p){return d.get(p);}
 public long getItemId(int p){return p;}
 public View getView(int p,View v,ViewGroup parent){
  FocusableRow r=v instanceof FocusableRow?(FocusableRow)v:new FocusableRow(c);
  Holder h=r.getTag() instanceof Holder?(Holder)r.getTag():new Holder(c);
  if(r.getTag()==null){r.setTag(h);r.addView(h.icon,new LinearLayout.LayoutParams(48,-1));r.addView(h.box,new LinearLayout.LayoutParams(0,-1,1));r.addView(h.time,new LinearLayout.LayoutParams(105,-1));}
  CallLogRepository.Entry e=d.get(p);
  h.icon.setText(e.type==CallLog.Calls.MISSED_TYPE?"●":e.type==CallLog.Calls.INCOMING_TYPE?"←":"→");h.icon.setTextSize(24);h.icon.setGravity(Gravity.CENTER);
  h.icon.setTextColor(e.type==CallLog.Calls.MISSED_TYPE?Palette.RED:Palette.accent(c));
  h.name.setText(e.name.length()>0?e.name:"לא מזוהה");h.name.setTextSize(18*Palette.scale(c));h.name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.name.setTextColor(Palette.text(c));
  h.num.setText((e.count>1?e.count+" × ":"")+PhoneFormatter.ltr(PhoneFormatter.format(e.number)));h.num.setTextSize(14*Palette.scale(c));h.num.setTextColor(Palette.secondary(c));
  h.time.setText(TimeUtil.relative(e.date));h.time.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);h.time.setTextColor(Palette.secondary(c));return r;
 }
 static class Holder{TextView icon,name,num,time;LinearLayout box;Holder(Context c){icon=new TextView(c);name=new TextView(c);num=new TextView(c);time=new TextView(c);box=new LinearLayout(c);box.setOrientation(LinearLayout.VERTICAL);box.addView(name);box.addView(num);}}
}