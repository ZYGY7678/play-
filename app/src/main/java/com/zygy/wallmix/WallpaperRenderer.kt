package com.zygy.wallmix

import android.graphics.*
import kotlin.math.*

object WallpaperRenderer {
    const val LAND = 0
    const val CAR = 1
    const val ANIMAL = 2
    const val ABSTRACT = 3

    data class Item(val id: Int, val title: String, val category: Int)

    val items = listOf(
        Item(0,"אגם זריחה",LAND), Item(1,"פסגות כחולות",LAND), Item(2,"יער ערפילי",LAND),
        Item(3,"מדבר זהב",LAND), Item(4,"מפל ירוק",LAND), Item(5,"ים לילי",LAND),
        Item(6,"מכונית אדומה",CAR), Item(7,"מכונית ספורט",CAR), Item(8,"מכונית עירונית",CAR),
        Item(9,"רכב שטח",CAR), Item(10,"קלאסית",CAR), Item(11,"כביש לילה",CAR),
        Item(12,"חתול",ANIMAL), Item(13,"כלב",ANIMAL), Item(14,"זאב",ANIMAL),
        Item(15,"אריה",ANIMAL), Item(16,"ינשוף",ANIMAL), Item(17,"שועל",ANIMAL),
        Item(18,"גלים צבעוניים",ABSTRACT), Item(19,"זוהר",ABSTRACT), Item(20,"מינימל",ABSTRACT),
        Item(21,"קווים",ABSTRACT), Item(22,"ספירלה",ABSTRACT), Item(23,"שקיעה מופשטת",ABSTRACT)
    )

