package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.example.contacts.util.Palette;

public class IconView extends View {
    public static final int CALL = 1, SMS = 2, INFO = 3, MENU = 4, STAR = 5, DELETE = 6, EDIT = 7, CLOCK = 8,
            IN = 9, OUT = 10, MISSED = 11, COPY = 12;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Path path = new Path();
    private int type = INFO;
    private int tint = 0;
    private boolean hasTint = false;
    public IconView(Context c) { super(c); setFocusable(false); setClickable(false); setContentDescription("פעולה"); p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); }
    public void setType(int t) { type=t; setContentDescription(label()); invalidate(); }
    public void setTint(int color) { hasTint=color!=0; tint=color; invalidate(); }
    private String label() { switch(type){case CALL:return "חיוג";case SMS:return "הודעה";case INFO:return "פרטים";case MENU:return "אפשרויות";case STAR:return "מועדף";case DELETE:return "מחיקה";case EDIT:return "עריכה";case CLOCK:return "שעה";case IN:return "שיחה נכנסת";case OUT:return "שיחה יוצאת";case COPY:return "העתקה";default:return "שיחה שלא נענתה";} }
    private int color(){if(hasTint)return tint;if(type==CALL||type==IN)return Palette.GREEN;if(type==MISSED||type==DELETE)return Palette.RED;if(type==STAR)return Palette.YELLOW;return Palette.accent(getContext());}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f,sz=Math.min(w,h)*.48f;p.setColor(color());p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.2f*d);path.reset();switch(type){case STAR:for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float rad=i%2==0?sz/2f:sz/4.2f,x=cx+(float)Math.cos(a)*rad,y=cy+(float)Math.sin(a)*rad;if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();c.drawPath(path,p);break;case CALL:{float R=sz*.55f,ox=cx+R*.5f,oy=cy-R*.5f;p.setStrokeWidth(3.2f*d);r.set(ox-R,oy-R,ox+R,oy+R);c.drawArc(r,90f,90f,false,p);c.drawLine(ox-R,oy,ox-R+.34f*R,oy,p);c.drawLine(ox,oy+R,ox,oy+R-.34f*R,p);break;}case SMS:r.set(cx-sz/2,cy-sz*.34f,cx+sz/2,cy+sz*.3f);c.drawRoundRect(r,6*d,6*d,p);path.moveTo(cx-sz*.18f,cy+sz*.3f);path.lineTo(cx-sz*.08f,cy+sz*.5f);path.lineTo(cx+sz*.06f,cy+sz*.3f);c.drawPath(path,p);break;case INFO:c.drawCircle(cx,cy,sz*.42f,p);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy-sz*.18f,2.1f*d,p);c.drawRect(cx-1.6f*d,cy-sz*.04f,cx+1.6f*d,cy+sz*.24f,p);break;case MENU:p.setStyle(Paint.Style.FILL);for(int i=-1;i<=1;i++)c.drawCircle(cx,cy+i*7*d,2.2f*d,p);break;case CLOCK:c.drawCircle(cx,cy,sz*.42f,p);c.drawLine(cx,cy,cx,cy-sz*.22f,p);c.drawLine(cx,cy,cx+sz*.18f,cy+sz*.08f,p);break;case DELETE:r.set(cx-sz*.32f,cy-sz*.22f,cx+sz*.32f,cy+sz*.35f);c.drawRoundRect(r,3*d,3*d,p);c.drawLine(cx-sz*.4f,cy-sz*.3f,cx+sz*.4f,cy-sz*.3f,p);c.drawLine(cx-sz*.12f,cy-sz*.42f,cx+sz*.12f,cy-sz*.42f,p);break;case EDIT:c.drawLine(cx-sz*.3f,cy+sz*.3f,cx+sz*.24f,cy-sz*.25f,p);c.drawLine(cx-sz*.38f,cy+sz*.4f,cx-sz*.27f,cy+sz*.28f,p);break;case COPY:r.set(cx-sz*.12f,cy-sz*.34f,cx+sz*.36f,cy+sz*.14f);c.drawRoundRect(r,3*d,3*d,p);r.set(cx-sz*.36f,cy-sz*.12f,cx+sz*.12f,cy+sz*.36f);c.drawRoundRect(r,3*d,3*d,p);break;case OUT:{float ex=cx+sz*.3f,ey=cy-sz*.3f;c.drawLine(cx-sz*.3f,cy+sz*.3f,ex,ey,p);path.moveTo(ex-sz*.34f,ey);path.lineTo(ex,ey);path.lineTo(ex,ey+sz*.34f);c.drawPath(path,p);break;}case IN:case MISSED:{float ex=cx-sz*.3f,ey=cy+sz*.3f;c.drawLine(cx+sz*.3f,cy-sz*.3f,ex,ey,p);path.moveTo(ex+sz*.34f,ey);path.lineTo(ex,ey);path.lineTo(ex,ey-sz*.34f);c.drawPath(path,p);break;}default:break;}}
}