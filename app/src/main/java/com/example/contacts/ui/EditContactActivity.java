package com.example.contacts.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.contacts.keys.KeyMapper;
import com.example.contacts.util.Palette;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.ScreenHeader;
import com.example.contacts.widget.SoftKeyBar;

public class EditContactActivity extends BaseKeyActivity {
 private EditText first,last,phone,email,note; private TextView type; private long id=-1;
 @Override protected void onCreate(Bundle b){super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);id=getIntent().getLongExtra("contactId",-1);build();if(id>=0)load();}
 private EditText e(String h){EditText x=new EditText(this);x.setHint(h);x.setHintTextColor(Palette.secondary(this));x.setTextColor(Palette.text(this));x.setTextSize(18*Palette.scale(this));x.setSingleLine(true);x.setFocusable(true);x.setFocusableInTouchMode(false);x.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);GradientDrawable normal=Ui.rounded(Palette.row(this),Palette.divider(this),1,12,this);GradientDrawable focus=Ui.rounded(Palette.row(this),Palette.accent(this),2,12,this);StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_focused},focus);states.addState(new int[]{},normal);x.setBackground(states);x.setPadding(Ui.dp(this,14),0,Ui.dp(this,14),0);return x;}
 private void build(){LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setBackgroundColor(Palette.bg(this));
  r.addView(new ScreenHeader(this,id<0?"הוספת איש קשר":"עריכת איש קשר"),new LinearLayout.LayoutParams(-1,-2));
  ScrollView sv=new ScrollView(this);sv.setFillViewport(true);sv.setVerticalScrollBarEnabled(false);
  LinearLayout f=new LinearLayout(this);f.setOrientation(LinearLayout.VERTICAL);f.setPadding(Ui.dp(this,16),Ui.dp(this,12),Ui.dp(this,16),Ui.dp(this,8));
  first=e("שם פרטי");last=e("שם משפחה");phone=e("טלפון");email=e("אימייל");note=e("הערה");phone.setInputType(3);
  note.setSingleLine(false);note.setMinLines(2);note.setGravity(Gravity.TOP|Gravity.START);note.setPadding(Ui.dp(this,14),Ui.dp(this,12),Ui.dp(this,14),Ui.dp(this,12));
  f.addView(first,Ui.lp(this,-1,52,0,0,0,8));f.addView(last,Ui.lp(this,-1,52,0,0,0,8));
  LinearLayout pl=new LinearLayout(this);pl.addView(phone,new LinearLayout.LayoutParams(0,Ui.dp(this,52),1));
  type=Ui.chip(this,"נייד",15);type.setOnClickListener(new View.OnClickListener(){public void onClick(View v){pickType();}});
  pl.addView(type,Ui.lp(this,92,52,8,0,0,0));f.addView(pl,Ui.lp(this,-1,-2,0,0,0,8));
  f.addView(email,Ui.lp(this,-1,52,0,0,0,8));f.addView(note,Ui.lp(this,-1,84,0,0,0,8));
  sv.addView(f,new android.widget.FrameLayout.LayoutParams(-1,-2));r.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
  SoftKeyBar sk=new SoftKeyBar(this,"שמור","OK","ביטול");
  sk.left.setOnClickListener(new View.OnClickListener(){public void onClick(View v){save();}});sk.center.setOnClickListener(new View.OnClickListener(){public void onClick(View v){save();}});sk.right.setOnClickListener(new View.OnClickListener(){public void onClick(View v){cancel();}});
  r.addView(sk,new LinearLayout.LayoutParams(-1,-2));setContentView(r);first.requestFocus();}
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