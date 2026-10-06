package com.example.contacts.ui;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.Ui;

public class EditContactActivity extends Activity {
    private EditText first,last,phone,email,note;
    private TextView phoneType,modeLabel;
    private long contactId=-1;
    private boolean originalLoaded=false, saving=false;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        contactId=getIntent().getLongExtra("contactId",-1);
        build();
    }

    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(ColorUtil.BG);
        TextView title=Ui.bar(this,contactId<0?"הוספת איש קשר":"עריכת איש קשר",22);root.addView(title,new LinearLayout.LayoutParams(-1,50));
        modeLabel=Ui.bar(this,"עב / EN / 123 • לפי שיטת הקלט של המכשיר",12);modeLabel.setTextColor(ColorUtil.SECONDARY);root.addView(modeLabel,new LinearLayout.LayoutParams(-1,28));
        LinearLayout form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(18,4,18,4);
        first=field(form,"שם פרטי","");last=field(form,"שם משפחה","");phone=field(form,"טלפון","");phone.setInputType(InputType.TYPE_CLASS_PHONE);
        LinearLayout phoneLine=new LinearLayout(this);phoneLine.addView(phone,new LinearLayout.LayoutParams(0,58,1f));
        phoneType=Ui.bar(this,"נייד",14);phoneLine.addView(phoneType,new LinearLayout.LayoutParams(105,58));phoneType.setOnClickListener(new View.OnClickListener(){public void onClick(View v){choosePhoneType();}});
        email=field(form,"אימייל","");email.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        note=field(form,"הערה","");
        // Rebuild phone slot so the type chooser sits on the same focus line.
        form.removeView(phone);form.addView(phoneLine,2,new LinearLayout.LayoutParams(-1,58));
        form.addView(email,new LinearLayout.LayoutParams(-1,58));form.addView(note,new LinearLayout.LayoutParams(-1,90));
        root.addView(form,new LinearLayout.LayoutParams(-1,0,1f));
        LinearLayout bottom=new LinearLayout(this);TextView save=Ui.bar(this,"שמור",15),cancel=Ui.bar(this,"ביטול",15);bottom.addView(save,new LinearLayout.LayoutParams(0,48,1f));bottom.addView(Ui.bar(this,"OK",15),new LinearLayout.LayoutParams(0,48,1f));bottom.addView(cancel,new LinearLayout.LayoutParams(0,48,1f));
        save.setOnClickListener(new View.OnClickListener(){public void onClick(View v){save();}});cancel.setOnClickListener(new View.OnClickListener(){public void onClick(View v){cancel();}});
        root.addView(bottom,new LinearLayout.LayoutParams(-1,48));setContentView(root);
        if(contactId>=0)new Load().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else first.requestFocus();
    }

    private EditText field(LinearLayout p,String hint,String value){
        EditText e=new EditText(this);e.setHint(hint);e.setText(value);e.setTextSize(18);e.setTextColor(ColorUtil.TEXT);e.setHintTextColor(ColorUtil.SECONDARY);e.setSingleLine(true);e.setFocusable(true);e.setFocusableInTouchMode(false);p.addView(e,new LinearLayout.LayoutParams(-1,58));return e;
    }

    private void choosePhoneType(){
        DialogUtil.actions(this,"סוג מספר",new String[]{"נייד","בית","עבודה","ראשי","פקס"},
            new DialogUtil.Choice(){public void onChoice(int w){phoneType.setText(new String[]{"נייד","בית","עבודה","ראשי","פקס"}[w]);}});
    }

    private class Load extends AsyncTask<Void,Void,String[]>{
        protected String[] doInBackground(Void...v){String n=getIntent().getStringExtra("name");String num=getIntent().getStringExtra("number");if(n==null)n="";if(num==null)num="";return new String[]{n,num};}
        protected void onPostExecute(String[] v){String[] p=v[0].trim().split("\\s+",2);first.setText(p.length>0?p[0]:"");last.setText(p.length>1?p[1]:"");phone.setText(v[1]);originalLoaded=true;}
    }

    private boolean changed(){
        return originalLoaded && !same(getIntent().getStringExtra("name"),first.getText().toString()+" "+last.getText().toString())||
               !same(getIntent().getStringExtra("number"),phone.getText().toString());
    }
    private boolean same(String a,String b){return (a==null?"":a).trim().equals((b==null?"":b).trim());}

    private void save(){
        if(saving)return;saving=true;
        final String display=(first.getText().toString().trim()+" "+last.getText().toString().trim()).trim();
        final String number=phone.getText().toString().trim(),mail=email.getText().toString().trim(),memo=note.getText().toString().trim();
        new AsyncTask<Void,Void,Boolean>(){
            protected Boolean doInBackground(Void...v){try{
                if(contactId<0){
                    ContentProviderOperation.Builder raw=ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
                    ArrayList<ContentProviderOperation> ops=new ArrayList<ContentProviderOperation>();
                    ops.add(raw.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,null).withValue(ContactsContract.RawContacts.ACCOUNT_NAME,null).build());
                    int rawIx=0;
                    if(display.length()>0)ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawIx).withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,first.getText().toString()).withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,last.getText().toString()).build());
                    if(number.length()>0)ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawIx).withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,number).withValue(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
                    if(mail.length()>0)ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawIx).withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Email.DATA,mail).withValue(ContactsContract.CommonDataKinds.Email.TYPE,ContactsContract.CommonDataKinds.Email.TYPE_HOME).build());
                    if(memo.length()>0)ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawIx).withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE).withValue(ContactsContract.CommonDataKinds.Note.NOTE,memo).build());
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY,ops);
                }else{
                    long rawId=com.example.contacts.data.ContactsRepository.findRawContact(EditContactActivity.this,contactId);if(rawId<0)return false;
                    String[] mt={ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                    for(String m:mt)getContentResolver().delete(ContactsContract.Data.CONTENT_URI,ContactsContract.Data.RAW_CONTACT_ID+"=? AND "+ContactsContract.Data.MIMETYPE+"=?",new String[]{String.valueOf(rawId),m});
                    ContentValues n=new ContentValues();n.put(ContactsContract.Data.RAW_CONTACT_ID,rawId);n.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);n.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,first.getText().toString());n.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,last.getText().toString());getContentResolver().insert(ContactsContract.Data.CONTENT_URI,n);
                    if(number.length()>0){ContentValues x=new ContentValues();x.put(ContactsContract.Data.RAW_CONTACT_ID,rawId);x.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);x.put(ContactsContract.CommonDataKinds.Phone.NUMBER,number);x.put(ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);getContentResolver().insert(ContactsContract.Data.CONTENT_URI,x);}
                    if(mail.length()>0){ContentValues x=new ContentValues();x.put(ContactsContract.Data.RAW_CONTACT_ID,rawId);x.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);x.put(ContactsContract.CommonDataKinds.Email.DATA,mail);x.put(ContactsContract.CommonDataKinds.Email.TYPE,ContactsContract.CommonDataKinds.Email.TYPE_HOME);getContentResolver().insert(ContactsContract.Data.CONTENT_URI,x);}
                    if(memo.length()>0){ContentValues x=new ContentValues();x.put(ContactsContract.Data.RAW_CONTACT_ID,rawId);x.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);x.put(ContactsContract.CommonDataKinds.Note.NOTE,memo);getContentResolver().insert(ContactsContract.Data.CONTENT_URI,x);}
                }
                return true;
            }catch(Exception e){return false;}}
            protected void onPostExecute(Boolean ok){saving=false;if(ok){Toast.makeText(EditContactActivity.this,"נשמר",Toast.LENGTH_SHORT).show();finish();}else Toast.makeText(EditContactActivity.this,"השמירה נכשלה",Toast.LENGTH_LONG).show();}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void cancel(){if(changed()){DialogUtil.actions(this,"יש שינויים שלא נשמרו",new String[]{"שמור","צא בלי לשמור"},new DialogUtil.Choice(){public void onChoice(int w){if(w==0)save();else finish();}});}else finish();}

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        int k=e.getKeyCode();
        if(e.getAction()==KeyEvent.ACTION_DOWN){
            if(k==KeyEvent.KEYCODE_DPAD_RIGHT||k==KeyEvent.KEYCODE_DPAD_LEFT){
                if(getCurrentFocus() instanceof EditText)return super.dispatchKeyEvent(e);
            }
            e.startTracking();return relevant(k);
        }
        if(e.getAction()==KeyEvent.ACTION_UP&&relevant(k)){
            if(k==KeyEvent.KEYCODE_DPAD_DOWN)focusNext(1);
            else if(k==KeyEvent.KEYCODE_DPAD_UP)focusNext(-1);
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER)saveOrType();
            else if(k==KeyEvent.KEYCODE_SOFT_LEFT||k==KeyEvent.KEYCODE_MENU)save();
            else if(k==KeyEvent.KEYCODE_SOFT_RIGHT)cancel();
            else if(k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_ENDCALL)cancel();
            return true;
        }
        return relevant(k);
    }

    private void focusNext(int delta){
        View v=getCurrentFocus();View next=v==null?first:null;
        if(v==first)next=delta>0?last:first;else if(v==last)next=delta>0?phone:last;else if(v==phone)next=delta>0?email:last;else if(v==email)next=delta>0?note:phone;else if(v==note)next=delta>0?first:email;else if(v==phoneType)next=delta>0?email:phone;
        if(next!=null)next.requestFocus();
    }
    private void saveOrType(){if(getCurrentFocus()==phoneType)choosePhoneType();else save();}
    private boolean relevant(int k){return k==KeyEvent.KEYCODE_DPAD_UP||k==KeyEvent.KEYCODE_DPAD_DOWN||k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER||k==KeyEvent.KEYCODE_SOFT_LEFT||k==KeyEvent.KEYCODE_SOFT_RIGHT||k==KeyEvent.KEYCODE_MENU||k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_ENDCALL||k==KeyEvent.KEYCODE_DPAD_LEFT||k==KeyEvent.KEYCODE_DPAD_RIGHT;}
}
