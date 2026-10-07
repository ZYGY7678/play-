package com.example.contacts.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contacts.R;
import com.example.contacts.data.ContactModel;
import com.example.contacts.data.ContactTools;
import com.example.contacts.data.ContactsRepository;
import com.example.contacts.keys.KeyMapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.CodeDialog;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.FocusableRow;
import com.example.contacts.widget.ScreenHeader;
import com.example.contacts.widget.SoftKeyBar;

import java.util.ArrayList;

public class SettingsActivity extends BaseKeyActivity {
    private final ArrayList<String> labels = new ArrayList<String>();
    private final ArrayList<String> values = new ArrayList<String>();
    private ListView list;
    private int selected = 0;
    private static final int PICK_VCARD = 900;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        buildLabels();
        build();
    }

    private void add(String title, String value) { labels.add(title); values.add(value); }

    private void buildLabels() {
        labels.clear();
        values.clear();
        add("מקשי תפריט", AppPrefs.bool(this,"soft_compat",false) ? "MENU/BACK" : "SOFT LEFT/RIGHT");
        add("T9", AppPrefs.bool(this,"t9_hebrew",true) ? "עברית" : "English");
        add("מיון", AppPrefs.bool(this,"sort_family",false) ? "משפחה" : "שם");
        add("תצוגת שם", AppPrefs.bool(this,"family_first",false) ? "משפחה, פרטי" : "פרטי, משפחה");
        add("גודל גופן", AppPrefs.str(this,"font_size","רגיל"));
        add("ערכת צבעים", AppPrefs.str(this,"theme","כהה"));
        add("רטט", AppPrefs.bool(this,"vibrate",true) ? "פעיל" : "כבוי");
        add("צלילי ניווט", AppPrefs.bool(this,"sounds",true) ? "פעילים" : "כבויים");
        add("גלישה מעגלית", AppPrefs.bool(this,"circular",false) ? "פעילה" : "כבויה");
        add("Volume = עמוד", AppPrefs.bool(this,"volume_page",true) ? "כן" : "לא");
        add("אנימציות", AppPrefs.bool(this,"animations",true) ? "פעילות" : "כבויות");
        add("מצב ילדים / נעילה", AppPrefs.bool(this,"kids_lock",false) ? "מופעל" : "כבוי");
        for(int d=2;d<=9;d++) add("חיוג מהיר "+d, AppPrefs.str(this,"speed_"+d,"לא הוגדר"));
        add("גיבוי אנשי קשר ל-vCard", "");
        add("ייבוא vCard", "");
        add("איחוד כפילויות לפי מספר", "");
        add("OK במועדפים", AppPrefs.bool(this,"favorite_ok_detail",false) ? "פרטים" : "חיוג");
    }

    private TextView txt(String s,float sp) {
        TextView t=new TextView(this);
        t.setText(s);t.setTextSize(sp*Palette.scale(this));t.setTextColor(Palette.text(this));
        t.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);return t;
    }

    private void build() {
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Palette.bg(this));
        root.addView(new ScreenHeader(this,"הגדרות"),new LinearLayout.LayoutParams(-1,-2));
        list=new ListView(this);list.setDivider(null);list.setSelector(new ColorDrawable(Color.TRANSPARENT));list.setItemsCanFocus(true);list.setVerticalScrollBarEnabled(false);
        list.setPadding(Ui.dp(this,8),Ui.dp(this,6),Ui.dp(this,8),Ui.dp(this,6));list.setClipToPadding(false);
        list.setAdapter(new BaseAdapter(){
            public int getCount(){return labels.size();}public Object getItem(int p){return labels.get(p);}public long getItemId(int p){return p;}
            public View getView(int p,View v,ViewGroup parent){
                SettingsActivity c=SettingsActivity.this;
                FocusableRow r=new FocusableRow(c);r.setMinimumHeight(Ui.dp(c,58));
                TextView title=txt(labels.get(p),16);title.setSingleLine(true);title.setEllipsize(android.text.TextUtils.TruncateAt.END);
                r.addView(title,new LinearLayout.LayoutParams(0,-2,1));
                String val=values.get(p);
                TextView chip=Ui.center(c,val.length()>0?val:"›",val.length()>0?14*Palette.scale(c):20,Palette.accent(c));
                Ui.bold(chip);chip.setSingleLine(true);
                if(val.length()>0){chip.setBackground(Ui.rounded(ColorUtil.alpha(Palette.accent(c),38),0,0,14,c));chip.setPadding(Ui.dp(c,12),Ui.dp(c,4),Ui.dp(c,12),Ui.dp(c,4));}
                LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-2,-2);cp.setMargins(Ui.dp(c,8),0,0,0);
                r.addView(chip,cp);
                return r;
            }
        });
        root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        SoftKeyBar sk=new SoftKeyBar(this,"","שינוי","חזרה");sk.left.setVisibility(View.INVISIBLE);
        sk.center.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggle(selected);}});
        sk.right.setOnClickListener(new View.OnClickListener(){public void onClick(View v){finish();}});
        root.addView(sk,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);list.requestFocus();list.setSelection(selected);
    }

    private void toggle(int p) {
        if(p==0)AppPrefs.put(this,"soft_compat",!AppPrefs.bool(this,"soft_compat",false));
        else if(p==1)AppPrefs.put(this,"t9_hebrew",!AppPrefs.bool(this,"t9_hebrew",true));
        else if(p==2)AppPrefs.put(this,"sort_family",!AppPrefs.bool(this,"sort_family",false));
        else if(p==3)AppPrefs.put(this,"family_first",!AppPrefs.bool(this,"family_first",false));
        else if(p==4)AppPrefs.put(this,"font_size",next(AppPrefs.str(this,"font_size","רגיל"),new String[]{"רגיל","גדול","ענק"}));
        else if(p==5)AppPrefs.put(this,"theme",next(AppPrefs.str(this,"theme","כהה"),new String[]{"כהה","בהירה","ניגודיות גבוהה"}));
        else if(p==6)AppPrefs.put(this,"vibrate",!AppPrefs.bool(this,"vibrate",true));
        else if(p==7)AppPrefs.put(this,"sounds",!AppPrefs.bool(this,"sounds",true));
        else if(p==8)AppPrefs.put(this,"circular",!AppPrefs.bool(this,"circular",false));
        else if(p==9)AppPrefs.put(this,"volume_page",!AppPrefs.bool(this,"volume_page",true));
        else if(p==10)AppPrefs.put(this,"animations",!AppPrefs.bool(this,"animations",true));
        else if(p==11){if(AppPrefs.bool(this,"kids_lock",false)){CodeDialog.check(this,new CodeDialog.Done(){public void onDone(){AppPrefs.put(SettingsActivity.this,"kids_lock",false);buildLabels();build();}});return;}else{CodeDialog.setCode(this,new CodeDialog.Done(){public void onDone(){AppPrefs.put(SettingsActivity.this,"kids_lock",true);buildLabels();build();}});return;}}
        else if(p>=12&&p<=19){speedDial(p-10);return;}
        else if(p==20){backup();return;}
        else if(p==21){pickVcard();return;}
        else if(p==22){merge();}
        else if(p==23){AppPrefs.put(this,"favorite_ok_detail",!AppPrefs.bool(this,"favorite_ok_detail",false));}
        buildLabels();build();
    }

    private String next(String s,String[] a){for(int i=0;i<a.length;i++)if(a[i].equals(s))return a[(i+1)%a.length];return a[0];}

    private void speedDial(final int d) {
        new AsyncTask<Void,Void,ArrayList<ContactModel>>() {
            protected ArrayList<ContactModel> doInBackground(Void...v){
                ArrayList<ContactModel> out=new ArrayList<ContactModel>();android.database.Cursor x=null;long last=-1;
                try{x=ContactsRepository.phones(SettingsActivity.this);if(x!=null)while(x.moveToNext()&&out.size()<40){long id=x.getLong(1);if(id==last)continue;last=id;ContactModel m=new ContactModel(id,x.getString(2));m.phones.add(new ContactModel.PhoneNumber(x.getString(3),ContactsRepository.phoneType(x.getInt(4))));out.add(m);}}
                finally{if(x!=null)x.close();}return out;
            }
            protected void onPostExecute(final ArrayList<ContactModel> cs){if(cs.size()==0){Toast.makeText(SettingsActivity.this,"אין אנשי קשר",Toast.LENGTH_SHORT).show();return;}String[]n=new String[cs.size()];for(int i=0;i<n.length;i++)n[i]=cs.get(i).name+"  "+cs.get(i).primary();DialogUtil.actions(SettingsActivity.this,"חיוג מהיר "+d,n,new DialogUtil.Choice(){public void onChoice(int w){AppPrefs.put(SettingsActivity.this,"speed_"+d,cs.get(w).primary());buildLabels();build();}});}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void backup() {
        new AsyncTask<Void,Void,String>() {
            protected String doInBackground(Void...v){try{return ContactTools.exportVCard(SettingsActivity.this).getAbsolutePath();}catch(Exception e){return "";}}
            protected void onPostExecute(String p){Toast.makeText(SettingsActivity.this,p.length()>0?"נוצר גיבוי: "+p:"הגיבוי נכשל",Toast.LENGTH_LONG).show();}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void pickVcard() {
        Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK_VCARD);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==PICK_VCARD&&resultCode==RESULT_OK&&data!=null&&data.getData()!=null){
            final Uri u=data.getData();
            new AsyncTask<Void,Void,Integer>() {
                protected Integer doInBackground(Void...v){try{return ContactTools.importVCard(SettingsActivity.this,u);}catch(Exception e){return 0;}}
                protected void onPostExecute(Integer n){Toast.makeText(SettingsActivity.this,"יובאו "+n+" אנשי קשר",Toast.LENGTH_LONG).show();}
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private void merge() {
        new AsyncTask<Void,Void,Integer>() {
            protected Integer doInBackground(Void...v){return ContactTools.mergeDuplicates(SettingsActivity.this);}
            protected void onPostExecute(Integer n){Toast.makeText(SettingsActivity.this,n+" כפילויות אוחדו",Toast.LENGTH_LONG).show();}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override protected void onKeyAction(KeyMapper.Result r,boolean down) {
        if(down)return;
        switch(r.action){
            case UP:selected=Math.max(0,selected-1);break;
            case DOWN:selected=Math.min(labels.size()-1,selected+1);break;
            case SELECT:toggle(selected);return;
            case BACK:finish();return;
            case SOFT_RIGHT:finish();return;
            default:return;
        }
        list.setSelection(selected);list.requestFocus();
    }
}