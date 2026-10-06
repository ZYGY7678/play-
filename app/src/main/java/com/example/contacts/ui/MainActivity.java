package com.example.contacts.ui;

import android.content.CursorLoader;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.view.Gravity;
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

import com.example.contacts.data.CallLogRepository;
import com.example.contacts.data.ContactModel;
import com.example.contacts.data.ContactsRepository;
import com.example.contacts.keys.KeyMapper;
import com.example.contacts.keys.T9Matcher;
import com.example.contacts.util.AppPrefs;
import com.example.contacts.util.Palette;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.widget.FocusableRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseKeyActivity implements LoaderManager.LoaderCallbacks<Cursor> {
 private static final int DIAL=0,CONTACTS=1,RECENTS=2,FAVS=3;
 private int tab=DIAL;private boolean contactSearch=false;private String query="";private String dial="";private String lastDialed="";
 private LinearLayout root,body,tabRow;private TextView title,softLeft,center,softRight,searchBar;private ListView list;private GridView grid;private ContactAdapter contactsAdapter;private CallLogAdapter recentAdapter;private FavoriteAdapter favAdapter;
 private final ArrayList<ContactModel> contacts=new ArrayList<ContactModel>();private final ArrayList<ContactModel> filtered=new ArrayList<ContactModel>();private final ArrayList<ContactAdapter.Row> contactRows=new ArrayList<ContactAdapter.Row>();private final ArrayList<CallLogRepository.Entry> recents=new ArrayList<CallLogRepository.Entry>();
 private int selected=0;private final Handler h=new Handler();private ContentObserver observer;

 @Override protected void onCreate(Bundle b){
  super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  build();getLoaderManager().initLoader(77,null,this);loadRecents();registerObserver();
 }
 private void build(){
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Palette.bg(this));
  root.addView(buildHeader(),new LinearLayout.LayoutParams(-1,72));
  body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);root.addView(body,new LinearLayout.LayoutParams(-1,0,1));
  root.addView(buildBottom(),new LinearLayout.LayoutParams(-1,50));setContentView(root);switchTab(DIAL);tabRow.getChildAt(0).requestFocus();
 }
 private View buildHeader(){
  LinearLayout outer=new LinearLayout(this);outer.setOrientation(LinearLayout.VERTICAL);outer.setBackgroundColor(Palette.bar(this));
  LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);
  TextView left=topText("‹"),right=topText("›");title=topText("חיוג");title.setTextSize(22*Palette.scale(this));line.addView(left,new LinearLayout.LayoutParams(48,44));line.addView(title,new LinearLayout.LayoutParams(0,44,1));line.addView(right,new LinearLayout.LayoutParams(48,44));
  left.setOnClickListener(new View.OnClickListener(){public void onClick(View v){moveTab(-1);}});right.setOnClickListener(new View.OnClickListener(){public void onClick(View v){moveTab(1);}});
  outer.addView(line,new LinearLayout.LayoutParams(-1,44));
  tabRow=new LinearLayout(this);tabRow.setGravity(Gravity.CENTER);
  final String[] names={"חיוג","אנשי קשר","אחרונות","מועדפים"};
  for(int i=0;i<4;i++){final int x=i;TextView t=topText(names[i]);t.setTextSize(14);t.setContentDescription(names[i]);t.setOnClickListener(new View.OnClickListener(){public void onClick(View v){switchTab(x);}});tabRow.addView(t,new LinearLayout.LayoutParams(0,28,1));}
  outer.addView(tabRow,new LinearLayout.LayoutParams(-1,28));return outer;
 }
 private TextView topText(String s){TextView t=new TextView(this);t.setText(s);t.setTextColor(Palette.text(this));t.setGravity(Gravity.CENTER);t.setFocusable(true);t.setFocusableInTouchMode(false);t.setBackgroundResource(com.example.contacts.R.drawable.row_selector);return t;}
 private View buildBottom(){
  LinearLayout b=new LinearLayout(this);b.setGravity(Gravity.CENTER_VERTICAL);b.setBackgroundColor(Palette.bar(this));
  softLeft=topText("אפשרויות");center=topText("OK");softRight=topText("חזרה / חפש");b.addView(softLeft,new LinearLayout.LayoutParams(0,50,1));b.addView(center,new LinearLayout.LayoutParams(0,50,1));b.addView(softRight,new LinearLayout.LayoutParams(0,50,1));
  softLeft.setOnClickListener(new View.OnClickListener(){public void onClick(View v){softLeft();}});center.setOnClickListener(new View.OnClickListener(){public void onClick(View v){select();}});softRight.setOnClickListener(new View.OnClickListener(){public void onClick(View v){softRight();}});return b;
 }
 private void switchTab(int t){
  tab=t;selected=0;contactSearch=false;query="";body.removeAllViews();title.setText(new String[]{"חיוג","אנשי קשר","אחרונות","מועדפים"}[tab]);
  if(tab==DIAL)buildDialer();else if(tab==CONTACTS)buildContacts();else if(tab==RECENTS)buildRecents();else buildFavs();
  for(int i=0;i<4;i++){TextView v=(TextView)tabRow.getChildAt(i);v.setTextColor(i==tab?Palette.accent(this):Palette.secondary(this));}
 }
 private void buildDialer(){
  FrameLayout f=new FrameLayout(this);
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(16,12,16,8);
  searchBar=makeInfo("הקלד מספר או שם",13);box.addView(searchBar,new LinearLayout.LayoutParams(-1,30));
  final TextView number=makeInfo("0",32);number.setTag("dial");number.setText("");number.setGravity(Gravity.CENTER);number.setTextDirection(View.TEXT_DIRECTION_LTR);box.addView(number,new LinearLayout.LayoutParams(-1,70));
  list=new ListView(this);list.setDivider(null);list.setSelector(com.example.contacts.R.drawable.row_selector);list.setItemsCanFocus(true);
  box.addView(list,new LinearLayout.LayoutParams(-1,0,1));f.addView(box,new FrameLayout.LayoutParams(-1,-1));body.addView(f);softRight.setText("חזרה");center.setText("חייג");softLeft.setText("אפשרויות");updateDialSuggestions();number.requestFocus();
 }
 private void buildContacts(){
  FrameLayout f=new FrameLayout(this);
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);searchBar=makeInfo("",15);searchBar.setVisibility(View.GONE);searchBar.setGravity(Gravity.CENTER|Gravity.RIGHT);searchBar.setTextColor(Palette.accent(this));box.addView(searchBar,new LinearLayout.LayoutParams(-1,38));
  list=new ListView(this);list.setDivider(null);list.setSelector(com.example.contacts.R.drawable.row_selector);list.setDrawSelectorOnTop(false);list.setChoiceMode(ListView.CHOICE_MODE_NONE);list.setItemsCanFocus(true);contactsAdapter=new ContactAdapter(this,contactRows);list.setAdapter(contactsAdapter);
  list.setOnItemClickListener(new AdapterView.OnItemClickListener(){public void onItemClick(AdapterView<?> a,View v,int p,long id){if(p>=0&&p<contactRows.size()&&contactRows.get(p).kind==1)open(contactRows.get(p).contact);}});
  box.addView(list,new LinearLayout.LayoutParams(-1,0,1));f.addView(box,new FrameLayout.LayoutParams(-1,-1));
  TextView alpha=makeInfo("א\nב\nג\nד\nה\nו\nז\nח\nט\nי\nכ\nל\nמ\nנ\nס\nע\nפ\nצ\nק\nר\nש\nת",9);alpha.setTextColor(Palette.secondary(this));alpha.setGravity(Gravity.CENTER);alpha.setFocusable(false);alpha.setClickable(false);
  FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(24,-1,Gravity.RIGHT);ap.topMargin=42;ap.bottomMargin=44;f.addView(alpha,ap);body.addView(f);
  softRight.setText("חפש");softLeft.setText("אפשרויות");center.setText("פתיחה");rebuildContactRows();list.requestFocus();
 }
 private void buildRecents(){
  list=new ListView(this);list.setDivider(null);list.setSelector(com.example.contacts.R.drawable.row_selector);list.setItemsCanFocus(true);recentAdapter=new CallLogAdapter(this,recents);list.setAdapter(recentAdapter);body.addView(list,new LinearLayout.LayoutParams(-1,0,1));softRight.setText("חזרה");softLeft.setText("אפשרויות");center.setText("פרטים");list.requestFocus();
 }
 private void buildFavs(){
  grid=new GridView(this);grid.setNumColumns(2);grid.setPadding(12,12,12,12);grid.setVerticalSpacing(8);grid.setHorizontalSpacing(8);grid.setSelector(com.example.contacts.R.drawable.row_selector);favAdapter=new FavoriteAdapter(this,getFavorites());grid.setAdapter(favAdapter);body.addView(grid,new LinearLayout.LayoutParams(-1,0,1));softRight.setText("חזרה");softLeft.setText("אפשרויות");center.setText("חייג");grid.requestFocus();
 }
 private TextView makeInfo(String s,float sp){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp*Palette.scale(this));t.setTextColor(Palette.text(this));t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 private void rebuildContactRows(){
  contactRows.clear();String last="";for(ContactModel c:filtered){String h=c.name.trim().length()==0?"#":c.name.trim().substring(0,1).toUpperCase(Locale.getDefault());if(!h.equals(last)){last=h;contactRows.add(new ContactAdapter.Row(h));}contactRows.add(new ContactAdapter.Row(c));}
  if(contactsAdapter!=null){contactsAdapter.actionMode=0;contactsAdapter.setRows(contactRows);}
  if(searchBar!=null){searchBar.setVisibility(contactSearch?View.VISIBLE:View.GONE);searchBar.setText("חיפוש: "+query);}
 }
 private void applyFilter(){
  final String q=query;final boolean he=AppPrefs.bool(this,"t9_hebrew",true);
  new AsyncTask<Void,Void,ArrayList<ContactModel>>(){protected ArrayList<ContactModel> doInBackground(Void...v){ArrayList<ContactModel> x=new ArrayList<ContactModel>();for(ContactModel c:contacts)if(T9Matcher.matches(c,q,he))x.add(c);return x;}
   protected void onPostExecute(ArrayList<ContactModel>x){filtered.clear();filtered.addAll(x);rebuildContactRows();if(list!=null&&x.size()>0)list.setSelection(0);if(q.length()>0&&x.size()==0)Toast.makeText(MainActivity.this,"לא נמצאו תוצאות עבור "+q,Toast.LENGTH_SHORT).show();}
  }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 }
 private void updateDialSuggestions(){if(list==null)return;final ArrayList<ContactModel> s=new ArrayList<ContactModel>();if(dial.length()>0){for(ContactModel c:contacts){if(T9Matcher.matches(c,dial,AppPrefs.bool(this,"t9_hebrew",true))){s.add(c);if(s.size()==3)break;}}}list.setAdapter(new BaseAdapter(){public int getCount(){return s.size();}public Object getItem(int p){return s.get(p);}public long getItemId(int p){return s.get(p).id;}public View getView(int p,View v,ViewGroup par){FocusableRow r=new FocusableRow(MainActivity.this);LinearLayout b=new LinearLayout(MainActivity.this);b.setOrientation(LinearLayout.VERTICAL);TextView n=makeInfo(s.get(p).name,19);n.setTypeface(null,android.graphics.Typeface.BOLD);TextView q=makeInfo(PhoneFormatter.ltr(PhoneFormatter.format(s.get(p).primary())),14);q.setTextColor(Palette.secondary(MainActivity.this));b.addView(n);b.addView(q);r.addView(b,new LinearLayout.LayoutParams(0,-1,1));return r;}});}
 private ArrayList<ContactModel> getFavorites(){ArrayList<ContactModel>x=new ArrayList<ContactModel>();int max="ענק".equals(AppPrefs.str(this,"font_size","רגיל"))?6:12;for(ContactModel c:contacts)if(c.favorite&&c.primary().length()>0){x.add(c);if(x.size()==max)break;}return x;}
 private void loadRecents(){new AsyncTask<Void,Void,List<CallLogRepository.Entry>>(){protected List<CallLogRepository.Entry>doInBackground(Void...v){return CallLogRepository.load(MainActivity.this);}protected void onPostExecute(List<CallLogRepository.Entry>x){recents.clear();recents.addAll(x);if(recentAdapter!=null)recentAdapter.setData(recents);}}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);}
 private void registerObserver(){observer=new ContentObserver(new Handler()){public void onChange(boolean self){h.removeCallbacks(refresh);h.postDelayed(refresh,350);}};getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,observer);}
 private final Runnable refresh=new Runnable(){public void run(){getLoaderManager().restartLoader(77,null,MainActivity.this);loadRecents();}};
 @Override protected void onDestroy(){if(observer!=null)getContentResolver().unregisterContentObserver(observer);super.onDestroy();}
 @Override public Loader<Cursor> onCreateLoader(int id,Bundle b){String[]p={ContactsContract.CommonDataKinds.Phone._ID,ContactsContract.CommonDataKinds.Phone.CONTACT_ID,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,ContactsContract.CommonDataKinds.Phone.STARRED};return new CursorLoader(this,ContactsContract.CommonDataKinds.Phone.CONTENT_URI,p,null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" COLLATE LOCALIZED ASC");}
 @Override public void onLoadFinished(Loader<Cursor> l,Cursor c){ArrayList<ContactModel> out=new ArrayList<ContactModel>();ContactModel cur=null;long id=-1;while(c!=null&&c.moveToNext()){long cid=c.getLong(1);if(cid!=id){cur=new ContactModel(cid,c.getString(2));cur.photoUri=c.getString(5);cur.favorite=c.getInt(6)!=0;out.add(cur);id=cid;}if(cur!=null)cur.phones.add(new ContactModel.PhoneNumber(c.getString(3),ContactsRepository.phoneType(c.getInt(4))));}contacts.clear();contacts.addAll(out);sortContacts();applyFilter();if(tab==FAVS&&favAdapter!=null)favAdapter.setData(getFavorites());updateDialSuggestions();}
 @Override public void onLoaderReset(Loader<Cursor>l){}
 private void sortContacts(){final boolean fam=AppPrefs.bool(this,"sort_family",false);Collections.sort(contacts,new Comparator<ContactModel>(){public int compare(ContactModel a,ContactModel b){return key(a.name,fam).compareToIgnoreCase(key(b.name,fam));}});}
 private String key(String n,boolean fam){String x=n==null?"":n.trim();if(!fam)return x;String[]p=x.split("\\s+");return p.length>1?p[p.length-1]+" "+x:x;}
 private void moveTab(int d){int x=(tab+d+4)%4;switchTab(x);}
 private boolean inContacts(){return tab==CONTACTS&&list!=null;}
 private int selectedContactRow(){if(!inContacts())return -1;int p=list.getSelectedItemPosition();return p>=0&&p<contactRows.size()&&contactRows.get(p).kind==1?p:-1;}
 private ContactModel selectedContact(){int p=selectedContactRow();return p<0?null:contactRows.get(p).contact;}
 private void moveList(int d){if(tab==FAVS){moveGrid(d);return;}if(list==null)return;int p=list.getSelectedItemPosition();if(p<0)p=0;int n=list.getCount();if(n==0)return;int next=p+d;boolean circ=AppPrefs.bool(this,"circular",false);if(next<0)next=circ?n-1:0;if(next>=n)next=circ?0:n-1;while(next>=0&&next<n&&list.getAdapter()!=null&&list.getAdapter().getItem(next) instanceof ContactAdapter.Row&&((ContactAdapter.Row)list.getAdapter().getItem(next)).kind==0&&next!=p)next+=d;if(next<0||next>=n)next=p;list.setSelectionFromTop(next,Math.max(0,list.getHeight()/2-36));if(tab==CONTACTS&&contactsAdapter!=null){contactsAdapter.actionMode=0;contactsAdapter.notifyDataSetChanged();}}
 private void moveGrid(int d){int p=grid.getSelectedItemPosition();if(p<0)p=0;int n=grid.getCount();if(n==0)return;int cols=2,next=p+d;if(d==1)next=p+1;else if(d==-1)next=p-1;grid.setSelection(Math.max(0,Math.min(n-1,next)));}
 private void select(){if(tab==DIAL){if(list!=null&&list.getSelectedItemPosition()>=0&&list.getSelectedItemPosition()<list.getCount()&&dial.length()==0){Object o=list.getAdapter().getItem(list.getSelectedItemPosition());if(o instanceof ContactModel)callContact((ContactModel)o);else callDial();}else callDial();}
  else if(tab==CONTACTS){ContactModel c=selectedContact();if(c==null)return;if(contactsAdapter.actionMode==1)callContact(c);else if(contactsAdapter.actionMode==2)sendSms(c.primary());else open(c);}
  else if(tab==RECENTS){if(list!=null&&list.getSelectedItemPosition()>=0)showRecentOptions(recents.get(list.getSelectedItemPosition()));}
  else if(tab==FAVS){int p=grid.getSelectedItemPosition();if(p>=0)callContact(favAdapter.getItem(p));}}
 private void callContact(final ContactModel c){if(c==null||c.phones.size()==0){Toast.makeText(this,"לאיש הקשר אין מספר",Toast.LENGTH_SHORT).show();return;}if(c.phones.size()==1){call(c.primary());return;}String[]a=new String[c.phones.size()];for(int i=0;i<a.length;i++)a[i]=c.phones.get(i).type+"  "+PhoneNumberUtils.formatNumber(c.phones.get(i).number);DialogUtil.actions(this,c.name,a,new DialogUtil.Choice(){public void onChoice(int w){call(c.phones.get(w).number);}});}
 private void callDial(){if(dial.length()==0){if(lastDialed.length()>0&& !AppPrefs.bool(this,"redial_armed",false)){dial=lastDialed;AppPrefs.put(this,"redial_armed",true);Toast.makeText(this,"לחץ CALL שוב כדי לחייג שוב",Toast.LENGTH_SHORT).show();updateDialDisplay();}else if(lastDialed.length()>0)call(lastDialed);return;}lastDialed=dial;call(dial);}
 private void call(String n){if(n==null||n.length()==0)return;try{startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(n))));}catch(Exception e){Toast.makeText(this,"אין הרשאת חיוג",Toast.LENGTH_LONG).show();}}
 private void sendSms(String n){try{startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:"+Uri.encode(n))));}catch(Exception e){Toast.makeText(this,"SMS לא זמין",Toast.LENGTH_SHORT).show();}}
 private void open(ContactModel c){startActivity(new Intent(this,ContactDetailActivity.class).putExtra("contactId",c.id).putExtra("name",c.name));}
 private void updateDialDisplay(){TextView n=(TextView)body.findViewWithTag("dial");if(n!=null)n.setText(PhoneNumberUtils.formatNumber(dial)==null?dial:PhoneNumberUtils.formatNumber(dial));updateDialSuggestions();}
 private void softLeft(){if(tab==CONTACTS){final ContactModel c=selectedContact();if(c!=null)contactMenu(c);else DialogUtil.actions(this,"אנשי קשר",new String[]{"הוסף איש קשר","חפש","הגדרות"},new DialogUtil.Choice(){public void onChoice(int w){if(w==0)add();else if(w==1)startSearch();else settings();}});}else if(tab==RECENTS){if(list!=null&&list.getSelectedItemPosition()>=0)showRecentOptions(recents.get(list.getSelectedItemPosition()));else settings();}else if(tab==DIAL)settings();else settings();}
 private void contactMenu(final ContactModel c){DialogUtil.actions(this,c.name,new String[]{"חייג","שלח הודעה","ערוך","מחק","מועדף","שתף"},new DialogUtil.Choice(){public void onChoice(int w){if(w==0)callContact(c);else if(w==1)sendSms(c.primary());else if(w==2)edit(c);else if(w==3)delete(c);else if(w==4)favorite(c);else share(c);}});}
 private void showRecentOptions(final CallLogRepository.Entry e){DialogUtil.actions(this,e.name.length()>0?e.name:e.number,new String[]{"חייג","הוסף לאנשי קשר","מחק מהרשימה","חסום מספר","פרטים"},new DialogUtil.Choice(){public void onChoice(int w){if(w==0)call(e.number);else if(w==1)startActivity(new Intent(MainActivity.this,EditContactActivity.class).putExtra("number",e.number));else if(w==2)deleteRecent(e);else if(w==3)block(e.number);else recentInfo(e);}});}
 private void recentInfo(CallLogRepository.Entry e){Toast.makeText(this,(e.name.length()>0?e.name+" ":"")+e.number+" • "+e.count+" שיחות • "+com.example.contacts.util.TimeUtil.relative(e.date),Toast.LENGTH_LONG).show();}
 private void deleteRecent(CallLogRepository.Entry e){getContentResolver().delete(CallLog.Calls.CONTENT_URI,CallLog.Calls.NUMBER+"=?",new String[]{e.number});loadRecents();}
 private void block(String n){String old=AppPrefs.str(this,"blocked","");if(old.indexOf("|"+PhoneFormatter.last9(n)+"|")<0)AppPrefs.put(this,"blocked",old+"|"+PhoneFormatter.last9(n)+"|");Toast.makeText(this,"המספר נחסם בתוך האפליקציה",Toast.LENGTH_SHORT).show();}
 private void favorite(final ContactModel c){ContentValues v=new ContentValues();v.put(ContactsContract.Contacts.STARRED,c.favorite?0:1);getContentResolver().update(ContactsContract.Contacts.CONTENT_URI,v,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(c.id)});c.favorite=!c.favorite;if(favAdapter!=null)favAdapter.setData(getFavorites());}
 private void delete(final ContactModel c){DialogUtil.actions(this,"מחיקת "+c.name,new String[]{"מחק","ביטול"},new DialogUtil.Choice(){public void onChoice(int w){if(w==0){getContentResolver().delete(ContactsContract.Contacts.CONTENT_URI,ContactsContract.Contacts._ID+"=?",new String[]{String.valueOf(c.id)});getLoaderManager().restartLoader(77,null,MainActivity.this);}}});}
 private void share(ContactModel c){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,c.name+"\n"+c.primary());startActivity(Intent.createChooser(i,"שתף איש קשר"));}
 private void edit(ContactModel c){startActivity(new Intent(this,EditContactActivity.class).putExtra("contactId",c.id).putExtra("name",c.name).putExtra("number",c.primary()));}
 private void add(){startActivity(new Intent(this,EditContactActivity.class));}
 private void settings(){startActivity(new Intent(this,SettingsActivity.class));}
 private void startSearch(){if(tab!=CONTACTS)return;contactSearch=true;query="";rebuildContactRows();softRight.setText("מחק");}
 private void softRight(){if(tab==CONTACTS){if(!contactSearch)startSearch();else if(query.length()>0){query=query.substring(0,query.length()-1);applyFilter();}else {contactSearch=false;rebuildContactRows();softRight.setText("חפש");}}else if(tab==DIAL){if(dial.length()>0){dial=dial.substring(0,dial.length()-1);updateDialDisplay();}else finish();}else finish();}
 private void inputDigit(int d){if(tab==CONTACTS){contactSearch=true;query+=d;applyFilter();return;}if(tab==DIAL){dial+=d;AppPrefs.put(this,"redial_armed",false);updateDialDisplay();}}
 private void longDigit(int d){if(d==0){if(tab==DIAL){dial+="+";updateDialDisplay();}return;}if(d==1){call(AppPrefs.str(this,"voicemail","*86"));return;}String n=AppPrefs.str(this,"speed_"+d,"");if(d>=2&&d<=9){if(n.length()>0)call(n);else Toast.makeText(this,"חיוג מהיר "+d+" לא הוגדר",Toast.LENGTH_SHORT).show();}}
 private void onStar(){if(tab==CONTACTS)jumpLetter(-1);}
 private void onPound(){if(tab==CONTACTS)jumpLetter(1);}
 private void jumpLetter(int d){if(filtered.size()==0)return;int p=Math.max(0,list.getSelectedItemPosition());for(int i=1;i<=filtered.size();i++){int j=(p+d*i)%filtered.size();if(j<0)j+=filtered.size();String n=filtered.get(j).name;if(n.length()>0&&n.substring(0,1).matches("[א-תA-Za-z]")){for(int k=0;k<contactRows.size();k++)if(contactRows.get(k).kind==1&&contactRows.get(k).contact==filtered.get(j)){list.setSelection(k);showLetter(n.substring(0,1));return;}}}}
 private void showLetter(final String s){final TextView b=makeInfo(s,42);b.setTextColor(Palette.accent(this));b.setGravity(Gravity.CENTER);b.setBackgroundColor(Palette.bar(this));FrameLayout decor=(FrameLayout)findViewById(android.R.id.content);if(decor==null)return;FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(130,100,Gravity.CENTER);decor.addView(b,p);h.postDelayed(new Runnable(){public void run(){((ViewGroup)b.getParent()).removeView(b);}},400);}
 @Override protected void onLongKey(KeyMapper.Result r){if(r.action==KeyMapper.Action.DIGIT)longDigit(r.digit);else if(r.action==KeyMapper.Action.STAR){AppPrefs.put(this,"t9_hebrew",!AppPrefs.bool(this,"t9_hebrew",true));applyFilter();}else if(r.action==KeyMapper.Action.DELETE||r.action==KeyMapper.Action.BACK){if(tab==DIAL){dial="";updateDialDisplay();}else if(tab==CONTACTS){query="";contactSearch=true;applyFilter();}}}
 @Override protected void onKeyAction(KeyMapper.Result r,boolean down){if(down)return;switch(r.action){case UP: if(isTabFocus())focusContent();else moveList(-1);break;case DOWN:if(isTabFocus())focusContent();else moveList(1);break;case LEFT:if(tab==CONTACTS&&list!=null&&list.hasFocus()){contactsAdapter.actionMode=(contactsAdapter.actionMode+3)%4;contactsAdapter.notifyDataSetChanged();}else moveTab(-1);break;case RIGHT:if(tab==CONTACTS&&list!=null&&list.hasFocus()){contactsAdapter.actionMode=(contactsAdapter.actionMode+1)%4;contactsAdapter.notifyDataSetChanged();}else moveTab(1);break;case SELECT:select();break;case CALL:callCurrent();break;case SOFT_LEFT:softLeft();break;case SOFT_RIGHT:softRight();break;case MENU:softLeft();break;case BACK:softRight();break;case DELETE:softRight();break;case DIGIT:inputDigit(r.digit);break;case STAR:onStar();break;case POUND:onPound();break;case PAGE_UP:moveList(-page());break;case PAGE_DOWN:moveList(page());break;default:break;}}
 private boolean isTabFocus(){return tabRow!=null&&tabRow.getFocusedChild()!=null;}
 private void focusContent(){if(tab==FAVS&&grid!=null)grid.requestFocus();else if(list!=null)list.requestFocus();}
 private int page(){return Math.max(1,AppPrefs.i(this,"page_size",6));}
 private void callCurrent(){if(tab==DIAL)callDial();else if(tab==CONTACTS){ContactModel c=selectedContact();if(c!=null)callContact(c);}else if(tab==RECENTS){int p=list.getSelectedItemPosition();if(p>=0&&p<recents.size())call(recents.get(p).number);}else if(tab==FAVS){int p=grid.getSelectedItemPosition();if(p>=0)callContact(favAdapter.getItem(p));}}
}