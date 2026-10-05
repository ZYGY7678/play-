package com.zygy.wallmix

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.io.IOException

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private var selected = 0
    private var activeCategory = WallpaperRenderer.LAND

    private val categories = listOf("נופים" to WallpaperRenderer.LAND, "רכבים" to WallpaperRenderer.CAR, "חיות" to WallpaperRenderer.ANIMAL, "מופשט" to WallpaperRenderer.ABSTRACT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(245,247,251)
        window.navigationBarColor = Color.rgb(245,247,251)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        showGallery()
    }

    private fun showGallery() {
        root = base()
        val title = TextView(this).apply { text="WallMix"; textSize=30f; setTextColor(Color.rgb(21,32,51)); setPadding(8,16,8,4); typeface=android.graphics.Typeface.DEFAULT_BOLD }
        root.addView(title, lp(-1,72))
        val sub = TextView(this).apply { text="רקעים יפים למסך הבית ולמסך הנעילה"; textSize=15f; setTextColor(Color.rgb(105,117,138)); setPadding(8,0,8,10) }
        root.addView(sub, lp(-1,42))
        val chipScroll=HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled=false; overScrollMode=View.OVER_SCROLL_NEVER }
        val chips=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; setPadding(0,0,0,10) }
        categories.forEach { (name,cat) -> chips.addView(chip(name,cat)) }
        chipScroll.addView(chips)
        root.addView(chipScroll,lp(-1,56))
        content=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        val sc=ScrollView(this).apply { isFillViewport=true; addView(content) }
        root.addView(sc, LinearLayout.LayoutParams(-1,0,1f))
        buildGrid()
        setContentView(root)
    }

    private fun chip(name:String,cat:Int):TextView = TextView(this).apply {
        text=name; textSize=14f; gravity=Gravity.CENTER; setPadding(24,0,24,0); setTextColor(if(activeCategory==cat)Color.WHITE else Color.rgb(45,57,76))
        background=round(if(activeCategory==cat)Color.rgb(79,103,255) else Color.WHITE,60)
        setOnClickListener { activeCategory=cat; showGallery() }
    }

    private fun buildGrid(){
        val grid=GridLayout(this).apply { columnCount=2; alignmentMode=GridLayout.ALIGN_BOUNDS; useDefaultMargins=false; setPadding(4,4,4,24) }
        WallpaperRenderer.items.filter{it.category==activeCategory}.forEach { item ->
            val card=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(7,7,7,12); background=round(Color.WHITE,28); elevation=3f }
            val iv=ImageView(this).apply { scaleType=ImageView.ScaleType.CENTER_CROP; setImageBitmap(WallpaperRenderer.render(item.id,420,640)) }
            card.addView(iv,GridLayout.LayoutParams().apply{width=0;height=420;columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);setMargins(7,7,7,3)})
            val tv=TextView(this).apply{text=item.title;textSize=16f;setTextColor(Color.rgb(21,32,51));gravity=Gravity.CENTER_HORIZONTAL;setPadding(4,8,4,4);typeface=android.graphics.Typeface.DEFAULT_BOLD}
            card.addView(tv,LinearLayout.LayoutParams(-1,52))
            card.setOnClickListener{selected=item.id;showPreview()}
            val glp=GridLayout.LayoutParams().apply{width=0;height=500;columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);rowSpec=GridLayout.spec(GridLayout.UNDEFINED);setMargins(7,7,7,7)}
            grid.addView(card,glp)
        }
        content.removeAllViews();content.addView(grid,LinearLayout.LayoutParams(-1,-2))
    }

    private fun showPreview(){
        val root=FrameLayout(this)
        root.setBackgroundColor(Color.rgb(16,21,31))
        val iv=ImageView(this).apply{scaleType=ImageView.ScaleType.CENTER_CROP;setImageBitmap(WallpaperRenderer.render(selected,900,1500))}
        root.addView(iv,FrameLayout.LayoutParams(-1,-1))
        val shade=View(this).apply{background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xB0000000.toInt(),0x00000000,0xC9000000.toInt()))}
        root.addView(shade,FrameLayout.LayoutParams(-1,-1))
        val title=TextView(this).apply{text=WallpaperRenderer.items[selected].title;textSize=24f;setTextColor(Color.WHITE);setPadding(28,24,28,12);typeface=android.graphics.Typeface.DEFAULT_BOLD}
        val top=FrameLayout.LayoutParams(-1,90);top.gravity=Gravity.TOP;root.addView(title,top)
        val actions=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(18,18,18,28)}
        val back=button("חזרה")
        val set=button("הגדר כטפט")
        actions.addView(back,LinearLayout.LayoutParams(0,64,1f))
        actions.addView(Space(this),LinearLayout.LayoutParams(18,1))
        actions.addView(set,LinearLayout.LayoutParams(0,64,1f))
        val bottom=FrameLayout.LayoutParams(-1,120);bottom.gravity=Gravity.BOTTOM;root.addView(actions,bottom)
        back.setOnClickListener{showGallery()}
        set.setOnClickListener{setWallpaper()}
        setContentView(root)
    }

    private fun setWallpaper(){
        val wm=WallpaperManager.getInstance(this)
        val dm=resources.displayMetrics
        val bmp=WallpaperRenderer.render(selected,dm.widthPixels,dm.heightPixels)
        try {
            wm.setBitmap(bmp,null,true,WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
            Toast.makeText(this,"הטפט הוגדר בהצלחה",Toast.LENGTH_LONG).show()
        } catch(e:IOException) {
            Toast.makeText(this,"לא הצלחתי להגדיר את הטפט",Toast.LENGTH_LONG).show()
        } catch(e:RuntimeException) {
            Toast.makeText(this,"הגדרת הטפט נכשלה במכשיר הזה",Toast.LENGTH_LONG).show()
        } finally {
            bmp.recycle()
        }
    }

    private fun base():LinearLayout=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(18,12,18,0);setBackgroundColor(Color.rgb(245,247,251))}
    private fun lp(w:Int,h:Int)=LinearLayout.LayoutParams(w,h)
    private fun round(color:Int,r:Float)=GradientDrawable().apply{setColor(color);cornerRadius=r}
    private fun button(t:String)=TextView(this).apply{ text=t;gravity=Gravity.CENTER;textSize=16f;setTextColor(Color.WHITE);background=round(Color.rgb(79,103,255),24);isClickable=true }
}
