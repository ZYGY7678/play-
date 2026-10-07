package com.example.contacts.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.Toast;

import com.example.contacts.util.Palette;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.AvatarView;
import com.example.contacts.widget.FocusableRow;
import com.example.contacts.widget.IconView;
import com.example.contacts.widget.SoftKeyBar;

import java.util.ArrayList;

public class ContactDetailActivity extends BaseKeyActivity {
 private long id=-1;private String contactName="";private final ArrayList<Field> fields=new ArrayList<Field>();private ListView list;private int selected=0;
 @Override protected void onCreate(Bundle b){super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);id=getIntent().getLongExtra("contactId",-1);contactName=getIntent().getStringExtra("name");if(contactName==null)contactName="";load();build();}
 private static class Field{int kind;String label,value;Field(int k,String l,String v){kind=k;label=l;value=v;}}
 private void load(){Cursor c=null;try{c=getContentResolver().query(ContactsContract.Data.CONTENT_URI,new String[]{ContactsContract.Data.MIMETYPE,ContactsContract.Data.DATA1,ContactsContract.Data.DATA2},ContactsContract.Data.CONTACT_ID+"=?",new String[]{String.valueOf(id)},null);if(c!=null)while(c.moveToNext()){String m=c.getString(0),v=c.getString(1);if(v==null||v.length()==0)continue;int t=c.isNull(2)?0:c.getInt(2);if(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(1,ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(),t,"טלפון").toString(),v));else if(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(2,"אימייל",v));else if(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(3,"כתובת",v));else if(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(4,"הערה",v));else if(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(m)&&t==ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)fields.add(new Field(5,"יום הולדת",v));}}finally{if(c!=null)c.close();}if(fields.size()==0)fields.add(new Field(0,"אין פרטים נוספים",""));}
 private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Palette.bg(this));
  LinearLayout head=new LinearLayout(this);head.setOrientation(LinearLayout.VERTICAL);head.setGravity(Gravity.CENTER_HORIZONTAL);head.setBackgroundColor(Palette.bar(this));head.setPadding(Ui.dp(this,16),Ui.dp(this,12),Ui.dp(this,16),Ui.dp(this,10));
  AvatarView av=new AvatarView(this);av.setName(contactName);av.setRing(true);int big=Ui.dim(this,com.example.contacts.R.dimen.avatar_hero);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(big,big);ap.gravity=Gravity.CENTER_HORIZONTAL;head.addView(av,ap);
  TextView n=txt(contactName.length()>0?contactName:"ללא שם",24,Palette.text(this));n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);n.setGravity(Gravity.CENTER);n.setSingleLine(true);n.setEllipsize(android.text.TextUtils.TruncateAt.END);n.setPadding(0,Ui.dp(this,8),0,0);head.addView(n,new LinearLayout.LayoutParams(-1,-2));
  String first=firstPhone();if(first.length()>0){TextView sub=txt(PhoneFormatter.ltr(PhoneFormatter.format(first)),15,Palette.secondary(this));sub.setGravity(Gravity.CENTER);head.addView(sub,new LinearLayout.LayoutParams(-1,-2));}
  root.addView(head,new LinearLayout.LayoutParams(-1,-2));root.addView(Ui.divider(this,Palette.divider(this),1));
  list=new ListView(this);list.setDivider(null);list.setSelector(new ColorDrawable(Color.TRANSPARENT));list.setDrawSelectorOnTop(false);list.setItemsCanFocus(true);list.setVerticalScrollBarEnabled(false);list.setPadding(Ui.dp(this,8),Ui.dp(this,6),Ui.dp(this,8),0);list.setClipToPadding(false);list.setAdapter(new Adapter());root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
  SoftKeyBar sk=new SoftKeyBar(this,"ערוך","OK","אפשרויות");
  sk.left.setOnClickListener(new View.OnClickListener(){public void onClick(View v){edit();}});sk.center.setOnClickListener(new View.OnClickListener(){public void onClick(View v){callCurrent();}});sk.right.setOnClickListener(new View.OnClickListener(){public void onClick(View v){options();}});
  root.addView(sk,new LinearLayout.LayoutParams(-1,-2));setContentView(root);list.requestFocus();list.setSelection(0);}
 private TextView txt(String s,float sp,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp*Palette.scale(this));t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);return t;}
 private IconView icon(int type){IconView i=new IconView(this);i.setType(type);return i;}
 private class Adapter extends BaseAdapter{public int getCount(){return fields.size();}public Object getItem(int p){return fields.get(p);}public long getItemId(int p){return p;}
  public View getView(int p,View v,ViewGroup parent){Field f=fields.get(p);FocusableRow r=new FocusableRow(ContactDetailActivity.this);
   LinearLayout box=new LinearLayout(ContactDetailActivity.this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER_VERTICAL);
   if(f.kind==0){box.addView(txt(f.label,16,Palette.secondary(ContactDetailActivity.this)),new LinearLayout.LayoutParams(-1,-2));}
   else{TextView a=txt(f.label,13,Palette.secondary(ContactDetailActivity.this));TextView b=txt(f.kind==1?PhoneFormatter.ltr(PhoneFormatter.format(f.value)):f.value,18,Palette.text(ContactDetailActivity.this));if(f.kind==1)b.setTextDirection(View.TEXT_DIRECTION_LTR);box.addView(a,new LinearLayout.LayoutParams(-1,-2));box.addView(b,new LinearLayout.LayoutParams(-1,-2));}
   r.addView(box,new LinearLayout.LayoutParams(0,-2,1));
   if(f.kind==1){int s=Ui.dp(ContactDetailActivity.this,28);r.addView(icon(IconView.CALL),new LinearLayout.LayoutParams(s,s));r.addView(icon(IconView.SMS),new LinearLayout.LayoutParams(s,s));r.addView(icon(IconView.COPY),new LinearLayout.LayoutParams(s,s));}
   return r;}}
 private Field current(){if(fields.size()==0)return null;return fields.get(Math.max(0,Math.min(selected,fields.size()-1)));}
 private void callCurrent(){Field f=current();if(f!=null&&f.kind==1)call(f.value);}
 private void call(String n){try{startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(n))));}catch(Exception e){Toast.makeText(this,"חיוג לא זמין",Toast.LENGTH_SHORT).show();}}
 private void sms(String n){try{startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:"+Uri.encode(n))));}catch(Exception e){Toast.makeText(this,"SMS לא זמין",Toast.LENGTH_SHORT).show();}}
 private void copy(String n){((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("phone",n));Toast.makeText(this,"הועתק",Toast.LENGTH_SHORT).show();}
 private void options(){final boolean locked=AppPrefs.bool(this,"kids_lock",false);final String[] a=locked?new String[]{"חייג","שלח הודעה","מועדף","שתף"}:new String[]{"חייג","שלח הודעה","ערוך","מחק","מועדף","שתף"};DialogUtil.actions(this,contactName,a,new DialogUtil.Choice(){public void onChoice(int w){Field f=current();if(w==0&&f!=null&&f.kind==1)call(f.value);else if(w==1&&f!=null&&f.kind==1)sms(f.value);else if(locked&&w==2)fav();else if(locked&&w==3)share();else if(!locked&&w==2)edit();else if(!locked&&w==3)del();else if(!locked&&w==4)fav();else if(!locked&&w==5)share();}});}
 private void edit(){if(AppPrefs.bool(this,"kids_lock",false)){Toast.makeText(this,"מצב ילדים פעיל",Toast.LENGTH_SHORT).show();return;}startActivity(new Intent(this,EditContactActivity.class).putExtra("contactId",id).putExtra("name",contactName).putExtra("number",firstPhone()));}
 private String firstPhone(){for(Field f:fields)if(f.kind==1)return f.value;return "";}
 private void del(){if(AppPrefs.bool(this,"kids_lock",false)){Toast.makeText(this,"מצב ילדים פעיל",Toast.LENGTH_SHORT).show();return;}getContentResolver().delete(ContactsContract.Contacts.CONTENT_URI,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(id)});finish();}
 private void fav(){ContentValues v=new ContentValues();v.put(ContactsContract.Contacts.STARRED,1);getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,v,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(id)});Toast.makeText(this,"נוסף למועדפים",Toast.LENGTH_SHORT).show();}
 private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,contactName+"\n"+firstPhone());startActivity(Intent.createChooser(i,"שתף איש קשר"));}
 @Override protected void onKeyAction(com.example.contacts.keys.KeyMapper.Result r,boolean down){if(down)return;switch(r.action){case UP:selected=Math.max(0,selected-1);break;case DOWN:selected=Math.min(fields.size()-1,selected+1);break;case CALL:callCurrent();break;case SELECT:callCurrent();break;case RIGHT:{Field f=current();if(f!=null&&f.kind==1)sms(f.value);break;}case LEFT:{Field f=current();if(f!=null&&f.kind==1)copy(f.value);break;}case SOFT_LEFT:edit();break;case SOFT_RIGHT:options();break;case MENU:options();break;case BACK:finish();break;default:return;}list.setSelectionFromTop(selected,Math.max(0,list.getHeight()/2-36));list.invalidateViews();}
}