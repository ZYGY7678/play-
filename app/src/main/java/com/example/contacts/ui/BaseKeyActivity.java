package com.example.contacts.ui;
import android.view.KeyEvent;
import android.widget.EditText;
import com.example.contacts.keys.KeyMapper;

public abstract class BaseKeyActivity extends android.app.Activity{
 protected boolean longHandled=false;
 protected boolean allowTextInput(){return false;}
 protected abstract void onKeyAction(KeyMapper.Result r,boolean down);
 protected void onLongKey(KeyMapper.Result r){}
 @Override public boolean dispatchKeyEvent(KeyEvent e){
  KeyMapper.Result r=KeyMapper.map(this,e);
  if(allowTextInput()&&getCurrentFocus() instanceof EditText){
   if(r.action==KeyMapper.Action.DIGIT||r.action==KeyMapper.Action.STAR||r.action==KeyMapper.Action.POUND||r.action==KeyMapper.Action.LEFT||r.action==KeyMapper.Action.RIGHT)return super.dispatchKeyEvent(e);
  }
  if(r.action==KeyMapper.Action.OTHER)return super.dispatchKeyEvent(e);
  if(e.getAction()==KeyEvent.ACTION_DOWN){
   e.startTracking();
   if(e.getRepeatCount()>0){longHandled=true;onLongKey(r);return true;}
   longHandled=false;onKeyAction(r,true);return true;
  }
  if(e.getAction()==KeyEvent.ACTION_UP){
   if((e.getFlags()&KeyEvent.FLAG_CANCELED_LONG_PRESS)==0&&!longHandled){if(com.example.contacts.util.AppPrefs.bool(this,"sounds",true)){try{((android.media.AudioManager)getSystemService(AUDIO_SERVICE)).playSoundEffect(android.view.SoundEffectConstants.CLICK);}catch(Exception ignored){}}onKeyAction(r,false);}
   return true;
  }
  return true;
 }
}