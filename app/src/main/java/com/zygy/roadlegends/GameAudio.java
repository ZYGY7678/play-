package com.zygy.roadlegends;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Tiny procedural sound layer: no bundled copyrighted recordings and no
 * external files. Sounds are short game effects only.
 */
public final class GameAudio {
    private static final int RATE=22050;
    private static final class Clip { final short[] pcm; Clip(short[] pcm){this.pcm=pcm;} }
    private final BlockingQueue<Clip> queue=new LinkedBlockingQueue<>();
    private final Thread worker;
    private volatile boolean running=true;
    private final AudioTrack track;

    public GameAudio(){
        AudioAttributes aa=new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        AudioFormat af=new AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build();
        int min=AudioTrack.getMinBufferSize(RATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
        track=new AudioTrack(aa,af,Math.max(min,RATE/2),AudioTrack.MODE_STREAM,0);
        track.play();
        worker=new Thread(new Runnable(){
            @Override public void run(){
                while(running){
                    try{
                        Clip c=queue.take();
                        if(!running) break;
                        track.write(c.pcm,0,c.pcm.length,AudioTrack.WRITE_BLOCKING);
                    }catch(InterruptedException ignored){}
                }
            }
        },"RoadLegendsAudio");
        worker.setDaemon(true);
        worker.start();
    }

    public void playShot(boolean heavy){ enqueue(shot(heavy)); }
    public void playFootstep(boolean runningNow){ enqueue(footstep(runningNow)); }
    public void playShout(){ enqueue(shout()); }
    public void playDoor(){ enqueue(door()); }

    private void enqueue(short[] pcm){
        while(queue.size()>4) queue.poll();
        queue.offer(new Clip(pcm));
    }

    private short[] shot(boolean heavy){
        int n=(int)(RATE*(heavy?.20:.14));
        short[] out=new short[n];
        long seed=System.nanoTime();
        for(int i=0;i<n;i++){
            seed^=seed<<13;seed^=seed>>>7;seed^=seed<<17;
            float noise=((seed&0xFFFF)/32768f)-1f;
            float t=i/(float)n, env=(1f-t)*(1f-t);
            float thump=(float)Math.sin(i*2*Math.PI*(heavy?95:125)/RATE)*env*.55f;
            out[i]=(short)Math.max(-32767,Math.min(32767,(noise*(heavy?.72f:.55f)+thump)*env*25000));
        }
        return out;
    }

    private short[] footstep(boolean runningNow){
        int n=(int)(RATE*(runningNow?.095:.07));
        short[] out=new short[n];
        long seed=System.nanoTime();
        for(int i=0;i<n;i++){
            seed^=seed<<13;seed^=seed>>>7;seed^=seed<<17;
            float noise=((seed&0xFFFF)/32768f)-1f;
            float t=i/(float)n;
            float env=(1f-t)*(1f-t);
            float tone=(float)Math.sin(2*Math.PI*(runningNow?78:62)*i/RATE);
            out[i]=(short)((noise*.70f+tone*.30f)*env*15000);
        }
        return out;
    }

    private short[] shout(){
        int n=(int)(RATE*.24);
        short[] out=new short[n];
        for(int i=0;i<n;i++){
            float t=i/(float)n;
            float env=(float)Math.sin(Math.PI*t)*.75f;
            float f=390f+110f*(float)Math.sin(2*Math.PI*t*2.1f);
            float v=(float)Math.sin(2*Math.PI*f*i/RATE)*env;
            out[i]=(short)(v*9000);
        }
        return out;
    }

    private short[] door(){
        int n=(int)(RATE*.16);
        short[] out=new short[n];
        for(int i=0;i<n;i++){
            float t=i/(float)n;
            float env=(1f-t)*.65f;
            float v=(float)Math.sin(2*Math.PI*(180+140*t)*i/RATE)*env;
            out[i]=(short)(v*7000);
        }
        return out;
    }

    public void release(){
        running=false;
        worker.interrupt();
        try{worker.join(100);}catch(InterruptedException ignored){}
        try{track.stop();}catch(Exception ignored){}
        try{track.release();}catch(Exception ignored){}
        queue.clear();
    }
}
