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
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.Palette;
import com.example.contacts.util.PhotoCache;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.AvatarView;
import com.example.contacts.widget.FocusableRow;

import java.util.List;

public class FavoriteAdapter extends BaseAdapter {
    private final Context c;
    private List<ContactModel> d;
    public FavoriteAdapter(Context c,List<ContactModel>d){this.c=c;this.d=d;}
    public void setData(List<ContactModel>d){this.d=d;notifyDataSetChanged();}
    public int getCount(){return d==null?0:d.size();}
    public ContactModel getItem(int p){return d.get(p);}
    public long getItemId(int p){return d.get(p).id;}

    private int assignedDigit(String number){
        for(int n=2;n<=9;n++) if(PhoneFormatter.same(number,AppPrefs.str(c,"speed_"+n,""))) return n;
        return 0;
    }

    public View getView(int p,View v,ViewGroup parent){
        FocusableRow r=v instanceof FocusableRow?(FocusableRow)v:new FocusableRow(c);
        r.setOrientation(LinearLayout.VERTICAL);r.setGravity(Gravity.CENTER);
        Holder h=r.getTag() instanceof Holder?(Holder)r.getTag():new Holder(c);
        if(r.getTag()==null){
            r.setTag(h);
            r.setPadding(Ui.dp(c,6),Ui.dp(c,10),Ui.dp(c,6),Ui.dp(c,10));
            LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(Ui.dp(c,64),Ui.dp(c,64));
            ap.gravity=Gravity.CENTER_HORIZONTAL;
            r.addView(h.avatar,ap);
            r.addView(h.name,new LinearLayout.LayoutParams(-1,-2));
            r.addView(h.num,new LinearLayout.LayoutParams(-1,-2));
        }

        ContactModel m=d.get(p);String n=m.name.length()>0?m.name:"ללא שם";
        h.avatar.setTag(m.photoUri);h.avatar.setContentDescription("תמונה של "+n);
        h.avatar.setName(n);
        h.avatar.setImageBitmap(null);
        if(m.photoUri!=null&&m.photoUri.length()>0)PhotoCache.loadInto(c,m.photoUri,h.avatar,n);

        int digit=assignedDigit(m.primary());
        h.name.setText((digit>0?digit+"  ":"")+n);
        h.name.setTextSize(16*Palette.scale(c));h.name.setGravity(Gravity.CENTER);
        h.name.setTextColor(Palette.text(c));h.name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        h.name.setSingleLine(true);h.name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        h.name.setPadding(0,Ui.dp(c,6),0,0);

        h.num.setText(PhoneFormatter.ltr(PhoneFormatter.format(m.primary())));
        h.num.setTextSize(12*Palette.scale(c));h.num.setGravity(Gravity.CENTER);
        h.num.setTextColor(Palette.secondary(c));h.num.setSingleLine(true);
        return r;
    }

    static class Holder{
        AvatarView avatar;TextView name,num;
        Holder(Context c){avatar=new AvatarView(c);name=new TextView(c);num=new TextView(c);}
    }
}