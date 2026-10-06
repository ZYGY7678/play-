package com.example.contacts.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contacts.R;
import com.example.contacts.call.InCallActivity;
import com.example.contacts.data.CallLogRepository;
import com.example.contacts.data.ContactModel;
import com.example.contacts.data.ContactsRepository;
import com.example.contacts.keys.KeyMapper;
import com.example.contacts.keys.T9Matcher;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.FocusableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int TAB_DIAL=0, TAB_CONTACTS=1, TAB_RECENTS=2, TAB_FAVS=3;
    private LinearLayout root, content, tabs, bottom;
    private TextView title, softLeft, softRight, centerAction, dialNumber, dialHint;
    private ListView list;
    private GridView grid;
    private ContactAdapter contactAdapter;
    private CallLogAdapter callAdapter;
    private FavoriteAdapter favAdapter;
    private ArrayList<ContactModel> allContacts=new ArrayList<ContactModel>();
    private ArrayList<ContactModel> filteredContacts=new ArrayList<ContactModel>();
    private ArrayList<CallLogRepository.Entry> callEntries=new ArrayList<CallLogRepository.Entry>();
    private int tab=TAB_DIAL;
    private boolean hebrewT9=true;
    private boolean searchMode=false;
    private String searchDigits="";
    private String dialDigits="";
    private boolean redialArmed=false;
    private String lastDialed="";
    private int dialerAction=0;
    private final Handler handler=new Handler();
    private final Runnable refreshContacts=new Runnable(){public void run(){getLoaderManager().restartLoader(101,null,MainActivity.this);loadCallLog();}};
    private android.database.ContentObserver contactsObserver;
    private boolean longHandled;
    private int downKey=-1;

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        buildUi();
        getLoaderManager().initLoader(101,null,this);
        loadCallLog();
        registerContactsObserver();
    }

    private void buildUi(){
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(ColorUtil.BG);
        buildTop();
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content,new LinearLayout.LayoutParams(-1,0,1f));
        buildBottom();
        setContentView(root);
        switchTab(TAB_DIAL);
    }

    private void buildTop(){
        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.VERTICAL);bar.setBackgroundColor(ColorUtil.BAR);
        title=Ui.bar(this,"חיוג",20);bar.addView(title,new LinearLayout.LayoutParams(-1,38));
        tabs=new LinearLayout(this);tabs.setGravity(android.view.Gravity.CENTER);bar.addView(tabs,new LinearLayout.LayoutParams(-1,44));
        String[] names={"חיוג","אנשי קשר","אחרונות","מועדפים"};
        for(int i=0;i<4;i++){
            final int x=i;TextView t=Ui.bar(this,names[i],15);t.setTag(Integer.valueOf(i));t.setContentDescription(names[i]);t.setBackgroundResource(R.drawable.row_selector);
            t.setOnClickListener(new View.OnClickListener(){public void onClick(View v){switchTab(x);}});
            tabs.addView(t,new LinearLayout.LayoutParams(0,-1,1f));
        }
        root.addView(bar,new LinearLayout.LayoutParams(-1,82));
    }

    private void buildBottom(){
        bottom=new LinearLayout(this);bottom.setGravity(android.view.Gravity.CENTER_VERTICAL);bottom.setPadding(8,0,8,0);bottom.setBackgroundColor(ColorUtil.BAR);
        softLeft=Ui.bar(this,"אפשרויות",14);softRight=Ui.bar(this,"חפש",14);centerAction=Ui.bar(this,"חייג",15);
        softLeft.setOnClickListener(new View.OnClickListener(){public void onClick(View v){menuAction();}});
        softRight.setOnClickListener(new View.OnClickListener(){public void onClick(View v){searchAction();}});
        bottom.addView(softLeft,new LinearLayout.LayoutParams(0,44,1f));
        bottom.addView(centerAction,new LinearLayout.LayoutParams(0,44,1f));
        bottom.addView(softRight,new LinearLayout.LayoutParams(0,44,1f));
        root.addView(bottom,new LinearLayout.LayoutParams(-1,48));
    }

    private void switchTab(int t){
        tab=t;searchMode=false;searchDigits="";dialerAction=0;content.removeAllViews();
        title.setText(new String[]{"חיוג","אנשי קשר","אחרונות","מועדפים"}[tab]);
        softLeft.setText(tab==TAB_CONTACTS||tab==TAB_RECENTS?"אפשרויות":"אפשרויות");
        softRight.setText(tab==TAB_CONTACTS?"חפש":"חזרה");
        if(tab==TAB_DIAL) buildDialer();
        else if(tab==TAB_CONTACTS) buildContacts();
        else if(tab==TAB_RECENTS) buildRecents();
        else buildFavorites();
        tintTabs();
    }

    private void tintTabs(){
        for(int i=0;i<tabs.getChildCount();i++){
            TextView t=(TextView)tabs.getChildAt(i);
            t.setTextColor(i==tab?ColorUtil.ACCENT:ColorUtil.SECONDARY);
        }
    }

    private void buildDialer(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(16,12,16,6);
        dialHint=Ui.text(this,"הקלד מספר או שם",14,ColorUtil.SECONDARY);dialHint.setGravity(android.view.Gravity.CENTER);
        box.addView(dialHint,new LinearLayout.LayoutParams(-1,28));
        dialNumber=Ui.text(this,"",32,ColorUtil.TEXT);dialNumber.setGravity(android.view.Gravity.CENTER);dialNumber.setTextDirection(View.TEXT_DIRECTION_LTR);
        dialNumber.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
        box.addView(dialNumber,new LinearLayout.LayoutParams(-1,60));
        list=new ListView(this);list.setDivider(null);list.setSelector(R.drawable.row_selector);list.setItemsCanFocus(false);list.setFocusable(true);list.setFocusableInTouchMode(false);
        box.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        content.addView(box);
        updateDialSuggestions();
        dialNumber.requestFocus();
        centerAction.setText("חייג");
        softRight.setText("חזרה");
    }

    private void buildContacts(){
        list=new ListView(this);list.setDivider(null);list.setSelector(R.drawable.row_selector);list.setItemsCanFocus(false);list.setChoiceMode(ListView.CHOICE_MODE_NONE);
        contactAdapter=new ContactAdapter(this,filteredContacts);list.setAdapter(contactAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){public void onItemClick(AdapterView<?> a,View v,int p,long id){openContact(filteredContacts.get(p));}});
        content.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        applyFilter("");
    }

    private void buildRecents(){
        list=new ListView(this);list.setDivider(null);list.setSelector(R.drawable.row_selector);list.setItemsCanFocus(false);
        callAdapter=new CallLogAdapter(this,callEntries);list.setAdapter(callAdapter);
        content.addView(list,new LinearLayout.LayoutParams(-1,0,1f));
        list.setEmptyView(emptyView("אין שיחות אחרונות"));
        if(callEntries.size()>0) list.setSelection(0);
    }

    private void buildFavorites(){
        grid=new GridView(this);grid.setNumColumns(2);grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);grid.setVerticalSpacing(Ui.dp(this,8));grid.setHorizontalSpacing(Ui.dp(this,8));grid.setPadding(12,10,12,10);grid.setSelector(R.drawable.row_selector);
        favAdapter=new FavoriteAdapter(this,getFavorites());grid.setAdapter(favAdapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener(){public void onItemClick(AdapterView<?>p,View v,int pos,long id){ContactModel c=favAdapter.getItem(pos);if(c!=null)call(c.number);}});
        content.addView(grid,new LinearLayout.LayoutParams(-1,0,1f));
        centerAction.setText("חייג");
        softRight.setText("חזרה");
    }

    private View emptyView(String s){
        TextView t=Ui.text(this,s,20,ColorUtil.SECONDARY);t.setGravity(android.view.Gravity.CENTER);return t;
    }

    private ArrayList<ContactModel> getFavorites(){
        ArrayList<ContactModel> f=new ArrayList<ContactModel>();
        int max=getSharedPreferences("keycontacts",0).getBoolean("huge",false)?6:12;
        for(ContactModel c:allContacts)if(c.favorite&&c.number.length()>0){f.add(c);if(f.size()==max)break;}
        return f;
    }

    private void loadCallLog(){
        new AsyncTask<Void,Void,List<CallLogRepository.Entry>>(){
            protected List<CallLogRepository.Entry> doInBackground(Void...v){return CallLogRepository.load(MainActivity.this);}
            protected void onPostExecute(List<CallLogRepository.Entry> r){callEntries.clear();if(r!=null)callEntries.addAll(r);if(callAdapter!=null)callAdapter.setData(callEntries);}
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void registerContactsObserver(){
        contactsObserver=new android.database.ContentObserver(new Handler()){
            public void onChange(boolean self){handler.removeCallbacks(refreshContacts);handler.postDelayed(refreshContacts,500);}
        };
        getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,contactsObserver);
    }

    @Override protected void onDestroy(){
        if(contactsObserver!=null)getContentResolver().unregisterContentObserver(contactsObserver);
        handler.removeCallbacks(refreshContacts);
        super.onDestroy();
    }

    @Override public Loader<Cursor> onCreateLoader(int id,Bundle args){
        return new CursorLoader(this,ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone._ID,ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.STARRED},null,null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" COLLATE LOCALIZED ASC");
    }

    @Override public void onLoadFinished(Loader<Cursor> l,Cursor c){
        if(c==null)return;
        ArrayList<ContactModel> next=new ArrayList<ContactModel>();
        long last=Long.MIN_VALUE;
        while(c.moveToNext()){
            long id=c.getLong(1);if(id==last)continue;last=id;
            String name=c.getString(2),num=c.getString(3);int type=c.getInt(4);
            String label=type==ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE?"נייד":
                    (type==ContactsContract.CommonDataKinds.Phone.TYPE_HOME?"בית":
                    (type==ContactsContract.CommonDataKinds.Phone.TYPE_WORK?"עבודה":"טלפון"));
            ContactModel cm=new ContactModel(id,-1,name,num,label,c.getString(5),c.getInt(6)!=0);
            cm.t9Name=T9Matcher.buildT9(cm.name,hebrewT9);
            next.add(cm);
        }
        allContacts.clear();allContacts.addAll(next);
        sortContacts();
        if(tab==TAB_CONTACTS)applyFilter(searchMode?searchDigits:"");
        if(tab==TAB_FAVS&&favAdapter!=null)favAdapter.setData(getFavorites());
        updateDialSuggestions();
    }

    @Override public void onLoaderReset(Loader<Cursor> l){}

    private void sortContacts(){
        final boolean family=getSharedPreferences("keycontacts",0).getBoolean("sort_family",false);
        Collections.sort(allContacts,new Comparator<ContactModel>(){
            public int compare(ContactModel a,ContactModel b){
                String aa=a.name==null?"":a.name,bb=b.name==null?"":b.name;
                if(family){aa=familyKey(aa);bb=familyKey(bb);}
                return java.text.Collator.getInstance(new Locale("he","IL")).compare(aa,bb);
            }
        });
    }

    private String familyKey(String n){
        String[] p=n.trim().split("\\s+");return p.length>1?p[p.length-1]+" "+n:n;
    }

    private void applyFilter(final String q){
        searchDigits=q==null?"":q;
        final int wanted= list==null?0:list.getSelectedItemPosition();
        new AsyncTask<Void,Void,ArrayList<ContactModel>>(){
            protected ArrayList<ContactModel> doInBackground(Void...v){
                ArrayList<ContactModel> r=new ArrayList<ContactModel>();
                for(ContactModel c:allContacts)if(T9Matcher.matches(c,searchDigits,hebrewT9))r.add(c);
                return r;
            }
            protected void onPostExecute(ArrayList<ContactModel> r){
                filteredContacts.clear();filteredContacts.addAll(r);
                if(contactAdapter!=null){contactAdapter.actionMode=0;contactAdapter.setData(filteredContacts);}
                if(list!=null&&r.size()>0)list.setSelection(Math.min(Math.max(0,wanted),r.size()-1));
                updateTopSearch();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateTopSearch(){
        if(tab==TAB_CONTACTS&&searchMode)title.setText("חיפוש  "+searchDigits);
        else title.setText(new String[]{"חיוג","אנשי קשר","אחרונות","מועדפים"}[tab]);
    }

    private void searchAction(){
        if(tab!=TAB_CONTACTS)return;
        searchMode=true;searchDigits="";updateTopSearch();
        Toast.makeText(this,"הקלד ספרות לחיפוש T9; Back מוחק",Toast.LENGTH_SHORT).show();
    }

    private void updateDialSuggestions(){
        if(tab!=TAB_DIAL||list==null)return;
        final ArrayList<ContactModel> suggestions=new ArrayList<ContactModel>();
        String q=dialDigits;
        if(q.length()>0){
            for(ContactModel c:allContacts){
                if(T9Matcher.matches(c,q,hebrewT9)){suggestions.add(c);if(suggestions.size()==3)break;}
            }
        }
        list.setAdapter(new SuggestionAdapter(this,suggestions));
    }

    private class SuggestionAdapter extends BaseAdapter{
        private Context c;private List<ContactModel>d;
        SuggestionAdapter(Context c,List<ContactModel>d){this.c=c;this.d=d;}
        public int getCount(){return d.size();}
        public ContactModel getItem(int p){return d.get(p);}
        public long getItemId(int p){return d.get(p).id;}
        public View getView(int p,View v,ViewGroup parent){
            FocusableRow r=new FocusableRow(c);
            r.setGravity(android.view.Gravity.CENTER_VERTICAL);
            TextView n=Ui.text(c,d.get(p).name,18,ColorUtil.TEXT);
            TextView num=Ui.text(c,PhoneFormatter.format(d.get(p).number),14,ColorUtil.SECONDARY);
            LinearLayout box=new LinearLayout(c);box.setOrientation(LinearLayout.VERTICAL);box.addView(n,new LinearLayout.LayoutParams(-1,34));box.addView(num,new LinearLayout.LayoutParams(-1,26));
            r.addView(box,new LinearLayout.LayoutParams(0,-1,1f));r.addView(Ui.text(c,"☎",23,ColorUtil.GREEN),new LinearLayout.LayoutParams(48,-1));
            return r;
        }
    }

    private void dialDigit(int d){
        if(redialArmed){dialDigits="";redialArmed=false;}
        dialDigits+=String.valueOf(d);
        dialNumber.setText(PhoneFormatter.format(dialDigits));
        updateDialSuggestions();
    }

    private void deleteDial(){
        if(dialDigits.length()>0){dialDigits=dialDigits.substring(0,dialDigits.length()-1);redrawDial();}
        else if(redialArmed){redialArmed=false;redrawDial();}
        else finish();
    }

    private void clearDial(){dialDigits="";redialArmed=false;redrawDial();}
    private void redrawDial(){
        if(dialNumber!=null)dialNumber.setText(PhoneFormatter.format(dialDigits));
        updateDialSuggestions();
    }

    private void callCurrent(){
        if(tab==TAB_DIAL){
            if(dialDigits.length()==0){
                if(lastDialed.length()>0&&!redialArmed){dialDigits=lastDialed;redialArmed=true;dialNumber.setText(PhoneFormatter.format(dialDigits));dialHint.setText("לחץ CALL שוב לאישור חיוג חוזר");return;}
                if(redialArmed){call(lastDialed);redialArmed=false;return;}
                return;
            }
            lastDialed=dialDigits;call(dialDigits);return;
        }
        if(tab==TAB_CONTACTS&&list!=null&&list.getSelectedItemPosition()>=0&&list.getSelectedItemPosition()<filteredContacts.size()){call(filteredContacts.get(list.getSelectedItemPosition()).number);return;}
        if(tab==TAB_RECENTS&&list!=null&&list.getSelectedItemPosition()>=0&&list.getSelectedItemPosition()<callEntries.size()){call(callEntries.get(list.getSelectedItemPosition()).number);return;}
        if(tab==TAB_FAVS&&grid!=null&&grid.getSelectedItemPosition()>=0&&grid.getSelectedItemPosition()<favAdapter.getCount()){call(favAdapter.getItem(grid.getSelectedItemPosition()).number);}
    }

    private void call(String number){
        if(number==null||number.length()==0)return;
        try{
            lastDialed=number;
            Intent i=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(number)));
            startActivity(i);
            Intent in=new Intent(this,InCallActivity.class);in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);in.putExtra("number",number);in.putExtra("outgoing",true);startActivity(in);
        }catch(SecurityException ex){Toast.makeText(this,"נדרשת הרשאת חיוג",Toast.LENGTH_LONG).show();}
    }

    private void openContact(ContactModel c){
        if(c==null)return;
        Intent i=new Intent(this,ContactDetailActivity.class);i.putExtra("contactId",c.id);i.putExtra("number",c.number);i.putExtra("name",c.name);startActivity(i);
    }

    private void menuAction(){
        if(tab==TAB_CONTACTS&&list!=null&&list.getSelectedItemPosition()>=0&&list.getSelectedItemPosition()<filteredContacts.size()){
            final ContactModel c=filteredContacts.get(list.getSelectedItemPosition());
            DialogUtil.actions(this,c.name.length()>0?c.name:"איש קשר",
                    new String[]{"חייג","שלח הודעה","ערוך","מחק","מועדף","שתף"},
                    new DialogUtil.Choice(){public void onChoice(int w){
                        if(w==0)call(c.number);else if(w==1)sms(c.number);else if(w==2)edit(c);else if(w==3)delete(c);else if(w==4)toggleFavorite(c);else if(w==5)share(c);
                    }});
        } else if(tab==TAB_RECENTS) Toast.makeText(this,"אפשרויות: מחיקה / חסימה / הוספה לאנשי קשר במסך הפרטים",Toast.LENGTH_SHORT).show();
        else if(tab==TAB_DIAL) settings();
    }

    private void sms(String number){
        if(number==null)return;
        try{startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:"+Uri.encode(number))));}catch(Exception e){Toast.makeText(this,"SMS לא זמין",Toast.LENGTH_SHORT).show();}
    }
    private void edit(ContactModel c){startActivity(new Intent(this,EditContactActivity.class).putExtra("contactId",c.id).putExtra("name",c.name).putExtra("number",c.number));}
    private void delete(final ContactModel c){
        try{int n=getContentResolver().delete(ContactsContract.Contacts.CONTENT_URI,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(c.id)});Toast.makeText(this,n>0?"נמחק":"לא ניתן למחוק",Toast.LENGTH_SHORT).show();getLoaderManager().restartLoader(101,null,this);}catch(Exception e){Toast.makeText(this,"מחיקה נכשלה",Toast.LENGTH_SHORT).show();}
    }
    private void toggleFavorite(ContactModel c){
        android.content.ContentValues v=new android.content.ContentValues();v.put(ContactsContract.Contacts.STARRED,c.favorite?0:1);
        getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,v,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(c.id)});
        c.favorite=!c.favorite;if(favAdapter!=null)favAdapter.setData(getFavorites());
    }
    private void share(ContactModel c){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,c.name+"  "+c.number);startActivity(Intent.createChooser(i,"שתף איש קשר"));
    }

    private void settings(){startActivity(new Intent(this,SettingsActivity.class));}

    private void moveSelection(int delta){
        if(tab==TAB_CONTACTS&&list!=null){
            int n=filteredContacts.size();if(n==0)return;int p=list.getSelectedItemPosition();if(p<0)p=0;int next=p+delta;boolean circ=getSharedPreferences("keycontacts",0).getBoolean("circular",false);
            if(next<0)next=circ?n-1:0;if(next>=n)next=circ?0:n-1;list.setSelectionFromTop(next,Math.max(0,list.getHeight()/2-36));contactAdapter.actionMode=0;list.invalidateViews();return;
        }
        if(tab==TAB_RECENTS&&list!=null&&callEntries.size()>0){int n=callEntries.size(),p=Math.max(0,list.getSelectedItemPosition());int next=Math.max(0,Math.min(n-1,p+delta));list.setSelectionFromTop(next,Math.max(0,list.getHeight()/2-36));}
        else if(tab==TAB_DIAL&&list!=null&&list.getCount()>0){int n=list.getCount(),p=Math.max(0,list.getSelectedItemPosition());list.setSelectionFromTop(Math.max(0,Math.min(n-1,p+delta)),Math.max(0,list.getHeight()/2-36));}
    }

    private void moveTab(int delta){
        int n=4;int x=tab+delta;if(x<0)x=n-1;if(x>=n)x=0;switchTab(x);
    }

    private void contextSelect(){
        if(tab==TAB_CONTACTS&&contactAdapter!=null){
            int p=list.getSelectedItemPosition();if(p<0||p>=filteredContacts.size())return;
            if(contactAdapter.actionMode==1)call(filteredContacts.get(p).number);
            else if(contactAdapter.actionMode==2)sms(filteredContacts.get(p).number);
            else if(contactAdapter.actionMode==3)openContact(filteredContacts.get(p));
            else openContact(filteredContacts.get(p));
        } else if(tab==TAB_DIAL&&list!=null&&list.getSelectedItemPosition()>=0){
            call(suggestionAt(list.getSelectedItemPosition()));
        } else if(tab==TAB_RECENTS&&list!=null&&callEntries.size()>0)callCurrent();
        else if(tab==TAB_FAVS&&grid!=null&&favAdapter!=null&&grid.getSelectedItemPosition()>=0)call(favAdapter.getItem(grid.getSelectedItemPosition()).number);
    }

    private String suggestionAt(int p){SuggestionAdapter a=(SuggestionAdapter)list.getAdapter();return a.getItem(p).number;}

    private void menuCycle(int dir){
        if(tab==TAB_CONTACTS&&contactAdapter!=null){
            int m=contactAdapter.actionMode+dir;if(m<0)m=3;if(m>3)m=0;contactAdapter.actionMode=m;list.invalidateViews();
        } else moveTab(dir);
    }

    private void inputDigit(int d){
        if(tab==TAB_CONTACTS){searchMode=true;applyFilter(searchDigits+String.valueOf(d));return;}
        if(tab==TAB_DIAL){dialDigit(d);return;}
        if(tab==TAB_RECENTS||tab==TAB_FAVS){return;}
    }

    private void longDigit(int d){
        if(d==0){if(tab==TAB_DIAL){dialDigits+="+";redrawDial();}return;}
        if(d==1){String v=getSharedPreferences("keycontacts",0).getString("voicemail","*86");call(v);return;}
        if(d>=2&&d<=9){
            String v=getSharedPreferences("keycontacts",0).getString("speed_"+d,"");
            if(v.length()>0)call(v);else Toast.makeText(this,"חיוג מהיר "+d+" לא הוגדר",Toast.LENGTH_SHORT).show();
        }
    }

    private void longStar(){hebrewT9=!hebrewT9;getSharedPreferences("keycontacts",0).edit().putBoolean("t9_hebrew",hebrewT9).apply();applyFilter(searchDigits);Toast.makeText(this,hebrewT9?"T9 עברית":"T9 אנגלית",Toast.LENGTH_SHORT).show();}
    private void longPound(){android.media.AudioManager am=(android.media.AudioManager)getSystemService(AUDIO_SERVICE);int m=am.getRingerMode();am.setRingerMode(m==android.media.AudioManager.RINGER_MODE_SILENT?android.media.AudioManager.RINGER_MODE_NORMAL:android.media.AudioManager.RINGER_MODE_SILENT);}

    @Override public boolean dispatchKeyEvent(KeyEvent e){
        int k=e.getKeyCode();
        boolean relevant=(k==KeyEvent.KEYCODE_DPAD_UP||k==KeyEvent.KEYCODE_DPAD_DOWN||k==KeyEvent.KEYCODE_DPAD_LEFT||k==KeyEvent.KEYCODE_DPAD_RIGHT||
                k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER||k==KeyEvent.KEYCODE_CALL||k==KeyEvent.KEYCODE_ENDCALL||
                k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_MENU||k==KeyEvent.KEYCODE_SOFT_LEFT||k==KeyEvent.KEYCODE_SOFT_RIGHT||
                k==KeyEvent.KEYCODE_STAR||k==KeyEvent.KEYCODE_POUND||(k>=KeyEvent.KEYCODE_0&&k<=KeyEvent.KEYCODE_9)||
                k==KeyEvent.KEYCODE_DEL||k==KeyEvent.KEYCODE_VOLUME_UP||k==KeyEvent.KEYCODE_VOLUME_DOWN);
        if(!relevant)return super.dispatchKeyEvent(e);
        if(e.getAction()==KeyEvent.ACTION_DOWN){
            e.startTracking();downKey=k;
            if(e.getRepeatCount()>0||(e.getFlags()&KeyEvent.FLAG_LONG_PRESS)!=0){longHandled=true;doLong(k);return true;}
            longHandled=false;return true;
        }
        if(e.getAction()==KeyEvent.ACTION_UP){
            if(!longHandled)doShort(k);
            downKey=-1;return true;
        }
        return true;
    }

    private void doLong(int k){
        if(k>=KeyEvent.KEYCODE_0&&k<=KeyEvent.KEYCODE_9)longDigit(k-KeyEvent.KEYCODE_0);
        else if(k==KeyEvent.KEYCODE_STAR)longStar();
        else if(k==KeyEvent.KEYCODE_POUND)longPound();
        else if(k==KeyEvent.KEYCODE_DEL||k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_ENDCALL){if(tab==TAB_DIAL)clearDial();else if(tab==TAB_CONTACTS&&searchMode){searchDigits="";applyFilter("");}}
    }

    private void doShort(int k){
        switch(k){
            case KeyEvent.KEYCODE_DPAD_UP:moveSelection(-1);break;
            case KeyEvent.KEYCODE_DPAD_DOWN:moveSelection(1);break;
            case KeyEvent.KEYCODE_DPAD_LEFT:menuCycle(-1);break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:menuCycle(1);break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:contextSelect();break;
            case KeyEvent.KEYCODE_CALL:callCurrent();break;
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_SOFT_LEFT:menuAction();break;
            case KeyEvent.KEYCODE_SOFT_RIGHT:searchAction();break;
            case KeyEvent.KEYCODE_STAR:
            case KeyEvent.KEYCODE_POUND:
                if(tab==TAB_CONTACTS){if(k==KeyEvent.KEYCODE_POUND)jumpLetter(1);else jumpLetter(-1);}break;
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ENDCALL:
                if(tab==TAB_DIAL&&(dialDigits.length()>0||redialArmed))deleteDial();
                else if(tab==TAB_CONTACTS&&searchMode&&searchDigits.length()>0){applyFilter(searchDigits.substring(0,searchDigits.length()-1));}
                else finish();break;
            case KeyEvent.KEYCODE_VOLUME_UP:moveSelection(-5);break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:moveSelection(5);break;
            default:if(k>=KeyEvent.KEYCODE_0&&k<=KeyEvent.KEYCODE_9)inputDigit(k-KeyEvent.KEYCODE_0);
        }
    }

    private void jumpLetter(int dir){
        if(filteredContacts.size()==0)return;
        int cur=Math.max(0,list.getSelectedItemPosition());
        for(int n=1;n<=filteredContacts.size();n++){
            int p=(cur+dir*n)%filteredContacts.size();if(p<0)p+=filteredContacts.size();
            String a=filteredContacts.get(p).name;if(a.length()>0&&a.substring(0,1).matches("[א-תA-Za-z]")){list.setSelection(p);return;}
        }
    }
}
