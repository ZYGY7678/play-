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
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;

import com.example.contacts.util.Palette;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.FocusableRow;

import java.util.ArrayList;

public class ContactDetailActivity extends BaseKeyActivity {
 private long id=-1;private String contactName="";private final ArrayList<Field> fields=new ArrayList<Field>();private ListView list;private int selected=0;
 @Override protected void onCreate(Bundle b){super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);id=getIntent().getLongExtra("contactId",-1);contactName=getIntent().getStringExtra("name");if(contactName==null)contactName="";load();build();}
 private static class Field{int kind;String label,value;Field(int k,String l,String v){kind=k;label=l;value=v;}}
 private void load(){Cursor c=null;try{c=getContentResolver().query(ContactsContract.Data.CONTENT_URI,new String[]{ContactsContract.Data.MIMETYPE,ContactsContract.Data.DATA1,ContactsContract.Data.DATA2},ContactsContract.Data.CONTACT_ID+"=?",new String[]{String.valueOf(id)},null);if(c!=null)while(c.moveToNext()){String m=c.getString(0),v=c.getString(1);if(v==null||v.length()==0)continue;int t=c.isNull(2)?0:c.getInt(2);if(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(1,ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(),t,"טלפון").toString(),v));else if(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(2,"אימייל",v));else if(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(3,"כתובת",v));else if(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE.equals(m))fields.add(new Field(4,"הערה",v));else if(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE.equals(m)&&t==ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY)fields.add(new Field(5,"יום הולדת",v));}}finally{if(c!=null)c.close();}if(fields.size()==0)fields.add(new Field(0,"אין פרטים נוספים",""));}
 private void build(){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Palette.bg(this));
  LinearLayout head=new LinearLayout(this);head.setPadding(Ui.dp(this,16),Ui.dp(this,8),Ui.dp(this,16),Ui.dp(this,8));head.setGravity(Gravity.CENTER_VERTICAL);TextView av=txt(contactName.length()>0?contactName.substring(0,1):"?",32,Palette.text(this));av.setGravity(Gravity.CENTER);GradientDrawable avatarBg=new GradientDrawable();avatarBg.setShape(GradientDrawable.OVAL);avatarBg.setColor(0xff3b6178);avatarBg.setStroke(Ui.dp(this,3),Palette.accent(this));av.setBackground(avatarBg);head.addView(av,new LinearLayout.LayoutParams(Ui.dp(this,100),Ui.dp(this,100)));TextView n=txt(contactName.length()>0?contactName:"ללא שם",26,Palette.text(this));n.setTypeface(Typeface.DEFAULT,Typeface.BOLD);n.setPadding(Ui.dp(this,18),0,0,0);head.addView(n,new LinearLayout.LayoutParams(0,Ui.dp(this,100),1));root.addView(head,new LinearLayout.LayoutParams(-1,Ui.dp(this,116)));
  list=new ListView(this);list.setDivider(null);list.setSelector(com.example.contacts.R.drawable.row_selector);list.setDrawSelectorOnTop(false);list.setItemsCanFocus(true);list.setAdapter(new Adapter());root.addView(list,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout bot=new LinearLayout(this);bot.setBackgroundColor(Palette.bar(this));bot.addView(txt("ערוך",15,Palette.text(this)),new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));bot.addView(txt("OK",15,Palette.text(this)),new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));bot.addView(txt("אפשרויות",15,Palette.text(this)),new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));root.addView(bot,new LinearLayout.LayoutParams(-1,Ui.dp(this,50)));setContentView(root);list.requestFocus();list.setSelection(0);}
 private TextView txt(String s,float sp,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp*Palette.scale(this));t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);return t;}
 private class Adapter extends BaseAdapter{public int getCount(){return fields.size();}public Object getItem(int p){return fields.get(p);}public long getItemId(int p){return p;}public View getView(int p,View v,ViewGroup parent){Field f=fields.get(p);FocusableRow r=new FocusableRow(ContactDetailActivity.this);LinearLayout box=new LinearLayout(ContactDetailActivity.this);box.setOrientation(LinearLayout.VERTICAL);TextView a=txt(f.label,14,Palette.secondary(ContactDetailActivity.this));TextView b=txt(f.kind==1?PhoneFormatter.ltr(PhoneFormatter.format(f.value)):f.value,18,Palette.text(ContactDetailActivity.this));if(f.kind==1)b.setTextDirection(View.TEXT_DIRECTION_LTR);box.addView(a,new LinearLayout.LayoutParams(-1,Ui.dp(ContactDetailActivity.this,30)));box.addView(b,new LinearLayout.LayoutParams(-1,Ui.dp(ContactDetailActivity.this,42)));r.addView(box,new LinearLayout.LayoutParams(0,Ui.dp(ContactDetailActivity.this,72),1));TextView hint=txt(f.kind==1?"☎  SMS  COPY":"",12,f.kind==1?Palette.accent(ContactDetailActivity.this):Palette.secondary(ContactDetailActivity.this));hint.setGravity(Gravity.CENTER);r.addView(hint,new LinearLayout.LayoutParams(Ui.dp(ContactDetailActivity.this,110),Ui.dp(ContactDetailActivity.this,72)));return r;}}
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