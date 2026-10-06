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
import com.example.contacts.util.Palette;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.widget.FocusableRow;
import java.util.List;

public class ContactAdapter extends BaseAdapter {
 public static class Row {
  public int kind; public String header; public ContactModel contact;
  public Row(String h){kind=0;header=h;}
  public Row(ContactModel c){kind=1;contact=c;}
 }
 private Context c; private List<Row> rows; public int actionMode=0;
 public ContactAdapter(Context c,List<Row> rows){this.c=c;this.rows=rows;}
 public void setRows(List<Row> r){rows=r;notifyDataSetChanged();}
 public int getCount(){return rows==null?0:rows.size();}
 public Object getItem(int p){return rows.get(p);}
 public long getItemId(int p){return p;}
 public boolean isEnabled(int p){return rows.get(p).kind==1;}
 public View getView(int p,View v,ViewGroup parent){
  Row x=rows.get(p);
  if(x.kind==0){
   TextView h=new TextView(c); h.setText(x.header); h.setTextSize(14); h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
   h.setTextColor(Palette.accent(c)); h.setGravity(Gravity.CENTER_VERTICAL); h.setPadding(18,0,18,0);
   h.setFocusable(false); h.setClickable(false); return h;
  }
  FocusableRow r=v instanceof FocusableRow?(FocusableRow)v:new FocusableRow(c);
  Holder h=r.getTag() instanceof Holder?(Holder)r.getTag():new Holder(c);
  if(r.getTag()==null){r.setTag(h);r.addView(h.avatar,new LinearLayout.LayoutParams(52,52));r.addView(h.box,new LinearLayout.LayoutParams(0,-1,1));r.addView(h.action,new LinearLayout.LayoutParams(50,54));}
  ContactModel m=x.contact;String n=m.name.length()>0?m.name:"ללא שם";
  h.avatar.setText(n.substring(0,1));h.avatar.setTextSize(20*Palette.scale(c));h.avatar.setGravity(Gravity.CENTER);h.avatar.setTextColor(Palette.text(c));h.avatar.setBackgroundColor(0xff3b6178);
  h.name.setText(n);h.name.setTextSize(20*Palette.scale(c));h.name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.name.setTextColor(Palette.text(c));
  h.num.setText(PhoneFormatter.ltr(m.primary().length()>0?PhoneFormatter.format(m.primary()):"אין מספר"));h.num.setTextSize(15*Palette.scale(c));h.num.setTextColor(Palette.secondary(c));
  h.star.setText(m.favorite?"★":"");h.star.setTextColor(Palette.YELLOW);h.star.setTextSize(20);
  h.action.setText(actionMode==1?"☎":actionMode==2?"✉":actionMode==3?"ⓘ":"⋮");h.action.setTextSize(24);h.action.setGravity(Gravity.CENTER);
  h.action.setTextColor(actionMode==1?Palette.GREEN:Palette.accent(c));
  h.action.setContentDescription(actionMode==1?"חייג":actionMode==2?"שלח הודעה":actionMode==3?"פרטים":"אפשרויות");
  return r;
 }
 static class Holder{
  TextView avatar,name,num,star,action; LinearLayout box;
  Holder(Context c){avatar=new TextView(c);name=new TextView(c);num=new TextView(c);star=new TextView(c);action=new TextView(c);box=new LinearLayout(c);box.setOrientation(LinearLayout.VERTICAL);
   LinearLayout line=new LinearLayout(c);line.setGravity(Gravity.CENTER_VERTICAL);line.addView(name,new LinearLayout.LayoutParams(0,38,1));line.addView(star,new LinearLayout.LayoutParams(34,38));box.addView(line,new LinearLayout.LayoutParams(-1,40));box.addView(num,new LinearLayout.LayoutParams(-1,25));
  }
 }
}