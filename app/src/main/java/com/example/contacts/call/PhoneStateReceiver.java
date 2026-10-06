package com.example.contacts.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.TelephonyManager;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static String lastState="";
    private static String pendingOutgoing="";

    public void onReceive(Context ctx, Intent intent) {
        if (intent == null) return;
        String a=intent.getAction();
        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(a)) {
            pendingOutgoing=intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if(pendingOutgoing==null)pendingOutgoing="";
            CallStateHolder.number=pendingOutgoing;
            CallStateHolder.direction="outgoing";
            CallStateHolder.incoming=false;
            open(ctx,false,false);
            return;
        }
        if (Intent.ACTION_BOOT_COMPLETED.equals(a)) return;
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(a)) return;
        String state=intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if(state==null)return;
        if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
            String n=intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if(n!=null&&n.length()>0)CallStateHolder.number=n;
            CallStateHolder.direction="incoming";
            CallStateHolder.incoming=true;
            CallStateHolder.active=true;
            open(ctx,true,false);
        } else if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)){
            if(CallStateHolder.number.length()==0)CallStateHolder.number=pendingOutgoing;
            CallStateHolder.direction=CallStateHolder.incoming?"incoming":"outgoing";
            CallStateHolder.startTime=System.currentTimeMillis();
            CallStateHolder.active=true;
            open(ctx,CallStateHolder.incoming,false);
        } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)){
            if(lastState.equals(TelephonyManager.EXTRA_STATE_IDLE))return;
            CallStateHolder.active=false;
            open(ctx,CallStateHolder.incoming,true);
            try{ctx.stopService(new Intent(ctx,CallOverlayService.class));}catch(Exception ignored){}
        }
        lastState=state;
    }

    private void open(Context ctx,boolean incoming,boolean ended){
        try{
            Intent i=new Intent(ctx,InCallActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            i.putExtra("number",CallStateHolder.number);
            i.putExtra("incoming",incoming);
            i.putExtra("ended",ended||!CallStateHolder.active);
            ctx.startActivity(i);
            if(CallStateHolder.active){
                try{ctx.startService(new Intent(ctx,CallOverlayService.class));}catch(Exception ignored){}
                postAgain(ctx,300);postAgain(ctx,800);
            }
        }catch(Exception ignored){}
    }

    private void postAgain(final Context ctx,long delay){
        new Handler().postDelayed(new Runnable(){public void run(){
            if(CallStateHolder.active){
                try{
                    Intent i=new Intent(ctx,InCallActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                    i.putExtra("number",CallStateHolder.number);
                    i.putExtra("incoming",CallStateHolder.incoming);
                    ctx.startActivity(i);
                }catch(Exception ignored){}
            }
        }},delay);
    }
}
