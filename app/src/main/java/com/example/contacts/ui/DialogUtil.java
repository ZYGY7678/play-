package com.example.contacts.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.view.ViewGroup;

import com.example.contacts.util.ColorUtil;

public final class DialogUtil {
    private DialogUtil() {}

    public interface Choice { void onChoice(int which); }

    public static void actions(Context c, final String title, final String[] items, final Choice cb) {
        final Dialog d=new Dialog(c);
        d.getWindow();
        LinearLayout root=new LinearLayout(c);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(18,12,18,12);root.setBackgroundColor(ColorUtil.BAR);
        TextView h=new TextView(c);h.setText(title);h.setTextColor(Color.WHITE);h.setTextSize(20);h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);h.setGravity(Gravity.CENTER_VERTICAL);root.addView(h,new LinearLayout.LayoutParams(-1,50));
        ListView list=new ListView(c);list.setDivider(null);list.setChoiceMode(ListView.CHOICE_MODE_NONE);list.setSelector(com.example.contacts.R.drawable.row_selector);
        list.setAdapter(new BaseAdapter(){
            public int getCount(){return items.length;}
            public Object getItem(int p){return items[p];}
            public long getItemId(int p){return p;}
            public View getView(final int p,View v,ViewGroup par){
                TextView t=new TextView(c);t.setText((p+1)+"  "+items[p]);t.setTextSize(18);t.setTextColor(ColorUtil.TEXT);t.setGravity(Gravity.CENTER_VERTICAL);t.setPadding(14,0,14,0);t.setFocusable(true);t.setFocusableInTouchMode(false);t.setBackgroundResource(com.example.contacts.R.drawable.row_selector);
                t.setOnClickListener(new View.OnClickListener(){public void onClick(View v){d.dismiss();cb.onChoice(p);}});
                return t;
            }
        });
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        d.setContentView(root);
        if(d.getWindow()!=null)d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.show();
        list.requestFocus();
        list.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v,int key,KeyEvent e){
                if(e.getAction()==KeyEvent.ACTION_UP && key>=KeyEvent.KEYCODE_1 && key<=KeyEvent.KEYCODE_9){
                    int i=key-KeyEvent.KEYCODE_1;if(i<items.length){d.dismiss();cb.onChoice(i);return true;}
                }
                if(e.getAction()==KeyEvent.ACTION_UP && (key==KeyEvent.KEYCODE_BACK||key==KeyEvent.KEYCODE_ENDCALL)){d.dismiss();return true;}
                return false;
            }
        });
    }
}
