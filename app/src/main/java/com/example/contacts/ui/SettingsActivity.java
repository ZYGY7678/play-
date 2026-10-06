package com.example.contacts.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.contacts.R;
import com.example.contacts.data.ContactModel;
import com.example.contacts.data.ContactsRepository;
import com.example.contacts.keys.KeyMapper;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.Palette;
import com.example.contacts.widget.FocusableRow;

import java.util.ArrayList;

public class SettingsActivity extends BaseKeyActivity {
 private final ArrayList<String> labels=new ArrayList<String>();private ListView list;private int selected=0;
 @Override protected void onCreate(Bundle b){super.onCreate(b);buildLabels();build();}
 private void buildLabels(){
  labels.clear();
  labels.add("מקשי תפריט: "+(AppPrefs.bool(this,"soft_compat",false)?"MENU/BACK":"SOFT LEFT/RIGHT"));
  labels.add("T9: "+(AppPrefs.bool(this,"t9_hebrew",true)?"עברית":"English"));
  labels.add("מיון: "+(AppPrefs.bool(this,"sort_family",false)?"משפחה":"שם"));
  labels.add("תצוגת שם: "+(AppPrefs.bool(this,"family_first",false)?"משפחה, פרטי":"פרטי, משפחה"));
  labels.add("גודל גופן: "+AppPrefs.str(this,"font_size","רגיל"));
  labels.add("ערכת צבעים: "+AppPrefs.str(this,"theme","כהה"));
  labels.add("רטט: "+(AppPrefs.bool(this,"vibrate",true)?"פעיל":"כבוי"));
  labels.add("צלילי ניווט: "+(AppPrefs.bool(this,"sounds",true)?"פעיל":"כבוי"));
  labels.add("גלישה מעגלית: "+(AppPrefs.bool(this,"circular",false)?"פעילה":"כבויה"));
  labels.add("Volume = עמוד: "+(AppPrefs.bool(this,"volume_page",true)?"כן":"לא"));
  labels.add("אנימציות: "+(AppPrefs.bool(this,"animations",true)?"פעילות":"כבויות"));
  labels.add("מצב ילדים / נעילה: "+(AppPrefs.bool(this,"kids_lock",false)?"מופעל":"כבוי"));
  for(int d=2;d<=9;d++)labels.add("חיוג מהיר "+d+": "+AppPrefs.str(this,"speed_"+d,"לא הוגדר"));
  labels.add("גיבוי אנשי קשר ל-vCard");
  labels.add("ייבוא vCard");
  labels.add("איחוד כפילויות לפי מספר");
 }
 private void build(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Palette.bg(this));
  TextView title=txt("הגדרות",23,Palette.text(this));title.setGravity(Gravity.CENTER);root.addView(title,new LinearLayout.LayoutParams(-1,56));
  list=new ListView(this);list.setDivider(null);list.setSelector(R.drawable.row_selector);list.setItemsCanFocus(true);
  list.setAdapter(new BaseAdapter(){public int getCount(){return labels.size();}public Object getItem(int p){return labels.get(p);}public long getItemId(int p){return p;}
   public View getView(int p,View v,ViewGroup parent){FocusableRow r=new FocusableRow(SettingsActivity.this);TextView t=txt((p+1)+"   "+labels.get(p),17,Palette.text(SettingsActivity.this));t.setPadding(12,0,12,0);r.addView(t,new LinearLayout.LayoutParams(-1,72));return r;}});
  root.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);list.requestFocus();list.setSelection(0);
 }
 private TextView txt(String s,float sp,int col){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp*Palette.scale(this));t.setTextColor(col);t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);return t;}
 private void toggle(int p){
  switch(p){
   case 0:AppPrefs.put(this,"soft_compat",!AppPrefs.bool(this,"soft_compat",false));break;
   case 1:AppPrefs.put(this,"t9_hebrew",!AppPrefs.bool(this,"t9_hebrew",true));break;
   case 2:AppPrefs.put(this,"sort_family",!AppPrefs.bool(this,"sort_family",false));break;
   case 3:AppPrefs.put(this,"family_first",!AppPrefs.bool(this,"family_first",false));break;
   case 4:AppPrefs.put(this,"font_size",next(AppPrefs.str(this,"font_size","רגיל"),new String[]{"רגיל","גדול","ענק"}));break;
   case 5:AppPrefs.put(this,"theme",next(AppPrefs.str(this,"theme","כהה"),new String[]{"כהה","בהירה","ניגודיות גבוהה"}));break;
   case 6:AppPrefs.put(this,"vibrate",!AppPrefs.bool(this,"vibrate",true));break;
   case 7:AppPrefs.put(this,"sounds",!AppPrefs.bool(this,"sounds",true));break;
   case 8:AppPrefs.put(this,"circular",!AppPrefs.bool(this,"circular",false));break;
   case 9:AppPrefs.put(this,"volume_page",!AppPrefs.bool(this,"volume_page",true));break;
   case 10:AppPrefs.put(this,"animations",!AppPrefs.bool(this,"animations",true));break;
   case 11:AppPrefs.put(this,"kids_lock",!AppPrefs.bool(this,"kids_lock",false));break;
   default:
    if(p>=12&&p<=19){speedDial(p-10);return;}
    if(p==20)Toast.makeText(this,"גיבוי vCard: נבנה בהמשך לפי אחסון ה-ROM",Toast.LENGTH_LONG).show();
    else if(p==21)Toast.makeText(this,"ייבוא vCard: בחר קובץ דרך מנהל קבצים",Toast.LENGTH_LONG).show();
    else if(p==22)Toast.makeText(this,"איחוד כפילויות לפי מספר: בחירת כפילויות תופיע בגרסה המלאה",Toast.LENGTH_LONG).show();
  }
  buildLabels();list.setAdapter(list.getAdapter());list.setSelection(selected);list.invalidateViews();
 }
 private String next(String s,String[]a){for(int i=0;i<a.length;i++)if(a[i].equals(s))return a[(i+1)%a.length];return a[0];}
 private void speedDial(final int d){
  CursorHolder.load(this,new CursorHolder.Done(){public void done(final ArrayList<ContactModel> cs){
   if(cs.size()==0){Toast.makeText(SettingsActivity.this,"אין אנשי קשר",Toast.LENGTH_SHORT).show();return;}
   String[]n=new String[cs.size()];for(int i=0;i<n.length;i++)n[i]=cs.get(i).name+"  "+cs.get(i).primary();
   DialogUtil.actions(SettingsActivity.this,"חיוג מהיר "+d,n,new DialogUtil.Choice(){public void onChoice(int w){AppPrefs.put(SettingsActivity.this,"speed_"+d,cs.get(w).primary());buildLabels();list.invalidateViews();}});
  }});
 }
 private static class CursorHolder{
  interface Done{void done(ArrayList<ContactModel> c);}
  static void load(final android.content.Context c,final Done done){new android.os.AsyncTask<Void,Void,ArrayList<ContactModel>>(){protected ArrayList<ContactModel>doInBackground(Void...v){ArrayList<ContactModel>o=new ArrayList<ContactModel>();android.database.Cursor x=null;try{x=ContactsRepository.phones(c);long last=-1;if(x!=null)while(x.moveToNext()&&o.size()<30){long id=x.getLong(1);if(id==last)continue;last=id;ContactModel m=new ContactModel(id,x.getString(2));m.phones.add(new ContactModel.PhoneNumber(x.getString(3),ContactsRepository.phoneType(x.getInt(4))));o.add(m);}}finally{if(x!=null)x.close();}return o;}protected void onPostExecute(ArrayList<ContactModel>x){done.done(x);}}.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);}
 }
 @Override protected void onKeyAction(KeyMapper.Result r,boolean down){if(down)return;switch(r.action){case UP:selected=Math.max(0,selected-1);break;case DOWN:selected=Math.min(labels.size()-1,selected+1);break;case SELECT:toggle(selected);break;case BACK:finish();break;default:return;}if(list!=null){list.setSelection(selected);list.invalidateViews();}}
}