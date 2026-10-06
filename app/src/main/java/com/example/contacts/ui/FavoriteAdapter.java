package com.example.contacts.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.contacts.data.ContactModel;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.Palette;
import com.example.contacts.util.PhotoCache;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
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
        if(r.getTag()==null){r.setTag(h);r.addView(h.avatar,new LinearLayout.LayoutParams(-1,Ui.dp(c,72)));r.addView(h.name,new LinearLayout.LayoutParams(-1,Ui.dp(c,48)));}

        ContactModel m=d.get(p);String n=m.name.length()>0?m.name:"ללא שם";
        h.avatar.setTag(m.photoUri);h.avatar.setContentDescription("תמונה של "+n);
        h.avatar.setImageBitmap(null);h.avatar.setBackground(PhotoCache.circle(0xff3b6178));
        if(m.photoUri!=null&&m.photoUri.length()>0)PhotoCache.loadInto(c,m.photoUri,h.avatar,n);

        int digit=assignedDigit(m.primary());
        h.name.setText((digit>0?digit+"   ":"")+n+"\n"+PhoneFormatter.ltr(PhoneFormatter.format(m.primary())));
        h.name.setTextSize(16*Palette.scale(c));h.name.setGravity(Gravity.CENTER);h.name.setTextColor(Palette.text(c));h.name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return r;
    }

    static class Holder{
        ImageView avatar;TextView name;
        Holder(Context c){avatar=new ImageView(c);avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);name=new TextView(c);}
    }
}