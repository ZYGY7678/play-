package com.zygy.streetlife

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.*

class GameView(c: Context) : View(c) {
    private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private val world=RectF(0f,0f,2400f,1400f)
    private var x=1200f; private var y=700f; private var speed=0f; private var angle=0f
    private var last=System.nanoTime()
    private val cars=mutableListOf<PointF>()
    private var joyX=0f; private var joyY=0f; private var touching=false
    init { for(i in 0 until 22) cars += PointF(120+i*101f, 220+(i%6)*190f) }

    override fun onDraw(c: Canvas) {
        super.onDraw(c); val dt=((System.nanoTime()-last)/1e9).toFloat().coerceAtMost(.05f); last=System.nanoTime()
        update(dt)
        val sx=width/world.width; val sy=height/world.height; val sc=min(sx,sy)
        c.save(); c.scale(sc,sc); val ox=(width/sc-world.width)/2; val oy=(height/sc-world.height)/2; c.translate(ox,oy)
        p.color=Color.rgb(38,45,42); c.drawRect(world,p)
        drawCity(c); drawCar(c,x,y,angle,Color.rgb(225,55,55))
        cars.forEachIndexed { i,q -> drawCar(c,q.x,q.y,if(i%2==0)0f else PI.toFloat()/2,Color.rgb(60+(i*7)%150,120,210)) }
        c.restore(); drawHud(c)
    }

    private fun drawCity(c:Canvas){
        p.color=Color.rgb(54,58,61)
        for(i in 0..8){ val xx=150+i*260f; c.drawRect(xx,0f,xx+92,1400f,p) }
        for(i in 0..5){ val yy=120+i*230f; c.drawRect(0f,yy,2400f,yy+86,p) }
        p.color=Color.rgb(112,112,105)
        for(i in 0..8) for(j in 0..5){ val bx=25+i*260f; val by=25+j*230f; c.drawRect(bx,by,bx+195,by+165,p) }
        p.color=Color.rgb(200,185,80); p.strokeWidth=5f
        for(i in 0..8) for(j in 0..5){ val xx=150+i*260f; val yy=120+j*230f; c.drawLine(xx,yy+8,xx,yy+78,p); c.drawLine(xx,yy+98,xx,yy+160,p) }
    }
    private fun drawCar(c:Canvas,cx:Float,cy:Float,a:Float,col:Int){
        c.save(); c.translate(cx,cy); c.rotate(Math.toDegrees(a.toDouble()).toFloat())
        p.color=Color.BLACK; c.drawRoundRect(-34f,-60f,34f,60f,10f,10f,p)
        p.color=col; c.drawRoundRect(-29f,-54f,29f,54f,9f,9f,p)
        p.color=Color.rgb(35,45,55); c.drawRect(-23f,-28f,23f,12f,p)
        p.color=Color.WHITE; c.drawRect(-22f,-48f,22f,-34f,p)
        c.restore()
    }
    private fun update(dt:Float){
        if(touching){ speed += (-joyY)*900f*dt; angle += joyX*2.4f*dt } else speed*=.94f
        speed=speed.coerceIn(-260f,520f); x += cos(angle)*speed*dt; y += sin(angle)*speed*dt
        x=x.coerceIn(70f,2330f); y=y.coerceIn(70f,1330f)
        invalidate()
    }
    private fun drawHud(c:Canvas){
        p.color=0xAA101010.toInt(); c.drawRoundRect(22f,22f,250f,98f,18f,18f,p)
        p.color=Color.WHITE; p.textSize=28f; c.drawText("STREET LIFE",42f,54f,p); p.textSize=18f; c.drawText("FREE ROAM",42f,82f,p)
        val r=78f; val cx=105f; val cy=height-105f
        p.color=0x88222222.toInt(); c.drawCircle(cx,cy,r,p)
        p.color=0xCCFFFFFF.toInt(); c.drawCircle(cx+joyX*r*.55f,cy+joyY*r*.55f,25f,p)
        p.color=0xAA111111.toInt(); c.drawCircle(width-105f,height-105f,62f,p)
        p.color=Color.WHITE; p.textSize=24f; c.drawText("GO",width-124f,height-96f,p)
    }
    override fun onTouchEvent(e:MotionEvent):Boolean{
        when(e.actionMasked){
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE -> {
                val dx=e.x-105f; val dy=e.y-(height-105f); val d=max(1f,sqrt(dx*dx+dy*dy))
                if(e.x<230f && e.y>height-220f){ joyX=(dx/d).coerceIn(-1f,1f); joyY=(dy/d).coerceIn(-1f,1f); touching=true }
                return true
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL -> { touching=false; joyX=0f; joyY=0f; return true }
        }; return true
    }
}
