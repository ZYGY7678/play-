package com.zygy.roadlegends;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class GameView extends FrameLayout {
  private final World world;
  private final HUD hud;
  public GameView(Context c){
    super(c);
    world=new World(c);
    hud=new HUD(c,world.r);
    addView(world,new FrameLayout.LayoutParams(-1,-1));
    addView(hud,new FrameLayout.LayoutParams(-1,-1));
  }

  private static final class World extends GLSurfaceView {
    final R r;
    World(Context c){
      super(c); setEGLContextClientVersion(2); r=new R();
      setRenderer(r); setRenderMode(RENDERMODE_CONTINUOUSLY);
    }
  }

  private static final class R implements GLSurfaceView.Renderer {
    private final String VS="attribute vec3 p;attribute vec3 n;uniform mat4 m;varying vec3 q;void main(){q=n;gl_Position=m*vec4(p,1.0);}";
    private final String FS="precision mediump float;uniform vec4 c;uniform vec3 l;varying vec3 q;void main(){float d=max(dot(normalize(q),normalize(l)),0.0);gl_FragColor=vec4(c.rgb*(0.28+d*.72),c.a);}";
    private FloatBuffer cube;
    private int pr,ap,an,um,uc,ul;
    private final float[] P=new float[16],V=new float[16],VP=new float[16],M=new float[16],MVP=new float[16];
    private final List<Obj> objs=new ArrayList<>(),traffic=new ArrayList<>(); private final List<Enemy> enemies=new ArrayList<>();
    private final Random rnd=new Random(77); private final Random eventRnd=new Random(20261005L);
    float x=0,z=4,yaw=0,spd=0,time=10.5f,fps=60,missionTime=0,playerHealth=100,combatCooldown=0,crimeCooldown=0,robberyTimer=0; int missionMilestone=0;
    boolean gas,brake,left,right,onFoot,robberyRunning; int cash=12500,wanted=0,quality=1,vehicle=0,camera=0,defeated=0,robberyReward=0;
    private long last=0,fs=0;private int fc=0; private float wantedT=0;

    R(){
      for(int i=0;i<42;i++)objs.add(new Obj(-95+rnd.nextFloat()*190,-40+rnd.nextFloat()*83,5+rnd.nextFloat()*4,8+rnd.nextFloat()*18));
      for(int i=0;i<18;i++)traffic.add(new Obj(-82+rnd.nextFloat()*164,-28+rnd.nextFloat()*58,3.8f,6));
      for(int i=0;i<10;i++)enemies.add(new Enemy(-70+(i%5)*35,18+(i/5)*17,i%4));
    }
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig c){
      GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glEnable(GLES20.GL_CULL_FACE);
      pr=link(shader(GLES20.GL_VERTEX_SHADER,VS),shader(GLES20.GL_FRAGMENT_SHADER,FS));
      ap=GLES20.glGetAttribLocation(pr,"p");an=GLES20.glGetAttribLocation(pr,"n");
      um=GLES20.glGetUniformLocation(pr,"m");uc=GLES20.glGetUniformLocation(pr,"c");ul=GLES20.glGetUniformLocation(pr,"l");
      cube=makeCube();last=System.nanoTime();fs=SystemClock.uptimeMillis();
    }
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 g,int w,int h){
      GLES20.glViewport(0,0,w,h);Matrix.perspectiveM(P,0,60,Math.max(.1f,w/(float)Math.max(1,h)),.1f,260);
    }
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 g){
      long n=System.nanoTime();float dt=Math.min(.04f,Math.max(.001f,(n-last)/1e9f));last=n;update(dt);
      fc++;long ms=SystemClock.uptimeMillis();if(ms-fs>1000){fps=fc*1000f/(ms-fs);fc=0;fs=ms;}
      float dl=.2f+(float)Math.max(0,Math.sin((time-7)/12*Math.PI))*.8f;
      GLES20.glClearColor(.03f+.08f*dl,.05f+.10f*dl,.07f+.13f*dl,1);GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
      GLES20.glUseProgram(pr);GLES20.glUniform3f(ul,-.3f,1,-.45f);
      box(0,-1,10,115,1,140,new float[]{.14f,.21f,.16f,1});
      box(0,-.42f,10,105,.2f,58,new float[]{.09f,.10f,.12f,1});
      box(0,-.30f,7,105,.14f,8,new float[]{.04f,.05f,.06f,1});
      box(-34,-.30f,15,8,.14f,54,new float[]{.04f,.05f,.06f,1});
      box(37,-.30f,18,9,.14f,56,new float[]{.04f,.05f,.06f,1});
      for(int i=-8;i<9;i++)box(i*11,-.21f,7,2,.03f,.14f,new float[]{.75f,.65f,.18f,1});
      box(0,-.2f,68,105,.2f,10,new float[]{.38f,.29f,.20f,1});
      box(0,-.78f,96,115,.12f,43,new float[]{.03f,.25f,.36f,1});
      for(int i=0;i<13;i++)box((float)Math.sin(time+i)*1.2f,-.69f,77+i*2.8f,86,.025f,.06f,new float[]{.20f,.58f,.68f,1});
      box(-74,-.28f,-84,32,.12f,9,new float[]{.07f,.08f,.09f,1});box(-102,.6f,-92,4,2,28,new float[]{.32f,.34f,.37f,1});
      int bmax=quality==0?18:(quality==1?32:objs.size());
      for(int i=0;i<bmax;i++){Obj o=objs.get(i);box(o.x,o.h/2,o.z,o.w,o.h,o.w*.82f,new float[]{.25f+.1f*dl,.27f+.1f*dl,.31f+.1f*dl,1});box(o.x,o.h*.52f,o.z-o.w*.43f,o.w*.48f,o.h*.30f,.04f,new float[]{.06f,.12f,.16f,1});}
      for(int i=0;i<(quality==0?22:40);i++){Obj o=objs.get((i*3)%objs.size());box(o.x+6,o.h*.20f,o.z+7,.45f,3.2f,.45f,new float[]{.24f,.14f,.08f,1});box(o.x+6,4,o.z+7,3.8f,3.8f,3.8f,new float[]{.07f,.30f,.13f,1});}
      for(Obj o:traffic)car(o.x,o.z,0);
      for(Enemy e:enemies) enemy(e);
      for(int i=0;i<5;i++){Obj q=new Obj(-50+i*20,-28-i*5,3.9f,6);if(wanted>=2){float dx=x-q.x,dz=z-q.z,l=(float)Math.hypot(dx,dz)+.01f;q.x+=dx/l*(6+wanted*1.7f)*dt;q.z+=dz/l*(6+wanted*1.7f)*dt;}police(q.x,q.z);}
      if(onFoot)player();else car(x,z,vehicle);
    }
    void update(float dt){
      float tar=gas?(15+vehicle*1.2f):0;if(brake)tar=-7;
      spd+=(tar-spd)*Math.min(1,dt*4);if(!gas&&!brake)spd*=Math.pow(.78,dt*10);
      yaw+=((right?1:0)-(left?1:0))*(.55+Math.abs(spd)*.018)*dt;
      float k=onFoot?.35f:1; x+=Math.sin(yaw)*spd*dt*k;z+=Math.cos(yaw)*spd*dt*k;x=cl(x,-106,106);z=cl(z,-110,110);
      if(wanted>0){wantedT-=dt;if(wantedT<=0&&Math.abs(spd)<2){wanted--;wantedT=3.2f;}}
      missionTime+=dt;
      combatCooldown=Math.max(0,combatCooldown-dt);
      int milestone = missionTime>=120?4:(missionTime>=84?3:(missionTime>=52?2:(missionTime>=24?1:0)));
      if(milestone>missionMilestone){ cash+=missionReward(milestone); missionMilestone=milestone; }
      int tier=difficultyTier();
      for(Enemy e:enemies){
        if(e.defeated) continue;
        float dx=x-e.x,dz=z-e.z,dist=(float)Math.hypot(dx,dz);

        if(wanted<=0){
          e.state=0; // CALM
          e.x+=Math.sin(e.phase+time)*.18f*dt;
          e.z+=Math.cos(e.phase+time)*.18f*dt;
          continue;
        }

        if(wanted==1){
          e.state=1; // ALERT: watches and keeps distance
          if(dist<20f && dist>.05f){
            float len=dist;
            e.x-=dx/len*(1.0f+tier*.08f)*dt;
            e.z-=dz/len*(1.0f+tier*.08f)*dt;
          }
        } else {
          e.state=2; // CHASE
          if(dist<30f && dist>.05f){
            float len=dist;
            float enemySpeed=1.25f+e.type*.30f+tier*.22f;
            e.x+=dx/len*enemySpeed*dt;
            e.z+=dz/len*enemySpeed*dt;
          }
          if(dist<2.6f && combatCooldown<=0){
            playerHealth-=6+tier*2;
            combatCooldown=.85f;
            if(playerHealth<=0){
              playerHealth=100;
              cash=Math.max(0,cash-450-tier*120);
              x=0;z=4;spd=0;wanted=0;
            }
          }
        }
      }
      crimeCooldown=Math.max(0,crimeCooldown-dt);
      if(robberyRunning){
        robberyTimer-=dt;
        if(robberyTimer<=0) finishRobbery();
      }
      time+=dt*.18;if(time>=24)time-=24;
      for(Obj o:traffic){o.z+=(o.x<0?1:-1)*o.h*dt;if(o.z>65)o.z=-60;if(o.z<-65)o.z=65;}
    }
    void car(float X,float Z,int t){float[] c=color(t);box(X,.58f,Z,4.2f,.95f,6.7f,c);box(X,1.25f,Z-.2f,3,.8f,3.35f,new float[]{.04f,.07f,.09f,1});for(int sx:new int[]{-1,1})for(int sz:new int[]{-1,1})box(X+sx*1.7f,.38f,Z+sz*2.35f,.45f,.58f,1.05f,new float[]{.02f,.02f,.02f,1});}
    void enemy(Enemy e){
      if(e.defeated)return;
      int tier=difficultyTier();
      float[] body;
      switch(e.type){
        case 3: body=new float[]{.30f,.16f,.34f,1}; break;
        case 2: body=new float[]{.20f,.31f,.22f,1}; break;
        case 1: body=new float[]{.36f,.27f,.12f,1}; break;
        default: body=new float[]{.10f,.15f,.20f,1};
      }
      float scale=1f+Math.min(.18f,tier*.025f);
      box(e.x,1.05f*scale,e.z,.9f*scale,1.8f*scale,.62f*scale,body);
      box(e.x,2.2f*scale,e.z,.50f*scale,.55f*scale,.50f*scale,new float[]{.55f,.38f,.28f,1});
      box(e.x-.32f*scale,1.02f*scale,e.z,.24f*scale,1.45f*scale,.30f*scale,new float[]{.055f,.06f,.07f,1});
      box(e.x+.32f*scale,1.02f*scale,e.z,.24f*scale,1.45f*scale,.30f*scale,new float[]{.055f,.06f,.07f,1});
      if(e.type>=2) box(e.x,2.48f*scale,e.z,.64f*scale,.12f*scale,.56f*scale,new float[]{.12f,.14f,.16f,1});
      float hp=Math.max(0,e.hp)/(float)Math.max(1,e.maxHp);
      box(e.x,2.72f*scale,e.z,1.0f*scale,.07f*scale,.08f*scale,new float[]{.07f,.07f,.08f,1});
      box(e.x-.5f*scale+hp*.5f*scale,2.73f*scale,e.z,hp*1.0f*scale,.09f*scale,.09f*scale,new float[]{.30f,.72f,.35f,1});
    }
    void police(float X,float Z){car(X,Z,5);box(X,1.75f,Z,1,.15f,.62f,new float[]{.08f,.20f,.78f,1});}
    void player(){box(x,1.1f,z,1,1.9f,.65f,new float[]{.10f,.28f,.50f,1});box(x,2.25f,z,.55f,.58f,.55f,new float[]{.62f,.42f,.30f,1});box(x-.35f,1.1f,z,.28f,1.5f,.32f,new float[]{.06f,.07f,.08f,1});box(x+.35f,1.1f,z,.28f,1.5f,.32f,new float[]{.06f,.07f,.08f,1});}
    void box(float X,float Y,float Z,float sx,float sy,float sz,float[] col){
      Matrix.setIdentityM(M,0);Matrix.translateM(M,0,X,Y,Z);Matrix.scaleM(M,0,sx/2,sy/2,sz/2);
      float ex,ey,ez,cx,cy,cz;
      if(camera==2){ex=x;ey=2.1f;ez=z-.8f;cx=x+(float)Math.sin(yaw)*15;cy=1.8f;cz=z+(float)Math.cos(yaw)*15;}
      else {float d=camera==1?7.5f:11.5f;ex=x-(float)Math.sin(yaw)*d;ey=camera==1?4:6.2f;ez=z-(float)Math.cos(yaw)*d;cx=x;cy=1;cz=z;}
      Matrix.setLookAtM(V,0,ex,ey,ez,cx,cy,cz,0,1,0);Matrix.multiplyMM(VP,0,P,0,V,0);Matrix.multiplyMM(MVP,0,VP,0,M,0);
      GLES20.glUniformMatrix4fv(um,1,false,MVP,0);GLES20.glUniform4fv(uc,1,col,0);
      cube.position(0);GLES20.glEnableVertexAttribArray(ap);GLES20.glVertexAttribPointer(ap,3,GLES20.GL_FLOAT,false,24,cube);
      cube.position(3);GLES20.glEnableVertexAttribArray(an);GLES20.glVertexAttribPointer(an,3,GLES20.GL_FLOAT,false,24,cube);
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,36);GLES20.glDisableVertexAttribArray(ap);GLES20.glDisableVertexAttribArray(an);
    }
    FloatBuffer makeCube(){float[]v={-1,-1,-1,0,0,-1,1,-1,-1,0,0,-1,1,1,-1,0,0,-1,-1,-1,-1,0,0,-1,1,1,-1,0,0,-1,-1,1,-1,0,0,-1,-1,-1,1,0,0,1,1,1,1,0,0,1,1,-1,1,0,0,1,-1,-1,1,0,0,1,-1,1,1,0,0,1,1,1,1,0,0,1,-1,-1,-1,-1,0,0,-1,1,1,-1,0,0,-1,-1,1,-1,-1,0,0,-1,-1,-1,-1,-1,0,0,-1,1,-1,-1,-1,0,0,-1,1,1,-1,-1,0,0,1,-1,-1,1,1,0,0,1,1,1,1,1,0,0,1,-1,-1,1,1,0,0,1,1,1,1,1,0,0,1,1,1,-1,1,0,0,-1,1,-1,0,1,0,1,1,-1,0,1,0,1,1,1,0,1,0,-1,1,-1,0,1,0,1,1,1,0,1,0,-1,1,1,0,1,0,-1,-1,-1,0,-1,0,-1,-1,1,0,-1,0,1,-1,1,0,-1,0,-1,-1,-1,0,-1,0,1,-1,1,0,-1,0,1,-1,-1,0,-1,0};ByteBuffer b=ByteBuffer.allocateDirect(v.length*4).order(ByteOrder.nativeOrder());FloatBuffer f=b.asFloatBuffer();f.put(v).position(0);return f;}
    int shader(int type,String s){int q=GLES20.glCreateShader(type);GLES20.glShaderSource(q,s);GLES20.glCompileShader(q);return q;}
    int link(int a,int b){int q=GLES20.glCreateProgram();GLES20.glAttachShader(q,a);GLES20.glAttachShader(q,b);GLES20.glLinkProgram(q);return q;}
    float[] color(int i){switch(i%8){case 1:return new float[]{.16f,.44f,.92f,1};case 2:return new float[]{.12f,.44f,.20f,1};case 3:return new float[]{.70f,.35f,.16f,1};case 4:return new float[]{.72f,.74f,.78f,1};case 5:return new float[]{.16f,.22f,.19f,1};case 6:return new float[]{.72f,.14f,.13f,1};case 7:return new float[]{.12f,.56f,.66f,1};default:return new float[]{.72f,.18f,.15f,1};}}

    int difficultyTier(){
      int t=1+(int)(missionTime/28f);
      return Math.max(1,Math.min(6,t));
    }
    String difficultyName(){
      String[] n={"מתחיל","חובב","מנוסה","קשוח","עילית","אגדי"};
      return n[difficultyTier()-1];
    }
    int rewardFor(int enemyType){
      int tier=difficultyTier();
      return 300 + tier*180 + enemyType*120;
    }
    int missionReward(int milestone){
      switch(milestone){case 1:return 1500;case 2:return 2600;case 3:return 4200;case 4:return 6800;default:return 0;}
    }
    void commitCrimeEvent(){
      if(crimeCooldown>0) return;
      crimeCooldown=2.2f;
      wanted=Math.min(5,wanted+1);
      wantedT=7.0f;
    }
    void startRobbery(){
      if(robberyRunning) return;
      robberyRunning=true;
      robberyTimer=4.2f;
      int tier=difficultyTier();
      robberyReward=700+tier*260;
      wanted=Math.min(5,wanted+1);
      wantedT=8.0f;
    }
    void finishRobbery(){
      if(!robberyRunning) return;
      robberyRunning=false;
      cash+=robberyReward;
      robberyReward=0;
      wanted=Math.min(5,wanted+(eventRnd.nextInt(4)==0?1:0));
      wantedT=7.5f;
    }
    int missionReward(){
      if(missionTime<24f)return 1500;
      if(missionTime<52f)return 2600;
      if(missionTime<84f)return 4200;
      if(missionTime<120f)return 6800;
      return 9500;
    }
    float health(){return playerHealth;}
    float cl(float v,float a,float b){return Math.max(a,Math.min(b,v));}
    String vname(){return new String[]{"Urban GT","Roadster X","Rally 4x4","Heavy Truck","Aero Moto","Armored SUV","Sea Runner","Sky Heli"}[vehicle];}
    int[] prices(){return new int[]{0,22000,32000,46000,14000,68000,28000,125000};}
    int difficulty(){return difficultyTier();} String difficultyText(){return difficultyName();} int defeated(){return defeated;} boolean robberyRunning(){return robberyRunning;} int robberyReward(){return robberyReward;}
    String mission(){if(wanted>0)return"מרדף פעיל • הימלט מהאזור";if(missionTime<24)return"משימת פתיחה • היכרות עם העיר";if(missionTime<52)return"מרוץ שכונתי • השג את נקודת הסיום";if(missionTime<84)return"סיור בנמל • הגעה לרציף";if(missionTime<120)return"קו החוף • חקור את האזור";return"עולם פתוח • בחר יעד משלך";}
    void setGas(boolean b){gas=b;}void setBrake(boolean b){brake=b;}void setLeft(boolean b){left=b;}void setRight(boolean b){right=b;}
    void toggleCamera(){camera=(camera+1)%3;}void enterExit(){onFoot=!onFoot;spd=0;}void trigger(){startRobbery();} void robbery(){startRobbery();}
    void quality(int q){quality=Math.max(0,Math.min(2,q));}boolean buy(int i){int[]p=prices();if(i==vehicle)return true;if(cash<p[i])return false;cash-=p[i];vehicle=i;spd=0;return true;}
    static final class Obj{float x,z,w,h;Obj(float x,float z,float w,float h){this.x=x;this.z=z;this.w=w;this.h=h;}}
    static final class Enemy{float x,z;int type,hp,maxHp,state=0;float phase;boolean defeated=false;Enemy(float x,float z,int type){this.x=x;this.z=z;this.type=type;this.maxHp=1+type;this.hp=maxHp;this.phase=x*.11f+z*.07f;}}
  }

  private static final class HUD extends View {
    final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final R r;int mode=0;boolean intro=true;
    HUD(Context c,R r){super(c);this.r=r;setFocusable(true);}
    protected void onDraw(Canvas c){
      int w=getWidth(),h=getHeight();
      if(mode==0){home(c,w,h);return;}
      hud(c,w,h);if(mode==2)garage(c,w,h);if(mode==3)map(c,w,h);if(mode==4)settings(c,w,h);if(intro)intro(c,w,h);postInvalidateDelayed(150);
    }
    void home(Canvas c,int w,int h){
      p.setShader(new LinearGradient(0,0,w,h,Color.rgb(8,13,18),Color.rgb(38,54,67),Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
      t(c,"ROAD",48,88,24,Color.LTGRAY);t(c,"LEGENDS",48,142,54,Color.WHITE);t(c,"עולם פתוח תלת־ממדי",50,174,19,Color.rgb(186,205,219));
      round(c,w*.56f,h*.31f,w*.92f,h*.70f,0x5530424F,28);t(c,"CITY  //  HARBOR  //  WILDS",w*.59f,h*.42f,14,Color.rgb(224,188,76));t(c,"עולם חי. נהיגה. משימות.",w*.59f,h*.50f,24,Color.WHITE);t(c,"תאורת יום/לילה • מפה • מוסך • אירועים",w*.59f,h*.55f,14,Color.LTGRAY);
      chip(c,48,h-135,"₪ "+money(r.cash),"יתרה");chip(c,190,h-135,"FPS "+Math.round(r.fps),"ביצועים");chip(c,332,h-135,"Android 9+","תאימות");
      primary(c,w-292,h-178,235,58,"התחל משחק");secondary(c,w-292,h-108,108,48,"מוסך");secondary(c,w-167,h-108,108,48,"הגדרות");t(c,"עברית RTL • שליטה ברורה • שמירה והתקדמות",50,h-28,14,Color.LTGRAY);t(c,"כלכלה: משימות → תגמולים → רכבים → שדרוגים",50,h-52,13,Color.rgb(205,184,126));
    }
    void hud(Canvas c,int w,int h){
      round(c,18,16,w-18,94,0xC0091015,22);t(c,"ROAD LEGENDS",38,47,19,Color.WHITE);t(c,r.vname(),38,73,15,Color.LTGRAY);t(c,"₪ "+money(r.cash),w-190,48,22,Color.WHITE);t(c,Math.round(Math.abs(r.spd)*7.2f)+" קמ״ש",w-190,74,14,Color.LTGRAY);t(c,"קושי "+r.difficultyText(),w/2f-42,70,14,Color.LTGRAY);
      String want=r.wanted==0?"הכול רגוע":"חיפוש "+"★ ".repeat(Math.min(5,r.wanted));t(c,want,w/2f-42,49,15,r.wanted==0?Color.rgb(150,184,160):Color.rgb(255,214,74));
      round(c,18,110,425,181,0xB20E151C,18);t(c,"המשימה הפעילה",38,136,13,Color.rgb(117,164,201));t(c,r.mission(),38,163,15,Color.WHITE);t(c,"יריבים פעילים: "+(10-r.defeated())+"   •   קושי: "+r.difficultyText(),38,184,12,Color.rgb(208,170,105));if(r.robberyRunning())t(c,"שוד וירטואלי פעיל • תגמול ₪ "+money(r.robberyReward()),38,202,12,Color.rgb(244,214,106));
      ctl(c,28,h-118,72,62,"◀");ctl(c,112,h-162,72,62,"▲");ctl(c,112,h-74,72,62,"▼");ctl(c,196,h-118,72,62,"▶");
      ctl(c,w-365,h-118,90,62,r.onFoot?"רכב":"יציאה");ctl(c,w-263,h-118,90,62,"מוסך");ctl(c,w-161,h-118,90,62,"מפה");
      sml(c,w-365,h-50,90,42,"שחקן");sml(c,w-263,h-50,90,42,"מצלמה");sml(c,w-161,h-50,90,42,r.robberyRunning()?"מתבצע":"שוד כסף");t(c,"חיים "+Math.round(r.health())+"%   •   תגמול משימה ₪ "+money(r.missionReward()),w/2f-210,h-42,13,Color.LTGRAY);t(c,"פעילות אסורה מעלה את רמת החיפוש • ▲ תאוצה   ▼ בלימה   ◀ ▶ היגוי",w/2f-205,h-18,12,Color.LTGRAY);
    }
    void intro(Canvas c,int w,int h){fill(c,0x77000000);c.drawRect(0,0,w,h,p);round(c,w/2f-265,h/2f-96,w/2f+265,h/2f+96,0xF01A222A,28);t(c,"ברוכים הבאים ל־ROAD LEGENDS",w/2f-212,h/2f-38,23,Color.WHITE);t(c,"תלת־ממד • עיר • נמל • שטח • שדה תעופה",w/2f-190,h/2f-5,15,Color.LTGRAY);t(c,"התחל במשימת הפתיחה, פגוש יריבים והתקדם לרכבים ולמוסך",w/2f-220,h/2f+25,15,Color.LTGRAY);primary(c,w/2f-105,h/2f+46,210,50,"הבנתי");}
    void garage(Canvas c,int w,int h){fill(c,0xA8000000);c.drawRect(0,0,w,h,p);round(c,26,24,w-26,h-24,0xF019222A,28);t(c,"המוסך שלי",52,68,30,Color.WHITE);t(c,"קנה והחלף כלי תחבורה",52,95,14,Color.LTGRAY);String[] n={"Urban GT","Roadster X","Rally 4x4","Heavy Truck","Aero Moto","Armored SUV","Sea Runner","Sky Heli"};String[] s={"ספורט","מרוץ","שטח","משאית","אופנוע","ממוגן","כלי שיט","מסוק"};float cw=(w-112)/4f;for(int i=0;i<8;i++){int col=i%4,row=i/4;float x=50+col*cw,y=118+row*92;round(c,x,y,x+cw-14,y+76,i==r.vehicle?0xFF314B60:0xFF202830,16);t(c,n[i],x+12,y+27,14,Color.WHITE);t(c,s[i],x+12,y+49,12,Color.LTGRAY);int q=r.prices()[i];t(c,q==0?"שלך":"₪ "+money(q),x+12,y+68,12,Color.rgb(244,214,106));}primary(c,w-170,h-78,120,48,"חזרה");}
    void map(Canvas c,int w,int h){fill(c,0xEE11181E);c.drawRoundRect(new RectF(28,25,w-28,h-25),28,28,p);t(c,"מפת העולם",54,70,30,Color.WHITE);round(c,56,94,w-56,h-95,Color.rgb(56,79,60),20);fill(c,Color.rgb(42,53,58));c.drawRect(56,h/2-24,w-56,h/2+24,p);fill(c,Color.rgb(36,105,138));c.drawRect(56,h-215,w-56,h-95,p);fill(c,Color.rgb(83,90,93));c.drawRect(w-280,110,w-86,h-252,p);mark(c,94,h/2,Color.WHITE,"אתה");mark(c,w-182,156,Color.rgb(245,185,70),"שדה");mark(c,w/2,h-155,Color.rgb(88,202,231),"נמל");primary(c,w-170,h-78,120,48,"חזרה");}
    void settings(Canvas c,int w,int h){fill(c,0xEE151C22);c.drawRoundRect(new RectF(58,35,w-58,h-35),28,28,p);t(c,"הגדרות",88,84,30,Color.WHITE);t(c,"איכות גרפיקה",88,130,16,Color.LTGRAY);secondary(c,88,148,108,46,"ביצועים");secondary(c,208,148,108,46,"גבוהה");secondary(c,328,148,108,46,"אולטרה");t(c,"מצלמה: "+(r.camera==0?"רחוקה":r.camera==1?"קרובה":"תא נהג"),88,238,17,Color.WHITE);t(c,"Android 9 ומעלה • טעינת עולם חכמה • FPS יציב",88,276,14,Color.LTGRAY);primary(c,w-190,h-90,130,50,"חזרה");}
    void chip(Canvas c,float x,float y,String a,String b){round(c,x,y,x+128,y+62,0x5526323A,18);t(c,a,x+13,y+27,17,Color.WHITE);t(c,b,x+13,y+49,12,Color.LTGRAY);}
    void ctl(Canvas c,float x,float y,float w,float h,String s){round(c,x,y,x+w,y+h,0xD01A242D,18);center(c,s,x+w/2,y+h/2+8,23,Color.WHITE);}
    void sml(Canvas c,float x,float y,float w,float h,String s){round(c,x,y,x+w,y+h,0xD01A242D,15);t(c,s,x+12,y+h/2+6,13,Color.WHITE);}
    void primary(Canvas c,float x,float y,float w,float h,String s){round(c,x,y,x+w,y+h,0xFFE3B84B,17);center(c,s,x+w/2,y+h/2+6,16,Color.rgb(18,20,22));}
    void secondary(Canvas c,float x,float y,float w,float h,String s){round(c,x,y,x+w,y+h,0xD01A242D,17);center(c,s,x+w/2,y+h/2+6,14,Color.WHITE);}
    void mark(Canvas c,float x,float y,int col,String s){fill(c,col);c.drawCircle(x,y,9,p);t(c,s,x+14,y+5,13,Color.WHITE);}
    void round(Canvas c,float a,float b,float d,float e,int col,float rad){fill(c,col);c.drawRoundRect(new RectF(a,b,d,e),rad,rad,p);}
    void fill(Canvas c,int col){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);}
    void t(Canvas c,String s,float x,float y,float sz,int col){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextSize(sz);p.setColor(col);p.setStyle(Paint.Style.FILL);p.setShader(null);c.drawText(s,x,y,p);}
    void center(Canvas c,String s,float x,float y,float sz,int col){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextSize(sz);p.setColor(col);c.drawText(s,x-p.measureText(s)/2,y,p);}
    String money(int n){return String.format(Locale.US,"%,d",n);}
    public boolean onTouchEvent(MotionEvent e){
      float x=e.getX(),y=e.getY();int w=getWidth(),h=getHeight();boolean up=e.getAction()==MotionEvent.ACTION_UP;
      if(e.getAction()!=MotionEvent.ACTION_DOWN&& !up)return true;
      if(mode==0&&up){if(x>w-305&&y>h-205&&y<h-110){mode=1;intro=true;}else if(x>w-305&&y>h-115){mode=2;}else if(x>w-175&&y>h-115){mode=4;}invalidate();return true;}
      if(mode==1){if(intro){if(up&&y>h/2){intro=false;invalidate();}return true;}if(up){stop();if(y>h-90&&x>w-280&&x<w-160)r.toggleCamera();else if(y>h-90&&x>w-170&&x<w-65)r.robbery();else if(y>h-90&&x>w-375&&x<w-275)r.enterExit();else if(y>h-150&&x>w-370&&x<w-270)r.enterExit();else if(y>h-150&&x>w-265&&x<w-175)mode=2;else if(y>h-150&&x>w-170&&x<w-70)mode=3;invalidate();return true;}held(x,y,h);}
      if(mode==2&&up){if(y>h-100){mode=1;invalidate();return true;}float cw=(w-112)/4f;for(int i=0;i<8;i++){int col=i%4,row=i/4;float bx=50+col*cw,by=118+row*92;if(x>=bx&&x<=bx+cw-14&&y>=by&&y<=by+76){r.buy(i);invalidate();return true;}}}
      if((mode==3||mode==4)&&up&&y>h-110){mode=1;invalidate();return true;}
      if(mode==4&&up&&y>=145&&y<=205){r.quality(Math.max(0,Math.min(2,(int)((x-88)/120))));invalidate();return true;}return true;
    }
    void held(float x,float y,int h){if(x<105)r.setLeft(true);else if(x>185&&x<280)r.setRight(true);else if(x>=100&&x<=185&&y<h-108)r.setGas(true);else if(x>=100&&x<=185)r.setBrake(true);}
    void stop(){r.setLeft(false);r.setRight(false);r.setGas(false);r.setBrake(false);}
    protected void onDetachedFromWindow(){stop();super.onDetachedFromWindow();}
  }
}
