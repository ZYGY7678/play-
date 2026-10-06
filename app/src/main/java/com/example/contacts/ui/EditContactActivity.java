package com.example.contacts.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.contacts.keys.KeyMapper;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;

public class EditContactActivity extends BaseKeyActivity {
 private EditText first,last,phone,email,note; private TextView type; private long id=-1;
 @Override protected void onCreate(Bundle b){super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);id=getIntent().getLongExtra("contactId",-1);build();if(id>=0)load();}
 private TextView t(String s,float z){TextView v=new TextView(this);v.setText(s);v.setTextSize(z*Palette.scale(this));v.setTextColor(Palette.text(this));v.setGravity(Gravity.CENTER);return v;}
 private EditText e(String h){EditText x=new EditText(this);x.setHint(h);x.setHintTextColor(Palette.secondary(this));x.setTextColor(Palette.text(this));x.setTextSize(18*Palette.scale(this));x.setSingleLine(true);x.setFocusable(true);x.setFocusableInTouchMode(false);return x;}
 private void build(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(Palette.bg(this));
  r.addView(t(id<0?"הוספת איש קשר":"עריכת איש קשר",22),new LinearLayout.LayoutParams(-1,Ui.dp(this,54)));
  r.addView(t("עב / EN / 123  •  IME של המכשיר",12),new LinearLayout.LayoutParams(-1,Ui.dp(this,30)));
  LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(Ui.dp(this,18),0,Ui.dp(this,18),0);
  first=e("שם פרטי");last=e("שם משפחה");phone=e("טלפון");email=e("אימייל");note=e("הערה");phone.setInputType(3);
  f.addView(first,new LinearLayout.LayoutParams(-1,Ui.dp(this,58)));f.addView(last,new LinearLayout.LayoutParams(-1,Ui.dp(this,58)));
  LinearLayout pl=new LinearLayout(this);pl.addView(phone,new LinearLayout.LayoutParams(0,Ui.dp(this,58),1));type=t("נייד",14);type.setBackgroundResource(com.example.contacts.R.drawable.row_selector);pl.addView(type,new LinearLayout.LayoutParams(100,58));type.setOnClickListener(new View.OnClickListener(){public void onClick(View v){pickType();}});f.addView(pl);
  f.addView(email,new LinearLayout.LayoutParams(-1,Ui.dp(this,58)));f.addView(note,new LinearLayout.LayoutParams(-1,Ui.dp(this,90)));r.addView(f,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout b=new LinearLayout(this);TextView s=t("שמור",15),o=t("OK",15),c=t("ביטול",15);b.addView(s,new LinearLayout.LayoutParams(0,50,1));b.addView(o,new LinearLayout.LayoutParams(0,50,1));b.addView(c,new LinearLayout.LayoutParams(0,50,1));s.setOnClickListener(new View.OnClickListener(){public void onClick(View v){save();}});c.setOnClickListener(new View.OnClickListener(){public void onClick(View v){cancel();}});r.addView(b,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));setContentView(r);first.requestFocus();}
 private void load(){android.os.AsyncTask.execute(new Runnable(){public void run(){Cursor c=null;try{c=getContentResolver().query(ContactsContract.Data.CONTENT_URI,new String[]{ContactsContract.Data.MIMETYPE,ContactsContract.Data.DATA1,ContactsContract.Data.DATA2},ContactsContract.Data.CONTACT_ID+"=?",new String[]{String.valueOf(id)},null);final String[]v=new String[]{"","","","",""};if(c!=null)while(c.moveToNext()){String m=c.getString(0),x=c.getString(1);if(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE.equals(m)&&v[0].length()==0)v[0]=x==null?"":x;else if(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(m)&&v[2].length()==0)v[2]=x==null?"":x;else if(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(m)&&v[3].length()==0)v[3]=x==null?"":x;else if(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(m)&&v[4].length()==0)v[4]=x==null?"":x;}runOnUiThread(new Runnable(){public void run(){String[]n=v[0].split(" ",2);first.setText(n.length>0?n[0]:"");last.setText(n.length>1?n[1]:"");phone.setText(v[2]);email.setText(v[3]);note.setText(v[4]);}});}finally{if(c!=null)c.close();}}});}
 private void pickType(){DialogUtil.actions(this,"סוג מספר",new String[]{"נייד","בית","עבודה","ראשי","פקס"},new DialogUtil.Choice(){public void onChoice(int w){type.setText(new String[]{"נייד","בית","עבודה","ראשי","פקס"}[w]);}});}
 private void save(){final String n=(first.getText().toString()+" "+last.getText().toString()).trim(),p=phone.getText().toString().trim();if(n.length()==0&&p.length()==0){Toast.makeText(this,"הזן שם או מספר",Toast.LENGTH_SHORT).show();return;}
  android.os.AsyncTask.execute(new Runnable(){public void run(){try{if(id<0){ContentValues rv=new ContentValues();rv.putNull(ContactsContract.RawContacts.ACCOUNT_NAME);rv.putNull(ContactsContract.RawContacts.ACCOUNT_TYPE);UriHolder.raw=getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI,rv);if(UriHolder.raw==null)throw new IllegalStateException();id=Long.parseLong(UriHolder.raw.getLastPathSegment());}else{long raw=com.example.contacts.data.ContactsRepository.rawId(EditContactActivity.this,id);id=raw;getContentResolver().delete(ContactsContract.Data.CONTENT_URI,ContactsContract.Data.RAW_CONTACT_ID+"=?",new String[]{String.valueOf(raw)});}
    long raw=id;ContentValues sn=new ContentValues();sn.put(ContactsContract.Data.RAW_CONTACT_ID,raw);sn.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);sn.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,first.getText().toString());sn.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,last.getText().toString());getContentResolver().insert(ContactsContract.Data.CONTENT_URI,sn);
    if(p.length()>0){ContentValues q=new ContentValues();q.put(ContactsContract.Data.RAW_CONTACT_ID,raw);q.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);q.put(ContactsContract.CommonDataKinds.Phone.NUMBER,p);q.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);getContentResolver().insert(ContactsContract.Data.CONTENT_URI,q);}
    String mail=email.getText().toString().trim();if(mail.length()>0){ContentValues q=new ContentValues();q.put(ContactsContract.Data.RAW_CONTACT_ID,raw);q.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);q.put(ContactsContract.CommonDataKinds.Email.DATA,mail);q.put(ContactsContract.CommonDataKinds.Email.TYPE,ContactsContract.CommonDataKinds.Email.TYPE_HOME);getContentResolver().insert(ContactsContract.Data.CONTENT_URI,q);}
    final String msg="נשמר";runOnUiThread(new Runnable(){public void run(){Toast.makeText(EditContactActivity.this,msg,Toast.LENGTH_SHORT).show();finish();}});
  }catch(Exception x){runOnUiThread(new Runnable(){public void run(){Toast.makeText(EditContactActivity.this,"השמירה נכשלה",Toast.LENGTH_LONG).show();}});}}});
 }
 private void cancel(){finish();}
 static final class UriHolder{static android.net.Uri raw;}
 @Override protected boolean allowTextInput(){return true;}
 @Override protected void onKeyAction(KeyMapper.Result r,boolean down){if(down)return;switch(r.action){case UP:focus(-1);break;case DOWN:focus(1);break;case SELECT:if(getCurrentFocus()==type)pickType();else save();break;case SOFT_LEFT:save();break;case SOFT_RIGHT:cancel();break;case MENU:save();break;case BACK:cancel();break;default:break;}}
 private void focus(int d){View v=getCurrentFocus();if(v==first)(d>0?last:first).requestFocus();else if(v==last)(d>0?phone:first).requestFocus();else if(v==phone)(d>0?email:last).requestFocus();else if(v==email)(d>0?note:phone).requestFocus();else if(v==note)(d>0?first:email).requestFocus();else if(v==type)(d>0?email:phone).requestFocus();}
}