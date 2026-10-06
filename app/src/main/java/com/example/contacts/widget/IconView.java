package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.example.contacts.util.Palette;

public class IconView extends View {
    public static final int CALL=1, SMS=2, INFO=3, MENU=4, STAR=5, DELETE=6, EDIT=7, CLOCK=8, IN=9, OUT=10, MISSED=11;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private int type=INFO;
    public IconView(Context c){super(c);setFocusable(false);setClickable(false);setContentDescription("פעולה");p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f*c.getResources().getDisplayMetrics().density);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
    public void setType(int t){type=t;setContentDescription(label());invalidate();}
    private String label(){switch(type){case CALL:return"חיוג";case SMS:return"הודעה";case INFO:return"פרטים";case MENU:return"אפשרויות";case STAR:return"מועדף";case DELETE:return"מחיקה";case EDIT:return"עריכה";case CLOCK:return"שעה";case IN:return"שיחה נכנסת";case OUT:return"שיחה יוצאת";default:return"שיחה שלא נענתה";}}
    private int color(){if(type==CALL||type==IN)return Palette.GREEN;if(type==MISSED||type==DELETE)return Palette.RED;if(type==STAR)return Palette.YELLOW;return Palette.accent(getContext());}
    protected void onDraw(Canvas c){
        super.onDraw(c);p.setColor(color());float d=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f,sz=Math.min(w,h)*0.48f;p.setStrokeWidth(2*d);
        if(type==STAR){Path s=new Path();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float r=(i%2==0?sz/2f:sz/4.2f);float x=cx+(float)Math.cos(a)*r,y=cy+(float)Math.sin(a)*r;if(i==0)s.moveTo(x,y);else s.lineTo(x,y);}s.close();c.drawPath(s,p);return;}
        if(type==CALL){Path q=new Path();q.moveTo(cx-sz*.35f,cy-sz*.25f);q.cubicTo(cx-sz*.1f,cy+sz*.35f,cx+sz*.15f,cy+sz*.35f,cx+sz*.35f,cy+sz*.2f);c.drawPath(q,p);return;}
        if(type==SMS){RectF r=new RectF(cx-sz/2,cy-sz*.34f,cx+sz/2,cy+sz*.3f);c.drawRoundRect(r,8*d,8*d,p);Path q=new Path();q.moveTo(cx-sz*.15f,cy+sz*.3f);q.lineTo(cx-sz*.05f,cy+sz*.48f);q.lineTo(cx+sz*.02f,cy+sz*.3f);c.drawPath(q,p);return;}
        if(type==INFO){c.drawCircle(cx,cy,sz*.42f,p);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy-sz*.18f,2.2f*d,p);c.drawRect(cx-2*d,cy-sz*.02f,cx+2*d,cy+sz*.25f,p);p.setStyle(Paint.Style.STROKE);return;}
        if(type==MENU){p.setStyle(Paint.Style.FILL);for(int i=-1;i<=1;i++)c.drawCircle(cx,cy+i*7*d,2.2f*d,p);p.setStyle(Paint.Style.STROKE);return;}
        if(type==CLOCK){c.drawCircle(cx,cy,sz*.42f,p);c.drawLine(cx,cy,cx,cy-sz*.22f,p);c.drawLine(cx,cy,cx+sz*.18f,cy+sz*.08f,p);return;}
        if(type==DELETE){RectF r=new RectF(cx-sz*.32f,cy-sz*.22f,cx+sz*.32f,cy+sz*.35f);c.drawRoundRect(r,3*d,3*d,p);c.drawLine(cx-sz*.38f,cy-sz*.3f,cx+sz*.38f,cy-sz*.3f,p);c.drawLine(cx-sz*.12f,cy-sz*.4f,cx+sz*.12f,cy-sz*.4f,p);return;}
        if(type==EDIT){c.drawLine(cx-sz*.3f,cy+sz*.3f,cx+sz*.24f,cy-sz*.25f,p);c.drawLine(cx-sz*.38f,cy+sz*.4f,cx-sz*.27f,cy+sz*.28f,p);return;}
        if(type==IN||type==OUT||type==MISSED){Path q=new Path();q.moveTo(cx-sz*.38f,cy);q.lineTo(cx+sz*.28f,cy);q.lineTo(cx+sz*.12f,cy-sz*.16f);q.moveTo(cx+sz*.28f,cy);q.lineTo(cx+sz*.12f,cy+sz*.16f);c.drawPath(q,p);return;}
    }
}