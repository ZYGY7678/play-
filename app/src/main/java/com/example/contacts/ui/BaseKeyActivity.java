package com.example.contacts.ui;
import android.app.Activity;import android.view.KeyEvent;import com.example.contacts.keys.KeyMapper;
public abstract class BaseKeyActivity extends Activity{
 protected boolean longHandled=false;
 protected abstract void onKeyAction(KeyMapper.Result r,boolean down);
 protected void onLongKey(KeyMapper.Result r){}
 @Override public boolean dispatchKeyEvent(KeyEvent e){KeyMapper.Result r=KeyMapper.map(this,e);if(r.action==KeyMapper.Action.OTHER)return super.dispatchKeyEvent(e);if(e.getAction()==KeyEvent.ACTION_DOWN){e.startTracking();if(e.getRepeatCount()>0){longHandled=true;onLongKey(r);return true;}longHandled=false;onKeyAction(r,true);return true;}if(e.getAction()==KeyEvent.ACTION_UP){if((e.getFlags()&KeyEvent.FLAG_CANCELED_LONG_PRESS)==0&&!longHandled)onKeyAction(r,false);return true;}return true;}
}