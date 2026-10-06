package com.example.contacts.widget;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public class AlphaIndexBar extends TextView {
    public AlphaIndexBar(Context c) {
        super(c);
        setFocusable(false);
        setClickable(false);
        setTextSize(14);
        setGravity(Gravity.CENTER);
        setTextColor(Color.rgb(0x9A,0xA3,0xAF));
        setText("א\nב\nג\nד\nה\nו\nז\nח\nט\nי\nכ\nל\nמ\nנ\nס\nע\nפ\nצ\nק\nר\nש\nת\nA\nB\nC\nD\nE\nF\nG\nH\nI\nJ\nK\nL\nM\nN\nO\nP\nQ\nR\nS\nT\nU\nV\nW\nX\nY\nZ");
    }
}
