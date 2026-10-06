package com.example.contacts.call;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneNumberUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import com.example.contacts.data.ContactsRepository;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;

public class InCallActivity extends Activity {
    private TextView name,number,status,timer,hint;
    private final Handler handler=new Handler();
    private long started;
    private boolean endedScreen=false,incoming=false;
    private String numberValue="";
    private final Runnable tick=new Runnable(){public void run(){updateTimer();if(!endedScreen)handler.postDelayed(this,1000);}};

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        build();apply(getIntent());
    }
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);apply(i);}
    private void apply(Intent i){
        if(i==null)return;
        String n=i.getStringExtra("number");if(n!=null&&n.length()>0)numberValue=n;
        incoming=i.getBooleanExtra("incoming",CallStateHolder.incoming);
        endedScreen=i.getBooleanExtra("ended",false)||!CallStateHolder.active;
        if(numberValue.length()==0)numberValue=CallStateHolder.number;
        number.setText(PhoneFormatter.format(numberValue));loadContactName();
        if(endedScreen){status.setText("השיחה הסתיימה");handler.postDelayed(new Runnable(){public void run(){finish();}},1500);}
        else if(incoming&&CallStateHolder.active&&CallStateHolder.startTime==0){status.setText("צלצול נכנס");}
        else{status.setText(CallStateHolder.active?"בשיחה":"מחייג...");started=CallStateHolder.startTime;}
        handler.removeCallbacks(tick);handler.post(tick);
    }
    private void build(){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(android.view.Gravity.CENTER_HORIZONTAL);root.setBackgroundColor(ColorUtil.BAR);root.setPadding(24,28,24,12);
        name=Ui.bar(this,"",30);root.addView(name,new LinearLayout.LayoutParams(-1,58));
        number=Ui.bar(this,"",18);number.setTextDirection(View.TEXT_DIRECTION_LTR);number.setTextColor(ColorUtil.SECONDARY);root.addView(number,new LinearLayout.LayoutParams(-1,42));
        timer=Ui.bar(this,"00:00",36);root.addView(timer,new LinearLayout.LayoutParams(-1,70));
        status=Ui.bar(this,"",18);status.setTextColor(ColorUtil.ACCENT);root.addView(status,new LinearLayout.LayoutParams(-1,44));
        LinearLayout actions=new LinearLayout(this);actions.setGravity(android.view.Gravity.CENTER);
        TextView mute=Ui.bar(this,"השתק",17),speaker=Ui.bar(this,"רמקול",17),hang=Ui.bar(this,"ניתוק",17);
        actions.addView(mute,new LinearLayout.LayoutParams(0,64,1f));actions.addView(speaker,new LinearLayout.LayoutParams(0,64,1f));actions.addView(hang,new LinearLayout.LayoutParams(0,64,1f));root.addView(actions,new LinearLayout.LayoutParams(-1,72));
        hint=Ui.bar(this,"CALL=ענה • ENDCALL=דחה",13);hint.setTextColor(ColorUtil.SECONDARY);root.addView(hint,new LinearLayout.LayoutParams(-1,46));
        setContentView(root);
        mute.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggleMute();}});
        speaker.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggleSpeaker();}});
        hang.setOnClickListener(new View.OnClickListener(){public void onClick(View v){endCall();}});
    }
    private void loadContactName(){
        final String n=numberValue;
        new android.os.AsyncTask<Void,Void,String>(){
            protected String doInBackground(Void...v){return ContactsRepository.lookupName(InCallActivity.this,n);}
            protected void onPostExecute(String s){name.setText(s!=null&&s.length()>0?s:(incoming?"מספר לא מזוהה":"שיחה יוצאת"));}
        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void updateTimer(){
        long st=started>0?started:(CallStateHolder.startTime>0?CallStateHolder.startTime:System.currentTimeMillis());
        long sec=Math.max(0,(System.currentTimeMillis()-st)/1000);timer.setText(String.format(java.util.Locale.US,"%02d:%02d",sec/60,sec%60));
    }
    private void toggleMute(){AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);CallStateHolder.isMuted=!CallStateHolder.isMuted;am.setMicrophoneMute(CallStateHolder.isMuted);status.setText(CallStateHolder.isMuted?"מושתק":"בשיחה");}
    private void toggleSpeaker(){AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);CallStateHolder.isSpeaker=!CallStateHolder.isSpeaker;am.setMode(AudioManager.MODE_IN_CALL);am.setSpeakerphoneOn(CallStateHolder.isSpeaker);status.setText(CallStateHolder.isSpeaker?"רמקול":"בשיחה");}
    private void answer(){
        if(!CallStateHolder.active)return;
        try{
            Intent d=new Intent(Intent.ACTION_MEDIA_BUTTON);d.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_HEADSETHOOK));sendBroadcast(d);
            Intent u=new Intent(Intent.ACTION_MEDIA_BUTTON);u.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_HEADSETHOOK));sendBroadcast(u);
            CallStateHolder.startTime=System.currentTimeMillis();started=CallStateHolder.startTime;status.setText("בשיחה");hint.setText("ENDCALL=ניתוק • 1=השתק • 2=רמקול");
        }catch(Exception e){Toast.makeText(this,"מענה אינו נתמך ב-ROM הזה",Toast.LENGTH_SHORT).show();}
    }
    private void endCall(){
        try{
            android.telephony.TelephonyManager tm=(android.telephony.TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            java.lang.reflect.Method m=Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");m.setAccessible(true);Object t=m.invoke(tm);java.lang.reflect.Method end=t.getClass().getMethod("endCall");end.invoke(t);
        }catch(Exception e){
            try{Intent u=new Intent(Intent.ACTION_MEDIA_BUTTON);u.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_HEADSETHOOK));sendBroadcast(u);}catch(Exception ignored){}
        }
        CallStateHolder.active=false;status.setText("השיחה הסתיימה");endedScreen=true;handler.removeCallbacks(tick);handler.postDelayed(new Runnable(){public void run(){finish();}},1500);
    }
    @Override public boolean dispatchKeyEvent(KeyEvent e){
        int k=e.getKeyCode();if(!relevant(k))return super.dispatchKeyEvent(e);
        if(e.getAction()==KeyEvent.ACTION_DOWN){e.startTracking();return true;}
        if(e.getAction()==KeyEvent.ACTION_UP){
            if(incoming&&!endedScreen&&k==KeyEvent.KEYCODE_CALL)answer();
            else if(k==KeyEvent.KEYCODE_ENDCALL)endCall();
            else if(k==KeyEvent.KEYCODE_BACK){return true;}
            else if(k==KeyEvent.KEYCODE_DPAD_CENTER)toggleMute();
            else if(k==KeyEvent.KEYCODE_DPAD_RIGHT)toggleSpeaker();
            else if(k==KeyEvent.KEYCODE_DPAD_LEFT)endCall();
            else if(k==KeyEvent.KEYCODE_1)toggleMute();
            else if(k==KeyEvent.KEYCODE_2)toggleSpeaker();
            else if(k==KeyEvent.KEYCODE_VOLUME_UP)volume(1);
            else if(k==KeyEvent.KEYCODE_VOLUME_DOWN)volume(-1);
            return true;
        }
        return true;
    }
    private boolean relevant(int k){return k==KeyEvent.KEYCODE_CALL||k==KeyEvent.KEYCODE_ENDCALL||k==KeyEvent.KEYCODE_BACK||k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_DPAD_LEFT||k==KeyEvent.KEYCODE_DPAD_RIGHT||k==KeyEvent.KEYCODE_1||k==KeyEvent.KEYCODE_2||k==KeyEvent.KEYCODE_VOLUME_UP||k==KeyEvent.KEYCODE_VOLUME_DOWN;}
    private void volume(int d){AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);am.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,d>0?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER,0);}
    @Override protected void onPause(){super.onPause();if(CallStateHolder.active&&!isFinishing())handler.postDelayed(new Runnable(){public void run(){try{startActivity(new Intent(InCallActivity.this,InCallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));}catch(Exception ignored){}}},300);}
    @Override protected void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
}
