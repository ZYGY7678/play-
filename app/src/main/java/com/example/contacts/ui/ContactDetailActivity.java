package com.example.contacts.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;

import com.example.contacts.R;
import com.example.contacts.data.ContactsRepository;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.FocusableRow;

import java.util.ArrayList;
import java.util.List;

public class ContactDetailActivity extends Activity {
    private long contactId;
    private String contactName;
    private List<Field> fields=new ArrayList<Field>();
    private ListView list;
    private int selected=0;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        contactId=getIntent().getLongExtra("contactId",-1);
        contactName=getIntent().getStringExtra("name");if(contactName==null)contactName="";
        load();build();
    }

    private void load(){
        Cursor c=null;
        try{
            c=getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                    new String[]{ContactsContract.Data.MIMETYPE,ContactsContract.Data.DATA1,ContactsContract.Data.DATA2},
                    ContactsContract.Data.CONTACT_ID+"=?",new String[]{String.valueOf(contactId)},null);
            if(c==null)return;
            while(c.moveToNext()){
                String type=c.getString(0),value=c.getString(1);int data2=c.isNull(2)?0:c.getInt(2);
                if(value==null||value.length()==0)continue;
                if(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(type))fields.add(new Field(1,"טלפון • "+phoneType(data2),value));
                else if(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(type))fields.add(new Field(2,"אימייל",value));
                else if(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(type))fields.add(new Field(3,"כתובת",value));
                else if(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(type))fields.add(new Field(4,"הערה",value));
                else if(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(type)&&data2==ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)fields.add(new Field(5,"יום הולדת",value));
            }
        }finally{if(c!=null)c.close();}
        if(fields.size()==0)fields.add(new Field(0,"אין פרטים נוספים",""));
    }

    private String phoneType(int t){
        if(t==ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)return"נייד";
        if(t==ContactsContract.CommonDataKinds.Phone.TYPE_HOME)return"בית";
        if(t==ContactsContract.CommonDataKinds.Phone.TYPE_WORK)return"עבודה";
        return"טלפון";
    }

    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(ColorUtil.BG);
        LinearLayout header=new LinearLayout(this);header.setPadding(16,8,16,8);header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        TextView avatar=Ui.text(this,contactName.length()>0?contactName.substring(0,1):"?",30,Color.WHITE);avatar.setGravity(android.view.Gravity.CENTER);avatar.setBackgroundColor(ColorUtil.stableColor(contactName));header.addView(avatar,new LinearLayout.LayoutParams(96,96));
        LinearLayout hb=new LinearLayout(this);hb.setOrientation(LinearLayout.VERTICAL);TextView n=Ui.text(this,contactName.length()>0?contactName:"ללא שם",26,ColorUtil.TEXT);n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);hb.addView(n,new LinearLayout.LayoutParams(-1,48));TextView id=Ui.text(this,"פרטי איש קשר",15,ColorUtil.SECONDARY);hb.addView(id,new LinearLayout.LayoutParams(-1,38));header.addView(hb,new LinearLayout.LayoutParams(0,96,1f));
        root.addView(header,new LinearLayout.LayoutParams(-1,112));
        list=new ListView(this);list.setDivider(null);list.setSelector(R.drawable.row_selector);list.setItemsCanFocus(false);list.setAdapter(new FieldAdapter());root.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        LinearLayout bottom=new LinearLayout(this);bottom.setBackgroundColor(ColorUtil.BAR);
        TextView left=Ui.bar(this,"ערוך",14),right=Ui.bar(this,"אפשרויות",14);bottom.addView(left,new LinearLayout.LayoutParams(0,48,1f));bottom.addView(Ui.bar(this,"OK",15),new LinearLayout.LayoutParams(0,48,1f));bottom.addView(right,new LinearLayout.LayoutParams(0,48,1f));
        left.setOnClickListener(new View.OnClickListener(){public void onClick(View v){edit();}});right.setOnClickListener(new View.OnClickListener(){public void onClick(View v){options();}});
        root.addView(bottom,new LinearLayout.LayoutParams(-1,48));
        setContentView(root);if(fields.size()>0){list.requestFocus();list.setSelection(0);}
    }

    private class FieldAdapter extends BaseAdapter{
        public int getCount(){return fields.size();}public Object getItem(int p){return fields.get(p);}public long getItemId(int p){return p;}
        public View getView(int p,View v,ViewGroup par){
            Field f=fields.get(p);FocusableRow r=new FocusableRow(ContactDetailActivity.this);r.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout box=new LinearLayout(ContactDetailActivity.this);box.setOrientation(LinearLayout.VERTICAL);
            TextView a=Ui.text(ContactDetailActivity.this,f.label,15,ColorUtil.SECONDARY);TextView b=Ui.text(ContactDetailActivity.this, f.value.length()>0?(f.kind==1?PhoneFormatter.format(f.value):f.value):"אין ערך",18,ColorUtil.TEXT);
            if(f.kind==1)b.setTextDirection(View.TEXT_DIRECTION_LTR);box.addView(a,new LinearLayout.LayoutParams(-1,30));box.addView(b,new LinearLayout.LayoutParams(-1,38));r.addView(box,new LinearLayout.LayoutParams(0,72,1f));
            TextView hint=Ui.text(ContactDetailActivity.this,f.kind==1?"☎  SMS  COPY":"",13,f.kind==1?ColorUtil.ACCENT:Color.TRANSPARENT);hint.setGravity(android.view.Gravity.CENTER);r.addView(hint,new LinearLayout.LayoutParams(110,72));
            return r;
        }
    }

    private static class Field{int kind;String label,value;Field(int k,String l,String v){kind=k;label=l;value=v;}}

    private void selectedPhoneAction(int action){
        if(fields.size()==0)return;Field f=fields.get(Math.max(0,Math.min(selected,fields.size()-1)));if(f.kind!=1)return;
        if(action==0)call(f.value);else if(action==1)sms(f.value);else copy(f.value);
    }

    private void call(String n){try{startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(n))));}catch(Exception e){Toast.makeText(this,"חיוג לא זמין",Toast.LENGTH_SHORT).show();}}
    private void sms(String n){try{startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:"+Uri.encode(n))));}catch(Exception e){Toast.makeText(this,"SMS לא זמין",Toast.LENGTH_SHORT).show();}}
    private void copy(String s){((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("phone",s));Toast.makeText(this,"הועתק",Toast.LENGTH_SHORT).show();}
    private void edit(){startActivity(new Intent(this,EditContactActivity.class).putExtra("contactId",contactId).putExtra("name",contactName).putExtra("number",firstPhone()));}
    private String firstPhone(){for(Field f:fields)if(f.kind==1)return f.value;return "";}
    private void options(){
        DialogUtil.actions(this,"אפשרויות",new String[]{"חייג","שלח הודעה","ערוך","מחק","מועדף","שתף"},
                new DialogUtil.Choice(){public void onChoice(int w){Field f=fields.get(Math.max(0,Math.min(selected,fields.size()-1)));if(w==0&&f.kind==1)call(f.value);else if(w==1&&f.kind==1)sms(f.value);else if(w==2)edit();else if(w==3)delete();else if(w==4)favorite();else if(w==5)share();}});
    }
    private void delete(){getContentResolver().delete(ContactsContract.Contacts.CONTENT_URI,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(contactId)});finish();}
    private void favorite(){ContentValues v=new ContentValues();v.put(ContactsContract.Contacts.STARRED,1);getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,v,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(contactId)});Toast.makeText(this,"נוסף למועדפים",Toast.LENGTH_SHORT).show();}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,contactName+"  "+firstPhone());startActivity(Intent.createChooser(i,"שתף איש קשר"));}

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        int k=e.getKeyCode();
        if(e.getAction()==KeyEvent.ACTION_DOWN){e.startTracking();return relevant(k);}
        if(e.getAction()==KeyEvent.ACTION_UP&&relevant(k)){
            if(k==KeyEvent.KEYCODE_DPAD_UP)selected=Math.max(0,selected-1);
            else if(k==KeyEvent.KEYCODE_DPAD_DOWN)selected=Math.min(fields.size()-1,selected+1);
            else if(k==KeyEvent.KEYCODE_CALL)selectedPhoneAction(0);
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)selectedPhoneAction(0);
            else if(k==KeyEvent.KEYCODE_DPAD_RIGHT)selectedPhoneAction(1);
            else if(k==KeyEvent.KEYCODE_DPAD_LEFT)selectedPhoneAction(2);
            else if(k==KeyEvent.KEYCODE_SOFT_LEFT||k==KeyEvent.KEYCODE_MENU)edit();
            else if(k==KeyEvent.KEYCODE_SOFT_RIGHT)options();
            else if(k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_ENDCALL)finish();
            if(list!=null){list.setSelectionFromTop(selected,Math.max(0,list.getHeight()/2-36));list.invalidateViews();}
            return true;
        }
        return relevant(k);
    }
    private boolean relevant(int k){return k==KeyEvent.KEYCODE_DPAD_UP||k==KeyEvent.KEYCODE_DPAD_DOWN||k==KeyEvent.KEYCODE_DPAD_LEFT||k==KeyEvent.KEYCODE_DPAD_RIGHT||k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER||k==KeyEvent.KEYCODE_CALL||k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_ENDCALL||k==KeyEvent.KEYCODE_MENU||k==KeyEvent.KEYCODE_SOFT_LEFT||k==KeyEvent.KEYCODE_SOFT_RIGHT;}
}
