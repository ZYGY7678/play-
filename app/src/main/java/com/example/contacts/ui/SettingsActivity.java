package com.example.contacts.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contacts.R;
import com.example.contacts.data.ContactModel;
import com.example.contacts.data.ContactsRepository;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.FocusableRow;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends Activity {
    private final ArrayList<String> labels=new ArrayList<String>();
    private ListView list;
    private SharedPreferences p;
    private int selected=0;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);
        p=getSharedPreferences("keycontacts",0);buildLabels();build();
    }

    private void buildLabels(){
        labels.clear();
        labels.add("מיפוי מקשי תפריט: "+(p.getBoolean("softCompat",false)?"MENU/BACK":"SOFT LEFT/RIGHT"));
        labels.add("שפת T9: "+(p.getBoolean("t9_hebrew",true)?"עברית":"English"));
        labels.add("מיון: "+(p.getBoolean("sort_family",false)?"משפחה":"שם פרטי"));
        labels.add("תצוגת שם: "+(p.getBoolean("family_first",false)?"משפחה, פרטי":"פרטי, משפחה"));
        labels.add("גודל גופן: "+p.getString("font_size","רגיל"));
        labels.add("ערכת צבעים: "+p.getString("theme","כהה"));
        labels.add("רטט: "+(p.getBoolean("vibrate",true)?"פעיל":"כבוי"));
        labels.add("צלילי ניווט: "+(p.getBoolean("sounds",true)?"פעיל":"כבוי"));
        labels.add("גלישה מעגלית: "+(p.getBoolean("circular",false)?"פעילה":"כבויה"));
        for(int d=2;d<=9;d++)labels.add("חיוג מהיר "+d+": "+speed(d));
    }

    private String speed(int d){
        String n=p.getString("speed_"+d,"");return n.length()==0?"לא הוגדר":n;
    }

    private void build(){
        android.widget.LinearLayout root=new android.widget.LinearLayout(this);root.setOrientation(android.widget.LinearLayout.VERTICAL);root.setBackgroundColor(ColorUtil.BG);
        root.addView(Ui.bar(this,"הגדרות",22),new android.widget.LinearLayout.LayoutParams(-1,54));
        list=new ListView(this);list.setDivider(null);list.setSelector(R.drawable.row_selector);list.setAdapter(new BaseAdapter(){
            public int getCount(){return labels.size();}
            public Object getItem(int pos){return labels.get(pos);}
            public long getItemId(int pos){return pos;}
            public View getView(int pos,View v,ViewGroup par){
                FocusableRow r=new FocusableRow(SettingsActivity.this);TextView t=Ui.text(SettingsActivity.this,(pos+1)+"  "+labels.get(pos),18,ColorUtil.TEXT);r.addView(t,new android.widget.LinearLayout.LayoutParams(-1,72));return r;
            }
        });
        root.addView(list,new android.widget.LinearLayout.LayoutParams(-1,0,1f));setContentView(root);list.requestFocus();list.setSelection(0);
    }

    private void cycle(int pos){
        android.content.SharedPreferences.Editor e=p.edit();
        if(pos==0)e.putBoolean("softCompat",!p.getBoolean("softCompat",false));
        else if(pos==1)e.putBoolean("t9_hebrew",!p.getBoolean("t9_hebrew",true));
        else if(pos==2)e.putBoolean("sort_family",!p.getBoolean("sort_family",false));
        else if(pos==3)e.putBoolean("family_first",!p.getBoolean("family_first",false));
        else if(pos==4)e.putString("font_size",next(p.getString("font_size","רגיל"),new String[]{"רגיל","גדול","ענק"}));
        else if(pos==5)e.putString("theme",next(p.getString("theme","כהה"),new String[]{"כהה","בהירה","ניגודיות גבוהה"}));
        else if(pos==6)e.putBoolean("vibrate",!p.getBoolean("vibrate",true));
        else if(pos==7)e.putBoolean("sounds",!p.getBoolean("sounds",true));
        else if(pos==8)e.putBoolean("circular",!p.getBoolean("circular",false));
        else if(pos>=9){final int digit=pos-7;chooseSpeedDial(digit);return;}
        e.apply();buildLabels();list.setAdapter(list.getAdapter());list.invalidateViews();Toast.makeText(this,"עודכן",Toast.LENGTH_SHORT).show();
    }

    private String next(String cur,String[] a){for(int i=0;i<a.length;i++)if(a[i].equals(cur))return a[(i+1)%a.length];return a[0];}

    private void chooseSpeedDial(final int digit){
        final ArrayList<ContactModel> cs=new ArrayList<ContactModel>();
        android.database.Cursor c=null;
        try{
            c=ContactsRepository.createPhoneCursor(this);
            if(c!=null){long last=-1;while(c.moveToNext()&&cs.size()<9){long id=c.getLong(1);if(id==last)continue;last=id;cs.add(new ContactModel(id,-1,c.getString(2),c.getString(3),"",c.getString(5),c.getInt(6)!=0));}}
        }finally{if(c!=null)c.close();}
        if(cs.size()==0){Toast.makeText(this,"אין אנשי קשר",Toast.LENGTH_SHORT).show();return;}
        String[] names=new String[cs.size()];for(int i=0;i<cs.size();i++)names[i]=cs.get(i).name+"  "+cs.get(i).number;
        DialogUtil.actions(this,"חיוג מהיר "+digit,names,new DialogUtil.Choice(){public void onChoice(int w){p.edit().putString("speed_"+digit,cs.get(w).number).apply();buildLabels();list.invalidateViews();Toast.makeText(SettingsActivity.this,"הוגדר",Toast.LENGTH_SHORT).show();}});
    }

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        int k=e.getKeyCode();if(e.getAction()==KeyEvent.ACTION_DOWN){e.startTracking();return relevant(k);}
        if(e.getAction()==KeyEvent.ACTION_UP&&relevant(k)){
            if(k==KeyEvent.KEYCODE_DPAD_UP)selected=Math.max(0,selected-1);
            else if(k==KeyEvent.KEYCODE_DPAD_DOWN)selected=Math.min(labels.size()-1,selected+1);
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)cycle(selected);
            else if(k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_ENDCALL)finish();
            if(list!=null){list.setSelection(selected);list.invalidateViews();}return true;
        }
        return relevant(k);
    }
    private boolean relevant(int k){return k==KeyEvent.KEYCODE_DPAD_UP||k==KeyEvent.KEYCODE_DPAD_DOWN||k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER||k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_ENDCALL;}
}
