package com.zygy.roadlegends;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random(7);
    private float px = 0, py = 0, vx = 0, vy = 0;
    private boolean inCar = true;
    private int vehicleIndex = 0;
    private int cash = 25000;
    private int wanted = 0;
    private int heatTimer = 0;
    private int mode = 0; // 0 driving, 1 garage, 2 inventory, 3 jail
    private float zoom = 1.0f;
    private long last;
    private final List<Entity> entities = new ArrayList<>();

    private static final String[] VEHICLES = {
            "Urban GT", "Roadster X", "4x4 Ranger", "Road Hauler",
            "Trail Bike", "Armor Van", "Speed Boat", "Sea Runner", "Heli Scout"
    };
    private static final int[] PRICES = {0, 18000, 24000, 32000, 8500, 65000, 22000, 27000, 120000};
    private static final String[] GEAR = {"אקדח", "רובה", "RPG", "רימון", "מצנח", "מפתח גנוב"};

    public GameView(Context c) {
        super(c);
        setFocusable(true);
        last = System.currentTimeMillis();
        buildWorld();
    }

    private void buildWorld() {
        entities.clear();
        for (int i = 0; i < 26; i++) {
            float x = -1400 + random.nextInt(2800);
            float y = -900 + random.nextInt(1800);
            int t = i % 7;
            entities.add(new Entity(x, y, t));
        }
        for (int i = 0; i < 10; i++) {
            float x = -1200 + i * 260;
            entities.add(new Entity(x, 520, 8)); // sea craft near harbor
        }
        entities.add(new Entity(-950, -500, 9)); // airport
        entities.add(new Entity(800, -550, 10)); // military base
        entities.add(new Entity(0, 0, 11)); // player area
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        long now = System.currentTimeMillis();
        float dt = Math.min(0.04f, (now - last) / 1000f);
        last = now;

        if (mode != 3) update(dt);

        drawWorld(c);
        drawVehicle(c);
        drawPlayer(c);
        drawHud(c);

        if (mode == 1) drawGarage(c);
        if (mode == 2) drawInventory(c);
        if (mode == 3) drawJail(c);

        postInvalidateDelayed(16);
    }

    private void update(float dt) {
        px += vx * dt;
        py += vy * dt;
        vx *= 0.90f;
        vy *= 0.90f;

        if (heatTimer > 0) {
            heatTimer -= (int)(dt * 1000);
            if (heatTimer <= 0) {
                heatTimer = 0;
                wanted = Math.max(0, wanted - 1);
            }
        }

        if (wanted > 0 && (Math.abs(px) > 1800 || Math.abs(py) > 1300)) {
            wanted = 0;
            heatTimer = 0;
        }

        // Gentle world bounds.
        px = Math.max(-2000, Math.min(2000, px));
        py = Math.max(-1200, Math.min(1200, py));
    }

    private void drawWorld(Canvas c) {
        float w = getWidth(), h = getHeight();
        c.drawColor(Color.rgb(18, 27, 22));

        c.save();
        c.translate(w / 2f - px * zoom, h / 2f - py * zoom);
        c.scale(zoom, zoom);

        // Water zone.
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(35, 96, 126));
        c.drawRect(-2100, 350, 2100, 1500, p);

        // Airport runway.
        p.setColor(Color.rgb(60, 64, 68));
        c.drawRect(-1250, -730, -450, -220, p);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(8);
        for (int i = 0; i < 9; i++) c.drawLine(-1180 + i * 80, -475, -1140 + i * 80, -475, p);

        // Main roads.
        p.setColor(Color.rgb(48, 51, 53));
        c.drawRect(-2100, -120, 2100, 130, p);
        c.drawRect(-120, -1300, 120, 1300, p);
        c.drawRect(-1600, -600, 700, -430, p);
        c.drawRect(500, 250, 2100, 410, p);

        // City blocks.
        p.setColor(Color.rgb(92, 88, 82));
        for (int gx = -1800; gx <= 1600; gx += 360) {
            for (int gy = -950; gy <= 50; gy += 300) {
                if (Math.abs(gx) < 250 || Math.abs(gy) < 200) continue;
                RectF r = new RectF(gx, gy, gx + 230, gy + 180);
                c.drawRoundRect(r, 18, 18, p);
            }
        }

        // Harbor.
        p.setColor(Color.rgb(75, 69, 60));
        c.drawRect(-1350, 260, 450, 480, p);
        p.setColor(Color.rgb(115, 100, 77));
        c.drawRect(-900, 300, -840, 350, p);
        c.drawRect(-500, 300, -440, 350, p);

        // Military base.
        p.setColor(Color.rgb(83, 98, 79));
        c.drawRect(620, -710, 1180, -390, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(12);
        p.setColor(Color.rgb(150, 170, 150));
        c.drawRect(620, -710, 1180, -390, p);
        p.setStyle(Paint.Style.FILL);

        // Entities.
        for (Entity e : entities) drawEntity(c, e);

        c.restore();
    }

    private void drawEntity(Canvas c, Entity e) {
        p.setStyle(Paint.Style.FILL);
        float x = e.x, y = e.y;
        switch (e.type) {
            case 0: drawCar(c, x, y, Color.rgb(210, 60, 60)); break;
            case 1: drawCar(c, x, y, Color.rgb(55, 120, 210)); break;
            case 2: drawCar(c, x, y, Color.rgb(210, 180, 60)); break;
            case 3: drawTruck(c, x, y); break;
            case 4: drawBike(c, x, y); break;
            case 5: drawPolice(c, x, y); break;
            case 6: drawArmor(c, x, y); break;
            case 8: drawBoat(c, x, y); break;
            case 9: drawAirport(c, x, y); break;
            case 10: drawBase(c, x, y); break;
            case 11: break;
        }
    }

    private void drawCar(Canvas c, float x, float y, int col) {
        p.setColor(col);
        c.drawRoundRect(new RectF(x - 55, y - 30, x + 55, y + 30), 15, 15, p);
        p.setColor(Color.DKGRAY);
        c.drawRect(x - 25, y - 22, x + 25, y + 22, p);
        p.setColor(Color.WHITE);
        c.drawCircle(x - 42, y + 25, 9, p);
        c.drawCircle(x + 42, y + 25, 9, p);
    }

    private void drawTruck(Canvas c, float x, float y) {
        p.setColor(Color.rgb(145, 96, 58));
        c.drawRect(x - 72, y - 36, x + 72, y + 36, p);
        p.setColor(Color.rgb(55, 61, 66));
        c.drawRect(x - 95, y - 28, x - 70, y + 30, p);
        c.drawCircle(x - 58, y + 40, 10, p);
        c.drawCircle(x + 52, y + 40, 10, p);
    }

    private void drawBike(Canvas c, float x, float y) {
        p.setColor(Color.WHITE);
        p.setStrokeWidth(8);
        c.drawCircle(x - 28, y + 18, 15, p);
        c.drawCircle(x + 28, y + 18, 15, p);
        c.drawLine(x - 28, y + 18, x, y - 8, p);
        c.drawLine(x, y - 8, x + 28, y + 18, p);
    }

    private void drawPolice(Canvas c, float x, float y) {
        drawCar(c, x, y, Color.rgb(235, 235, 235));
        p.setColor(Color.rgb(210, 50, 50));
        c.drawRect(x - 16, y - 42, x + 2, y - 30, p);
        p.setColor(Color.rgb(50, 90, 220));
        c.drawRect(x + 2, y - 42, x + 20, y - 30, p);
    }

    private void drawArmor(Canvas c, float x, float y) {
        p.setColor(Color.rgb(72, 83, 66));
        c.drawRect(x - 65, y - 40, x + 65, y + 40, p);
        p.setColor(Color.rgb(45, 50, 40));
        c.drawCircle(x + 10, y - 4, 24, p);
        c.drawRect(x + 22, y - 9, x + 75, y + 9, p);
    }

    private void drawBoat(Canvas c, float x, float y) {
        p.setColor(Color.rgb(230, 230, 230));
        Path path = new Path();
        path.moveTo(x - 70, y);
        path.lineTo(x + 60, y);
        path.lineTo(x + 35, y + 38);
        path.lineTo(x - 48, y + 38);
        path.close();
        c.drawPath(path, p);
        p.setColor(Color.rgb(45, 55, 65));
        c.drawRect(x - 18, y - 36, x + 25, y + 2, p);
    }

    private void drawAirport(Canvas c, float x, float y) {
        p.setColor(Color.WHITE);
        c.drawCircle(x, y, 34, p);
        p.setColor(Color.rgb(70, 90, 120));
        c.drawRect(x - 44, y - 10, x + 44, y + 10, p);
        c.drawRect(x - 10, y - 44, x + 10, y + 44, p);
    }

    private void drawBase(Canvas c, float x, float y) {
        p.setColor(Color.rgb(30, 35, 30));
        c.drawRect(x - 55, y - 35, x + 55, y + 35, p);
        p.setColor(Color.rgb(180, 180, 160));
        c.drawRect(x - 8, y - 55, x + 8, y + 55, p);
    }

    private void drawVehicle(Canvas c) {
        if (!inCar) return;
        float cx = getWidth()/2f, cy = getHeight()/2f;
        p.setColor(Color.rgb(215, 65, 55));
        c.drawRoundRect(new RectF(cx - 85, cy - 48, cx + 85, cy + 48), 22, 22, p);
        p.setColor(Color.rgb(30, 35, 42));
        c.drawRoundRect(new RectF(cx - 35, cy - 36, cx + 40, cy + 30), 12, 12, p);
        p.setColor(Color.WHITE);
        c.drawCircle(cx - 65, cy + 48, 12, p);
        c.drawCircle(cx + 65, cy + 48, 12, p);
    }

    private void drawPlayer(Canvas c) {
        if (inCar) return;
        float cx = getWidth()/2f, cy = getHeight()/2f;
        p.setColor(Color.rgb(30, 30, 30));
        c.drawCircle(cx, cy - 32, 18, p);
        c.drawRoundRect(new RectF(cx - 20, cy - 12, cx + 20, cy + 44), 14, 14, p);
    }

    private void drawHud(Canvas c) {
        float w = getWidth(), h = getHeight();
        // Header.
        p.setColor(0xCC0D1117);
        c.drawRect(0, 0, w, 74, p);
        text(c, "ROAD LEGENDS", 28, 48, 27, Color.WHITE);
        text(c, "₪" + cash, w - 160, 45, 23, Color.WHITE);
        if (wanted > 0) {
            text(c, "חיפוש " + "★".repeat(Math.min(5, wanted)), w - 160, 68, 18, Color.rgb(255, 215, 80));
        } else {
            text(c, "הכל שקט", w - 160, 68, 17, Color.LTGRAY);
        }

        // Left control pad.
        button(c, 32, h - 150, 92, 58, "◀");
        button(c, 132, h - 150, 92, 58, "▲");
        button(c, 232, h - 150, 92, 58, "▶");

        // Right actions.
        button(c, w - 350, h - 150, 98, 58, inCar ? "יציאה" : "רכב");
        button(c, w - 245, h - 150, 98, 58, "גראז'");
        button(c, w - 140, h - 150, 108, 58, "ציוד");

        button(c, 32, h - 78, 92, 52, inCar ? "▼" : "↑");
        button(c, 132, h - 78, 92, 52, "זום");
        button(c, 232, h - 78, 92, 52, "אירוע");

        text(c, "עולם פתוח • נהיגה • נמל • שדה תעופה • שטח", 28, 105, 18, Color.WHITE);
    }

    private void drawGarage(Canvas c) {
        overlay(c);
        float w = getWidth(), h = getHeight();
        panel(c, 55, 70, w - 55, h - 85);
        text(c, "מוסך ורכבים", 85, 115, 32, Color.WHITE);
        text(c, "רכב נוכחי: " + VEHICLES[vehicleIndex], 85, 150, 20, Color.LTGRAY);
        int cols = 3;
        for (int i = 0; i < VEHICLES.length; i++) {
            int col = i % cols, row = i / cols;
            float x = 85 + col * 240, y = 195 + row * 105;
            button(c, x, y, 215, 78, VEHICLES[i] + "  ₪" + PRICES[i]);
        }
        button(c, w - 190, h - 130, 120, 58, "סגור");
    }

    private void drawInventory(Canvas c) {
        overlay(c);
        float w = getWidth(), h = getHeight();
        panel(c, 100, 80, w - 100, h - 90);
        text(c, "ציוד", 135, 125, 34, Color.WHITE);
        for (int i = 0; i < GEAR.length; i++) {
            float y = 180 + i * 58;
            button(c, 140, y, 180, 45, GEAR[i]);
            text(c, i == 4 ? "לצניחה/מילוט" : "פריט משחק", 350, y + 30, 17, Color.LTGRAY);
        }
        button(c, w - 210, h - 135, 130, 58, "סגור");
    }

    private void drawJail(Canvas c) {
        overlay(c);
        float w = getWidth(), h = getHeight();
        panel(c, 130, 100, w - 130, h - 110);
        text(c, "המעצר הסתיים", w/2f - 115, 155, 32, Color.WHITE);
        text(c, "המשטרה תפסה אותך והעבירה אותך לכלא.", w/2f - 255, 205, 20, Color.LTGRAY);
        text(c, "הבריחה כאן היא חלק מכניקת המשחק בלבד.", w/2f - 245, 238, 18, Color.LTGRAY);
        button(c, w/2f - 100, h - 200, 200, 70, "שחרור");
    }

    private void overlay(Canvas c) {
        p.setColor(0xAA000000);
        c.drawRect(0,0,getWidth(),getHeight(),p);
    }

    private void panel(Canvas c, float l, float t, float r, float b) {
        p.setColor(0xF02A3038);
        c.drawRoundRect(new RectF(l,t,r,b), 24, 24, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        p.setColor(0xFF7A8695);
        c.drawRoundRect(new RectF(l,t,r,b), 24, 24, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void button(Canvas c, float x, float y, float bw, float bh, String label) {
        p.setColor(0xDD1D252D);
        c.drawRoundRect(new RectF(x,y,x+bw,y+bh), 16, 16, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        p.setColor(0xFF6E7A88);
        c.drawRoundRect(new RectF(x,y,x+bw,y+bh), 16, 16, p);
        p.setStyle(Paint.Style.FILL);
        textCentered(c, label, x + bw/2, y + bh/2 + 7, 18, Color.WHITE);
    }

    private void text(Canvas c, String s, float x, float y, float size, int color) {
        p.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        p.setTextSize(size);
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        c.drawText(s, x, y, p);
    }

    private void textCentered(Canvas c, String s, float x, float y, float size, int color) {
        p.setTextSize(size);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        float tw = p.measureText(s);
        text(c, s, x - tw/2, y, size, color);
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_UP) return true;
        float x = e.getX(), y = e.getY(), w = getWidth(), h = getHeight();

        if (mode == 3) {
            if (y > h - 230 && y < h - 100) {
                mode = 0;
                wanted = 0;
            }
            return true;
        }

        if (mode == 1) {
            if (y > h - 155) {
                mode = 0;
                return true;
            }
            int cols = 3;
            for (int i = 0; i < VEHICLES.length; i++) {
                int col = i % cols, row = i / cols;
                float bx = 85 + col * 240, by = 195 + row * 105;
                if (x >= bx && x <= bx + 215 && y >= by && y <= by + 78) {
                    if (cash >= PRICES[i]) {
                        cash -= PRICES[i];
                        vehicleIndex = i;
                    }
                    mode = 0;
                    return true;
                }
            }
            return true;
        }

        if (mode == 2) {
            if (y > h - 160) mode = 0;
            return true;
        }

        if (y >= h - 175 && y <= h - 75) {
            if (x < 340) {
                if (x < 115) vx -= 480;
                else if (x < 225) vy -= 480;
                else vx += 480;
                wanted = Math.max(wanted, 1);
                heatTimer = 5000;
            } else if (x > w - 365 && x < w - 250) {
                inCar = !inCar;
            } else if (x > w - 255 && x < w - 145) {
                mode = 1;
            } else if (x > w - 150) {
                mode = 2;
            }
        } else if (y > h - 90 && y < h) {
            if (x >= 125 && x <= 235) zoom = zoom == 1f ? 1.25f : 1f;
            else if (x >= 235 && x <= 340) {
                wanted = Math.max(wanted, 2);
                heatTimer = 8000;
            }
            else if (x < 125) {
                vy += 480;
            }
        } else if (y < 74 && x < 260) {
            mode = 0;
        }

        // A patrol catches the player if the wanted level lasts long enough.
        if (wanted >= 3 && heatTimer > 0 && Math.abs(vx) + Math.abs(vy) < 70) {
            mode = 3;
            vx = vy = 0;
        }
        return true;
    }

    private static class Entity {
        final float x, y;
        final int type;
        Entity(float x, float y, int type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }
}
