package com.example.contacts.data;
import android.content.Context;import android.database.Cursor;import android.provider.CallLog;import com.example.contacts.util.PhoneFormatter;import java.util.ArrayList;import java.util.List;
public final class CallLogRepository{
 private CallLogRepository(){}
 public static class Entry{public String number,name="";public int type;public long date;public int count=1;public Entry(String n,String nm,int t,long d){number=n==null?"":n;name=nm==null?"":nm;type=t;date=d;}}
 public static List<Entry> load(Context c){ArrayList<Entry> out=new ArrayList<Entry>();Cursor x=null;try{x=c.getContentResolver().query(CallLog.Calls.CONTENT_URI,new String[]{CallLog.Calls.NUMBER,CallLog.Calls.CACHED_NAME,CallLog.Calls.TYPE,CallLog.Calls.DATE},null,null,CallLog.Calls.DEFAULT_SORT_ORDER);if(x==null)return out;Entry last=null;while(x.moveToNext()){String n=x.getString(0);String nm=x.getString(1);int t=x.getInt(2);long d=x.getLong(3);if(last!=null&&PhoneFormatter.same(last.number,n)){last.count++;continue;}last=new Entry(n,nm,t,d);out.add(last);if(out.size()>=500)break;}}finally{if(x!=null)x.close();}return out;}
}