    fun render(itemId: Int, w: Int, h: Int): Bitmap {
        val safeW = w.coerceAtLeast(240)
        val safeH = h.coerceAtLeast(240)
        val b = Bitmap.createBitmap(safeW, safeH, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.drawColor(Color.rgb(18, 24, 36))
        when (itemId) {
            in 0..5 -> landscape(c, safeW, safeH, itemId)
            in 6..11 -> car(c, safeW, safeH, itemId - 6)
            in 12..17 -> animal(c, safeW, safeH, itemId - 12)
            else -> abstract(c, safeW, safeH, itemId - 18)
        }
        return b
    }

    private fun grad(c: Canvas, w: Int, h: Int, top: Int, bottom: Int) {
        val sh = Paint().apply { shader = LinearGradient(0f,0f,0f,h.toFloat(),top,bottom,Shader.TileMode.CLAMP) }
        c.drawRect(0f,0f,w.toFloat(),h.toFloat(),sh)
    }

    private fun landscape(c: Canvas,w:Int,h:Int,id:Int) {
        val sky = when(id){0->intArrayOf(Color.rgb(252,191,104),Color.rgb(87,145,224));1->intArrayOf(Color.rgb(50,78,132),Color.rgb(195,218,245));2->intArrayOf(Color.rgb(70,106,121),Color.rgb(35,56,55));3->intArrayOf(Color.rgb(244,192,104),Color.rgb(181,112,61));4->intArrayOf(Color.rgb(107,187,215),Color.rgb(57,103,78));else->intArrayOf(Color.rgb(19,36,68),Color.rgb(7,14,28))}
        grad(c,w,h,sky[0],sky[1])
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        if(id==0){p.color=Color.rgb(245,228,169);c.drawCircle(w*.76f,h*.22f,h*.07f,p);p.color=Color.rgb(32,103,94);c.drawOval(w*.18f,h*.57f,w*.84f,h*.78f,p);p.color=Color.rgb(80,156,183);c.drawRect(0f,h*.58f,w.toFloat(),h.toFloat(),p);p.color=Color.rgb(36,111,91); for(i in 0..12)c.drawCircle(i*w/12f,h*.66f+(i%2)*16f,55f,p)}
        else if(id==1){p.color=Color.rgb(60,79,112);path(c,p,floatArrayOf(0f,h*.62f,w*.14f,h*.38f,w*.29f,h*.6f,w*.45f,h*.27f,w*.58f,h*.58f,w*.77f,h*.34f,w,h*.62f));p.color=Color.rgb(128,151,174);path(c,p,floatArrayOf(0f,h*.78f,w*.22f,h*.52f,w*.39f,h*.74f,w*.57f,h*.46f,w*.74f,h*.72f,w,h*.55f,w,h,0f,h))}
        else if(id==2){p.color=Color.rgb(42,67,57);c.drawRect(0f,h*.44f,w.toFloat(),h.toFloat(),p);p.color=Color.rgb(55,88,62);for(i in 0..35){val x=(i*79%w).toFloat();val y=(h*.43f+(i*53%(h/2)));c.drawCircle(x,y,42f,p);c.drawRect(x-10,y,x+10,h.toFloat(),p)};p.color=0x40FFFFFF.toInt();for(i in 0..6)c.drawOval(-100f,h*(.25f+i*.07f),w+120f,h*(.35f+i*.07f),p)}
        else if(id==3){p.color=Color.rgb(205,151,91);c.drawRect(0f,h*.45f,w.toFloat(),h.toFloat(),p);p.color=Color.rgb(232,178,104);path(c,p,floatArrayOf(0f,h*.68f,w*.35f,h*.42f,w*.55f,h*.66f,w*.82f,h*.47f,w,h*.7f,w,h,0f,h));p.color=Color.rgb(85,117,52);c.drawRect(w*.13f,h*.55f,w*.15f,h*.82f,p);c.drawCircle(w*.14f,h*.5f,45f,p);c.drawRect(w*.74f,h*.57f,w*.76f,h*.84f,p);c.drawCircle(w*.75f,h*.52f,55f,p)}
        else if(id==4){p.color=Color.rgb(35,105,72);c.drawRect(0f,h*.48f,w.toFloat(),h.toFloat(),p);p.color=Color.WHITE;path(c,p,floatArrayOf(w*.38f,h*.38f,w*.48f,h*.7f,w*.58f,h*.38f,w*.7f,h*.38f,w*.56f,h*.55f,w*.69f,h*.54f,w*.5f,h*.98f,w*.31f,h*.54f,w*.44f,h*.55f,w*.3f,h*.38f));p.color=Color.rgb(55,130,83);for(i in 0..14)c.drawCircle(i*w/14f,h*.78f+(i%3)*22f,34f,p)}
        else {p.color=Color.rgb(235,221,166);c.drawCircle(w*.76f,h*.21f,h*.06f,p);p.color=Color.rgb(10,30,55);c.drawRect(0f,h*.5f,w.toFloat(),h.toFloat(),p);p.color=Color.rgb(20,62,90);for(i in 0..8)c.drawOval(-30f,h*(.53f+i*.055f),w+40f,h*(.61f+i*.055f),p);p.color=0x50FFFFFF.toInt();for(i in 0..50){val x=(i*97%w).toFloat();val y=(i*151%(h/2)).toFloat();c.drawCircle(x,y,3f,p)}}
    }

    private fun car(c:Canvas,w:Int,h:Int,id:Int){
        grad(c,w,h,Color.rgb(24,31,44),Color.rgb(82,91,110)); val p=Paint(Paint.ANTI_ALIAS_FLAG)
        p.color=Color.rgb(33,38,47);c.drawRect(0f,h*.63f,w.toFloat(),h.toFloat(),p)
        p.color=0x50FFFFFF.toInt();for(i in 0..7)c.drawRect(i*w/8f,h*.7f,(i*w/8f)+w*.018f,h,p)
        drawCar(c,w*.5f,h*.61f,1f+id*.06f, when(id){0->Color.rgb(224,48,53);1->Color.rgb(245,195,46);2->Color.rgb(59,170,128);3->Color.rgb(70,105,160);4->Color.rgb(230,230,230);else->Color.rgb(150,64,190)})
        if(id==5){p.color=0x70FFFFFF.toInt();for(i in 0..15)c.drawCircle((i*117%w).toFloat(),(i*83%(h/2)).toFloat(),2f,p)}
    }

    private fun drawCar(c:Canvas,x:Float,y:Float,s:Float,col:Int){
        val p=Paint(Paint.ANTI_ALIAS_FLAG);p.color=Color.BLACK;c.drawRoundRect(x-170*s,y-85*s,x+170*s,y+95*s,42*s,42*s,p)
        p.color=col;c.drawRoundRect(x-155*s,y-72*s,x+155*s,y+80*s,38*s,38*s,p)
        p.color=Color.rgb(35,49,65);c.drawRoundRect(x-95*s,y-55*s,x+95*s,y+10*s,22*s,22*s,p)
        p.color=Color.WHITE;c.drawRect(x-140*s,y-5*s,x-108*s,y+22*s,p);c.drawRect(x+108*s,y-5*s,x+140*s,y+22*s,p)
        p.color=Color.rgb(30,30,30);c.drawCircle(x-105*s,y+80*s,32*s,p);c.drawCircle(x+105*s,y+80*s,32*s,p)
        c.drawCircle(x-105*s,y-80*s,30*s,p);c.drawCircle(x+105*s,y-80*s,30*s,p)
    }

    private fun animal(c:Canvas,w:Int,h:Int,id:Int){
        grad(c,w,h,Color.rgb(32,46,58),Color.rgb(12,17,24)); val p=Paint(Paint.ANTI_ALIAS_FLAG)
        p.color=0x35FFFFFF.toInt();for(i in 0..7)c.drawCircle((i*151%w).toFloat(),(i*211%(h/2)).toFloat(),i*9f+8f,p)
        val cx=w/2f;val cy=h*.57f; p.color=when(id){0->Color.rgb(210,166,120);1->Color.rgb(130,94,68);2->Color.rgb(74,78,86);3->Color.rgb(194,145,67);4->Color.rgb(104,85,63);else->Color.rgb(198,110,52)}
        c.drawOval(cx-180f,cy-175f,cx+180f,cy+180f,p)
        path(c,p,floatArrayOf(cx-130f,cy-135f,cx-190f,cy-245f,cx-95f,cy-195f,cx,cy-225f,cx+95f,cy-195f,cx+190f,cy-245f,cx+130f,cy-135f))
        p.color=Color.rgb(22,25,29);c.drawCircle(cx-58f,cy-20f,13f,p);c.drawCircle(cx+58f,cy-20f,13f,p)
        p.color=Color.WHITE;c.drawCircle(cx-54f,cy-24f,4f,p);c.drawCircle(cx+54f,cy-24f,4f,p)
        p.color=0xFF000000.toInt();c.drawOval(cx-27f,cy+34f,cx+27f,cy+68f,p)
    }

    private fun abstract(c:Canvas,w:Int,h:Int,id:Int){
        val p=Paint(Paint.ANTI_ALIAS_FLAG)
        grad(c,w,h,Color.rgb(31,39,63),Color.rgb(90,50,120))
        when(id){0->{p.style=Paint.Style.STROKE;p.strokeWidth=w*.08f;p.color=Color.rgb(80,190,240);for(i in 0..7)c.drawOval(-w*.2f,h*(.18f+i*.08f),w*1.1f,h*(.65f+i*.08f),p)}
            1->{p.style=Paint.Style.FILL;for(i in 0..17){p.color=Color.HSVToColor(floatArrayOf((i*21f)%360,0.72f,1f));c.drawCircle(w*(.15f+(i*37%80)/100f),h*(.1f+(i*61%80)/100f),20f+(i%6)*18f,p)}}
            2->{p.color=Color.rgb(245,245,245);c.drawRoundRect(w*.13f,h*.12f,w*.87f,h*.88f,70f,70f,p);p.color=Color.rgb(42,49,63);c.drawCircle(w*.5f,h*.5f,h*.22f,p);p.color=Color.rgb(245,245,245);c.drawCircle(w*.5f,h*.5f,h*.1f,p)}
            3->{p.style=Paint.Style.STROKE;p.strokeWidth=8f;for(i in 0..11){p.color=Color.HSVToColor(floatArrayOf(i*22f,0.7f,1f));c.drawLine(w*.1f+i*w*.065f,0f,w*.1f+(i*41%11)*w*.075f,h,p)}}
            4->{p.style=Paint.Style.STROKE;p.strokeWidth=w*.018f;p.color=Color.WHITE;var r=min(w,h)*.06f;for(i in 0..20){c.drawCircle(w*.5f,h*.5f,r+i*r*.55f,p)}}
            else->{p.style=Paint.Style.FILL;p.color=Color.rgb(246,146,82);c.drawCircle(w*.72f,h*.25f,h*.12f,p);p.color=Color.rgb(104,67,168);path(c,p,floatArrayOf(0f,h*.63f,w*.25f,h*.42f,w*.5f,h*.67f,w*.75f,h*.4f,w,h*.6f,w,h,0f,h))}
        }
    }

    private fun path(c:Canvas,p:Paint,pts:FloatArray){val q=Path();q.moveTo(pts[0],pts[1]);var i=2;while(i<pts.size){q.lineTo(pts[i],pts[i+1]);i+=2};q.close();c.drawPath(q,p)}
}
