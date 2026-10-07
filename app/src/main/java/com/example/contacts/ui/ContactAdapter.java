package com.example.contacts.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.contacts.R;
import com.example.contacts.data.ContactModel;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.Palette;
import com.example.contacts.util.PhotoCache;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.AvatarView;
import com.example.contacts.widget.FocusableRow;
import com.example.contacts.widget.IconView;

import java.util.List;

public class ContactAdapter extends BaseAdapter {
    public static class Row {
        public int kind;
        public String header;
        public ContactModel contact;
        public Row(String h){kind=0;header=h;}
        public Row(ContactModel c){kind=1;contact=c;}
    }

    private final Context c;
    private List<Row> rows;
    public int actionMode=0;

    public ContactAdapter(Context c,List<Row> rows){this.c=c;this.rows=rows;}
    public void setRows(List<Row> r){rows=r;notifyDataSetChanged();}
    public int getCount(){return rows==null?0:rows.size();}
    public Object getItem(int p){return rows.get(p);}
    public long getItemId(int p){return p;}
    public boolean isEnabled(int p){return rows.get(p).kind==1;}
    public int getViewTypeCount(){return 2;}
    public int getItemViewType(int p){return rows.get(p).kind;}

    public View getView(int p,View v,ViewGroup parent){
        Row x=rows.get(p);
        if(x.kind==0){
            TextView h=v instanceof TextView?(TextView)v:new TextView(c);
            h.setText(x.header);
            h.setTextSize(13*Palette.scale(c));
            h.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
            h.setTextColor(Palette.accent(c));
            h.setGravity(Gravity.CENTER_VERTICAL);
            h.setPadding(Ui.dp(c,16),Ui.dp(c,6),Ui.dp(c,16),Ui.dp(c,2));
            h.setMinimumHeight(Ui.dp(c,30));
            h.setFocusable(false);
            h.setClickable(false);
            return h;
        }

        FocusableRow r=v instanceof FocusableRow?(FocusableRow)v:new FocusableRow(c);
        Holder h=r.getTag() instanceof Holder?(Holder)r.getTag():new Holder(c);
        if(r.getTag()==null){
            r.setTag(h);
            int a=Ui.dim(c,R.dimen.avatar_row);
            r.addView(h.avatar,new LinearLayout.LayoutParams(a,a));
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1);
            bp.setMargins(Ui.dp(c,12),0,Ui.dp(c,8),0);
            r.addView(h.box,bp);
            r.addView(h.action,new LinearLayout.LayoutParams(Ui.dp(c,38),Ui.dp(c,38)));
        }

        ContactModel m=x.contact;
        String n=m.name.length()>0?m.name:"ללא שם";
        if(AppPrefs.bool(c,"family_first",false)){
            String[] parts=n.trim().split("\\s+");
            if(parts.length>1){
                String family=parts[parts.length-1];
                String given=n.substring(0, n.length()-family.length()).trim();
                n=family+" "+given;
            }
        }

        h.avatar.setTag(m.photoUri);
        h.avatar.setContentDescription("תמונה של "+n);
        h.avatar.setName(n);
        h.avatar.setImageBitmap(null);
        if(m.photoUri!=null&&m.photoUri.length()>0)PhotoCache.loadInto(c,m.photoUri,h.avatar,n);

        h.name.setText(n);
        h.name.setTextSize(19*Palette.scale(c));
        h.name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        h.name.setTextColor(Palette.text(c));
        h.name.setSingleLine(true);
        h.name.setEllipsize(android.text.TextUtils.TruncateAt.END);

        String number=m.primary().length()>0?PhoneFormatter.format(m.primary()):"אין מספר";
        h.num.setText(PhoneFormatter.ltr(number));
        h.num.setTextSize(14*Palette.scale(c));
        h.num.setTextColor(Palette.secondary(c));
        h.num.setSingleLine(true);
        h.num.setEllipsize(android.text.TextUtils.TruncateAt.END);

        h.star.setVisibility(m.favorite?View.VISIBLE:View.GONE);
        h.star.setText("★");
        h.star.setTextColor(Palette.YELLOW);
        h.star.setTextSize(16*Palette.scale(c));

        h.action.setType(actionMode==1?IconView.CALL:actionMode==2?IconView.SMS:actionMode==3?IconView.INFO:IconView.MENU);
        h.action.setContentDescription(actionMode==1?"חייג":actionMode==2?"שלח הודעה":actionMode==3?"פרטים":"אפשרויות");
        return r;
    }

    static class Holder{
        AvatarView avatar;
        TextView name,num,star;
        IconView action;
        LinearLayout box;

        Holder(Context c){
            avatar=new AvatarView(c);
            name=new TextView(c);
            num=new TextView(c);
            star=new TextView(c);
            action=new IconView(c);
            box=new LinearLayout(c);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout line=new LinearLayout(c);
            line.setGravity(Gravity.CENTER_VERTICAL);
            line.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            star.setPadding(Ui.dp(c,6),0,0,0);
            line.addView(star,new LinearLayout.LayoutParams(-2,-2));
            box.addView(line,new LinearLayout.LayoutParams(-1,-2));
            box.addView(num,new LinearLayout.LayoutParams(-1,-2));
        }
    }
}