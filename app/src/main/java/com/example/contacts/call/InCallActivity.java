package com.example.contacts.call;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contacts.data.ContactsRepository;
import com.example.contacts.keys.KeyMapper;
import com.example.contacts.ui.BaseKeyActivity;
import com.example.contacts.util.ColorUtil;
import com.example.contacts.util.Palette;
import com.example.contacts.util.PhoneFormatter;
import com.example.contacts.util.Ui;
import com.example.contacts.widget.AvatarView;
import com.example.contacts.widget.FocusableRow;
import com.example.contacts.widget.SoftKeyBar;

public class InCallActivity extends BaseKeyActivity {
    private TextView name, number, timer, status, bottomLeft, bottomCenter, bottomRight;
    private View avatar;
    private final Handler handler=new Handler();
    private long started=0L;
    private String numberValue="";
    private boolean incoming=false, ended=false;
    private int action=0;

    private final Runnable clock=new Runnable(){
        public void run(){updateTimer();if(!ended)handler.postDelayed(this,1000);}
    };

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        android.view.Window w=getWindow();
        w.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                |android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                |android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                |android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        build();
        apply(getIntent());
    }

    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);apply(i);}

    private void build(){
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Palette.bar(this));
        root.setPadding(Ui.dp(this,16),Ui.dp(this,12),Ui.dp(this,16),0);

        LinearLayout top=new LinearLayout(this);
        top.setGravity(Gravity.CENTER_HORIZONTAL);
        top.setOrientation(LinearLayout.VERTICAL);

        TextView kind=Ui.center(this,"שיחה",13,Palette.secondary(this));
        top.addView(kind,new LinearLayout.LayoutParams(-1,Ui.dp(this,24)));

        AvatarView av=new AvatarView(this);
        avatar=av;
        av.setRing(true);
        LinearLayout.LayoutParams avp=new LinearLayout.LayoutParams(Ui.dp(this,108),Ui.dp(this,108));
        avp.gravity=Gravity.CENTER_HORIZONTAL;
        avp.topMargin=Ui.dp(this,4);
        top.addView(av,avp);

        name=Ui.center(this,"",28,Palette.text(this));
        name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        name.setSingleLine(true);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        top.addView(name,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));

        number=Ui.center(this,"",17,Palette.secondary(this));
        number.setTextDirection(View.TEXT_DIRECTION_LTR);
        top.addView(number,new LinearLayout.LayoutParams(-1,Ui.dp(this,30)));

        timer=Ui.center(this,"00:00",34,Palette.text(this));
        top.addView(timer,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));

        status=Ui.center(this,"",16,Palette.accent(this));
        status.setBackground(Ui.rounded(ColorUtil.alpha(Palette.accent(this),36),0,0,16,this));
        status.setPadding(Ui.dp(this,18),0,Ui.dp(this,18),0);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,Ui.dp(this,32));
        sp.gravity=Gravity.CENTER_HORIZONTAL;
        top.addView(status,sp);

        root.addView(top,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER);
        FocusableRow a=new FocusableRow(this);
        a.setGravity(Gravity.CENTER);
        a.addView(Ui.bold(Ui.center(this,"השתק",16,Palette.text(this))),new LinearLayout.LayoutParams(-1,-2));
        FocusableRow s=new FocusableRow(this);
        s.setGravity(Gravity.CENTER);
        s.addView(Ui.bold(Ui.center(this,"רמקול",16,Palette.text(this))),new LinearLayout.LayoutParams(-1,-2));
        FocusableRow h=new FocusableRow(this);
        h.setGravity(Gravity.CENTER);
        TextView hang=Ui.bold(Ui.center(this,"ניתוק",16,Palette.RED));
        h.addView(hang,new LinearLayout.LayoutParams(-1,-2));
        actions.addView(a,new LinearLayout.LayoutParams(0,-2,1));
        actions.addView(s,new LinearLayout.LayoutParams(0,-2,1));
        actions.addView(h,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(actions,new LinearLayout.LayoutParams(-1,-2));

        SoftKeyBar soft=new SoftKeyBar(this,"ענה","השתק","דחה");
        bottomLeft=soft.left;
        bottomCenter=soft.center;
        bottomRight=soft.right;
        bottomRight.setNormalColor(Palette.RED);
        // soft bar spans the full width, so cancel the root's side padding
        LinearLayout.LayoutParams sbp=new LinearLayout.LayoutParams(-1,-2);
        sbp.setMargins(-Ui.dp(this,16),0,-Ui.dp(this,16),0);
        root.addView(soft,sbp);

        setContentView(root);
        softColors(true);
        a.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggleMute();}});
        s.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggleSpeaker();}});
        h.setOnClickListener(new View.OnClickListener(){public void onClick(View v){endCall();}});
    }

    /** Left soft key is green only while the phone is ringing (it is "answer"). */
    private void softColors(boolean ringing){
        bottomLeft.setNormalColor(ringing?Palette.GREEN:Palette.text(this));
        bottomCenter.setNormalColor(Palette.text(this));
        bottomRight.setNormalColor(Palette.RED);
    }

    private void apply(Intent i){
        if(i==null)return;
        String n=i.getStringExtra("number");
        if(n!=null&&n.length()>0)numberValue=n;
        incoming=i.getBooleanExtra("incoming",CallStateHolder.incoming);
        ended=i.getBooleanExtra("ended",false)||!CallStateHolder.active;
        if(numberValue.length()==0)numberValue=CallStateHolder.number;
        number.setText(PhoneFormatter.ltr(PhoneFormatter.format(numberValue)));
        name.setText(incoming?"מספר לא ידוע":"שיחה יוצאת");
        new android.os.AsyncTask<Void,Void,String>() {
            protected String doInBackground(Void... v){return ContactsRepository.lookupName(InCallActivity.this,numberValue);}
            protected void onPostExecute(String who){if(who!=null&&who.length()>0){name.setText(who);((AvatarView)avatar).setName(who);}}
        }.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
        if(ended){
            status.setText("השיחה הסתיימה");
            bottomLeft.setText("חייג שוב");
            bottomCenter.setText("הוסף");
            bottomRight.setText("סגור");
            updateTimer();
            handler.postDelayed(new Runnable(){public void run(){finish();}},1500);
        } else if(incoming&&CallStateHolder.startTime==0){
            status.setText("צלצול נכנס");
            bottomLeft.setText("ענה");
            bottomCenter.setText("השתק צלצול");
            bottomRight.setText("דחה");
            startPulse();
        } else {
            started=CallStateHolder.startTime;
            status.setText(CallStateHolder.active?"בשיחה":"מחייג...");
            bottomLeft.setText("השתק");
            bottomCenter.setText("רמקול");
            bottomRight.setText("ניתוק");
        }
        softColors(!ended&&incoming&&CallStateHolder.startTime==0);
        handler.removeCallbacks(clock);
        handler.post(clock);
    }

    private void startPulse(){
        if(!com.example.contacts.util.AppPrefs.bool(this,"animations",true))return;
        avatar.animate().scaleX(1.08f).scaleY(1.08f).setDuration(450).withEndAction(new Runnable(){
            public void run(){avatar.animate().scaleX(1f).scaleY(1f).setDuration(450).start();}
        }).start();
        handler.postDelayed(new Runnable(){public void run(){if(!ended&&incoming)startPulse();}},900);
    }

    private void updateTimer(){
        long st=started>0?started:(CallStateHolder.startTime>0?CallStateHolder.startTime:System.currentTimeMillis());
        long sec=Math.max(0,(System.currentTimeMillis()-st)/1000);
        timer.setText(String.format(java.util.Locale.US,"%02d:%02d",sec/60,sec%60));
    }

    private void toggleMute(){
        AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);
        CallStateHolder.isMuted=!CallStateHolder.isMuted;
        am.setMicrophoneMute(CallStateHolder.isMuted);
        status.setText(CallStateHolder.isMuted?"מושתק":"בשיחה");
    }

    private void toggleSpeaker(){
        AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);
        CallStateHolder.isSpeaker=!CallStateHolder.isSpeaker;
        am.setMode(AudioManager.MODE_IN_CALL);
        am.setSpeakerphoneOn(CallStateHolder.isSpeaker);
        status.setText(CallStateHolder.isSpeaker?"רמקול":"בשיחה");
    }

    private void answer(){
        if(!CallStateHolder.active)return;
        try{
            Intent d=new Intent(Intent.ACTION_MEDIA_BUTTON);
            d.putExtra(Intent.EXTRA_KEY_EVENT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,android.view.KeyEvent.KEYCODE_HEADSETHOOK));
            sendBroadcast(d);
            Intent u=new Intent(Intent.ACTION_MEDIA_BUTTON);
            u.putExtra(Intent.EXTRA_KEY_EVENT,new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,android.view.KeyEvent.KEYCODE_HEADSETHOOK));
            sendBroadcast(u);
            CallStateHolder.startTime=System.currentTimeMillis();
            started=CallStateHolder.startTime;
            status.setText("בשיחה");
            bottomLeft.setText("השתק");
            bottomCenter.setText("רמקול");
            bottomRight.setText("ניתוק");
            softColors(false);
        }catch(Exception e){Toast.makeText(this,"מענה אינו נתמך במכשיר הזה",Toast.LENGTH_SHORT).show();}
    }

    private void endCall(){
        try{
            android.telephony.TelephonyManager tm=(android.telephony.TelephonyManager)getSystemService(TELEPHONY_SERVICE);
            java.lang.reflect.Method m=Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            Object tel=m.invoke(tm);
            tel.getClass().getMethod("endCall").invoke(tel);
        }catch(Exception ignored){}
        CallStateHolder.active=false;
        ended=true;
        status.setText("השיחה הסתיימה");
        handler.removeCallbacks(clock);
        handler.postDelayed(new Runnable(){public void run(){finish();}},1500);
    }

    @Override protected void onKeyAction(KeyMapper.Result r,boolean down){
        if(down)return;
        switch(r.action){
            case CALL:
                if(incoming&&!ended&&CallStateHolder.startTime==0)answer();else if(ended)startDialAgain();
                break;
            case SOFT_LEFT:
                if(incoming&&!ended&&CallStateHolder.startTime==0)answer();else toggleMute();
                break;
            case SOFT_RIGHT:
                if(incoming&&!ended&&CallStateHolder.startTime==0)endCall();else endCall();
                break;
            case MENU:
                Toast.makeText(this,"אפשרויות שיחה: השתּק / רמקול / ניתוק",Toast.LENGTH_SHORT).show();
                break;
            case BACK:
                if(ended)finish();
                break;
            case LEFT:
                action=(action+2)%3;updateActionLabel();
                break;
            case RIGHT:
                action=(action+1)%3;updateActionLabel();
                break;
            case SELECT:
                executeAction();
                break;
            case DIGIT:
                if(r.digit==1)toggleMute();
                else if(r.digit==2)toggleSpeaker();
                else if(r.digit==3)Toast.makeText(this,"מקלדת DTMF אינה זמינה ב-ROM רגיל",Toast.LENGTH_SHORT).show();
                else if(r.digit==0&&!ended)Toast.makeText(this,"DTMF 0 אינו זמין ללא הרשאת מערכת",Toast.LENGTH_SHORT).show();
                break;
            case PAGE_UP:
                if(incoming&&!ended&&CallStateHolder.startTime==0)muteRinger();else volume(1);
                break;
            case PAGE_DOWN:
                if(incoming&&!ended&&CallStateHolder.startTime==0)muteRinger();else volume(-1);
                break;
            default:break;
        }
    }

    private void updateActionLabel(){
        if(ended)return;
        bottomCenter.setText(action==0?"השתק":action==1?"רמקול":"ניתוק");
    }
    private void executeAction(){if(incoming&&!ended&&CallStateHolder.startTime==0){answer();return;}if(action==0)toggleMute();else if(action==1)toggleSpeaker();else endCall();}
    private void muteRinger(){AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);am.setRingerMode(AudioManager.RINGER_MODE_SILENT);status.setText("צלצול מושתק");}
    private void volume(int d){AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);am.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL,d>0?AudioManager.ADJUST_RAISE:AudioManager.ADJUST_LOWER,0);}
    private void startDialAgain(){try{startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(numberValue))));}catch(Exception ignored){}}
    @Override protected void onPause(){super.onPause();if(CallStateHolder.active&&!isFinishing())handler.postDelayed(new Runnable(){public void run(){try{startActivity(new Intent(InCallActivity.this,InCallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));}catch(Exception ignored){}}},300);}
    @Override protected void onDestroy(){handler.removeCallbacks(clock);super.onDestroy();}
}