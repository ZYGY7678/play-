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

public class FavoriteAdapter extends BaseAdapter {
 private Context c; private List<ContactModel> d;
 public FavoriteAdapter(Context c,List<ContactModel>d){this.c=c;this.d=d;}
 public void setData(List<ContactModel>d){this.d=d;notifyDataSetChanged();}
 public int getCount(){return d==null?0:d.size();}
 public ContactModel getItem(int p){return d.get(p);}
 public long getItemId(int p){return d.get(p).id;}
 public View getView(int p,View v,ViewGroup parent){
  FocusableRow r=v instanceof FocusableRow?(FocusableRow)v:new FocusableRow(c);r.setOrientation(LinearLayout.VERTICAL);r.setGravity(Gravity.CENTER);
  if(r.getChildCount()==0){TextView a=new TextView(c),n=new TextView(c);a.setTag("avatar");n.setTag("name");r.addView(a,new LinearLayout.LayoutParams(-1,72));r.addView(n,new LinearLayout.LayoutParams(-1,50));}
  TextView a=(TextView)r.findViewWithTag("avatar"),n=(TextView)r.findViewWithTag("name");ContactModel m=d.get(p);String name=m.name.length()>0?m.name:"ללא שם";
  a.setText(name.substring(0,1));a.setTextSize(28);a.setGravity(Gravity.CENTER);a.setTextColor(Palette.text(c));a.setBackgroundColor(0xff3b6178);
  n.setText((p<8?(p+2)+"  ":"")+name+"\n"+PhoneFormatter.ltr(PhoneFormatter.format(m.primary())));n.setTextSize(16*Palette.scale(c));n.setTextColor(Palette.text(c));n.setGravity(Gravity.CENTER);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
  return r;
 }
}