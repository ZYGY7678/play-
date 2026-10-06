package com.example.contacts.ui;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.example.contacts.R;
import com.example.contacts.util.Palette;

public final class DialogUtil{
 private DialogUtil(){}
 public interface Choice{void onChoice(int which);}
 public static Dialog actions(final Context c,final String title,final String[] items,final Choice cb){
  final Dialog d=new Dialog(c);
  LinearLayout root=new LinearLayout(c);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(10,8,10,8);root.setBackgroundColor(Palette.bar(c));
  TextView h=new TextView(c);h.setText(title);h.setTextSize(21*Palette.scale(c));h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setTextColor(Palette.text(c));h.setGravity(Gravity.CENTER);root.addView(h,new LinearLayout.LayoutParams(-1,56));
  final ListView list=new ListView(c);list.setDivider(null);list.setSelector(R.drawable.row_selector);list.setItemsCanFocus(true);
  list.setAdapter(new BaseAdapter(){
   public int getCount(){return items.length;}public Object getItem(int p){return items[p];}public long getItemId(int p){return p;}
   public View getView(final int p,View v,ViewGroup par){
    TextView t=new TextView(c);t.setText((p+1)+"    "+items[p]);t.setTextSize(18*Palette.scale(c));t.setTextColor(Palette.text(c));t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(18,0,18,0);t.setFocusable(true);t.setBackgroundResource(R.drawable.row_selector);
    t.setOnClickListener(new View.OnClickListener(){public void onClick(View v){d.dismiss();cb.onChoice(p);}});return t;
   }
  });
  root.addView(list,new LinearLayout.LayoutParams(-1,0,1));d.setContentView(root);d.show();list.requestFocus();list.setSelection(0);
  list.setOnKeyListener(new View.OnKeyListener(){public boolean onKey(View v,int key,KeyEvent e){
   if(e.getAction()==KeyEvent.ACTION_UP&&key>=KeyEvent.KEYCODE_1&&key<=KeyEvent.KEYCODE_9){int i=key-KeyEvent.KEYCODE_1;if(i<items.length){d.dismiss();cb.onChoice(i);return true;}}
   if(e.getAction()==KeyEvent.ACTION_UP&&(key==KeyEvent.KEYCODE_BACK||key==KeyEvent.KEYCODE_ENDCALL)){d.dismiss();return true;}return false;
  }});return d;
 }
}