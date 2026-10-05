package com.zygy.roadlegends;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private static final int MODE_GAME = 0;
    private static final int MODE_GARAGE = 1;
    private static final int MODE_MENU = 2;
    private static final int MODE_JAIL = 3;

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random(42);
    private final List<WorldVehicle> vehicles = new ArrayList<>();
    private final List<Police> police = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<Citizen> citizens = new ArrayList<>();

    private float playerX = 0f;
    private float playerY = -50f;
    private float inputX = 0f;
    private float inputY = 0f;
    private float cameraZoom = 1.0f;
    private float wantedPulse = 0f;

    private boolean inVehicle = true;
    private int ownedVehicle = 0;
    private int cash = 50000;
    private int wanted = 0;
    private long wantedUntil = 0L;
    private int mode = MODE_GAME;
    private long lastFrame = System.currentTimeMillis();
    private long lastShot = 0L;

    private static final VehicleSpec[] VEHICLES = {
            new VehicleSpec("Urban GT", "מכונית ספורט", 0, Color.rgb(220, 60, 55), 0),
            new VehicleSpec("Roadster X", "מכונית מרוץ", 22000, Color.rgb(45, 125, 230), 0),
            new VehicleSpec("Rally 4x4", "ג'יפ שטח", 32000, Color.rgb(210, 165, 55), 1),
            new VehicleSpec("Heavy Truck", "משאית", 42000, Color.rgb(175, 105, 60), 2),
            new VehicleSpec("Trail Moto", "אופנוע שטח", 12000, Color.rgb(70, 200, 120), 1),
            new VehicleSpec("Armored SUV", "רכב ממוגן", 68000, Color.rgb(75, 85, 78), 3),
            new VehicleSpec("Rescue Van", "רכב חילוץ", 46000, Color.rgb(225, 225, 225), 3),
            new VehicleSpec("Speed Boat", "סירת מרוץ", 28000, Color.rgb(70, 170, 220), 4),
            new VehicleSpec("Sea Runner", "אופנוע ים", 21000, Color.rgb(40, 210, 210), 4),
            new VehicleSpec("Heli Scout", "מסוק", 125000, Color.rgb(115, 145, 95), 5),
            new VehicleSpec("Airliner", "מטוס נוסעים", 350000, Color.rgb(235, 235, 235), 5),
            new VehicleSpec("Offroad Quad", "טרקטורון", 15000, Color.rgb(235, 120, 45), 1)
    };

    public GameView(Context context) {
        super(context);
        setFocusable(true);
        buildWorld();
    }

    private void buildWorld() {
        vehicles.clear();
        police.clear();

        // City traffic.
        addVehicle("Sedan A", 0, 330, Color.rgb(200, 70, 70), 0, false);
        addVehicle("Sedan B", -620, 330, Color.rgb(70, 150, 230), 0, false);
        addVehicle("Coupe", 640, 330, Color.rgb(235, 180, 60), 0, false);
        addVehicle("Rally", -360, -510, Color.rgb(220, 160, 50), 2, false);
        addVehicle("Truck", 780, -510, Color.rgb(175, 105, 60), 3, false);
        addVehicle("Bike", -120, 720, Color.rgb(60, 210, 120), 4, false);
        addVehicle("Quad", 470, 850, Color.rgb(235, 120, 45), 11, false);
        addVehicle("Armored", 1040, -500, Color.rgb(70, 80, 72), 5, false);
        addVehicle("Rescue", -1050, -470, Color.WHITE, 6, false);

        // Harbor traffic.
        for (int i = 0; i < 6; i++) {
            addVehicle("Boat " + i, -1100 + i * 300, 700, i % 2 == 0 ? Color.rgb(235,235,235) : Color.rgb(80,170,220), 7, false);
        }
        addVehicle("Jet", 850, 760, Color.rgb(45, 210, 215), 8, false);

        // Airport.
        addVehicle("Heli", -900, -900, Color.rgb(110, 145, 90), 9, false);
        addVehicle("Plane", -500, -900, Color.rgb(238, 238, 238), 10, false);

        for (int i = 0; i < 12; i++) {
            float x = -1200 + random.nextInt(2400);
            float y = -1050 + random.nextInt(2100);
            if (Math.abs(x) < 260 && Math.abs(y) < 260) {
                x += 500;
            }
            int type = random.nextInt(6);
            addVehicle("Traffic " + i, x, y, VEHICLES[type].color, type, true);
        }

        // A few patrol cars start far away; they become active when wanted rises.
        for (int i = 0; i < 5; i++) {
            Police unit = new Police(-1500 + i * 620, -1200 + i * 180);
            police.add(unit);
        }
        for (int i = 0; i < 16; i++) {
            float x = -1700 + random.nextInt(3300);
            float y = -1000 + random.nextInt(1950);
            citizens.add(new Citizen(x, y));
        }
    }

    private void addVehicle(String name, float x, float y, int color, int type, boolean traffic) {
        vehicles.add(new WorldVehicle(name, x, y, color, type, traffic));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.currentTimeMillis();
        float dt = Math.min(0.035f, Math.max(0.001f, (now - lastFrame) / 1000f));
        lastFrame = now;

        if (mode == MODE_GAME) {
            updateWorld(dt);
        }

        drawWorld(canvas);
        drawPlayer(canvas);
        drawHud(canvas);

        if (mode == MODE_GARAGE) drawGarage(canvas);
        else if (mode == MODE_MENU) drawMenu(canvas);
        else if (mode == MODE_JAIL) drawJail(canvas);

        postInvalidateDelayed(16);
    }

    private void updateWorld(float dt) {
        float speed = inVehicle ? 420f : 235f;
        playerX += inputX * speed * dt;
        playerY += inputY * speed * dt;
        inputX *= 0.82f;
        inputY *= 0.82f;

        wantedPulse += dt;
        if (wanted > 0) {
            if (System.currentTimeMillis() > wantedUntil) {
                wanted = Math.max(0, wanted - 1);
                wantedUntil = System.currentTimeMillis() + 4000L;
            }
        }

        // Light traffic movement keeps the city alive.
        for (WorldVehicle v : vehicles) {
            if (!v.traffic) continue;
            v.x += v.dirX * 28f * dt;
            v.y += v.dirY * 28f * dt;
            if (v.x < -1900) v.x = 1900;
            if (v.x > 1900) v.x = -1900;
            if (v.y < -1200) v.y = 1200;
            if (v.y > 1200) v.y = -1200;
        }

        // Wanted level activates nearby patrols.
        updateShots(dt);

        for (Citizen person : citizens) {
            person.x += person.dx * 22f * dt;
            person.y += person.dy * 22f * dt;
            if (person.x < -1900 || person.x > 1900) person.dx *= -1f;
            if (person.y < -1100 || person.y > 1100) person.dy *= -1f;
        }

        for (Police unit : police) {
            if (wanted >= 2) {
                float dx = playerX - unit.x;
                float dy = playerY - unit.y;
                float dist = Math.max(1f, (float)Math.sqrt(dx * dx + dy * dy));
                float chase = (wanted >= 4 ? 185f : 135f) * dt;
                unit.x += dx / dist * chase;
                unit.y += dy / dist * chase;
                if (dist < 70 && wanted >= 3) {
                    mode = MODE_JAIL;
                    inputX = inputY = 0f;
                }
            } else {
                unit.x += unit.dirX * 18f * dt;
                unit.y += unit.dirY * 18f * dt;
            }
        }

        playerX = clamp(playerX, -1950f, 1950f);
        playerY = clamp(playerY, -1150f, 1150f);
    }

    private void drawWorld(Canvas c) {
        float w = getWidth();
        float h = getHeight();

        c.drawColor(Color.rgb(23, 31, 29));
        c.save();
        c.translate(w / 2f - playerX * cameraZoom, h / 2f - playerY * cameraZoom);
        c.scale(cameraZoom, cameraZoom);

        // Grass / city land.
        fill(c, Color.rgb(68, 92, 66));
        c.drawRect(-2100, -1250, 2100, 1250, p);

        // Coast and sea.
        fill(c, Color.rgb(33, 104, 138));
        c.drawRect(-2100, 520, 2100, 1250, p);
        drawWaterLines(c);

        // City district.
        fill(c, Color.rgb(92, 91, 83));
        c.drawRect(-1850, -850, 600, 500, p);

        // Roads with center markings.
        drawRoad(c, -2000, -140, 4000, 260, true);
        drawRoad(c, -130, -1200, 260, 1700, false);
        drawRoad(c, -1600, -620, 2200, 160, true);
        drawRoad(c, 500, 250, 1650, 170, true);

        // City buildings.
        for (int x = -1750; x <= 350; x += 330) {
            for (int y = -760; y <= 200; y += 270) {
                if (Math.abs(x) < 300 || Math.abs(y) < 160) continue;
                drawBuilding(c, x, y, 230, 175);
            }
        }

        // Off-road area.
        fill(c, Color.rgb(97, 116, 67));
        c.drawRect(620, -150, 2000, 510, p);
        for (int i = 0; i < 18; i++) {
            float x = 700 + (i * 97) % 1200;
            float y = -70 + (i * 73) % 470;
            fill(c, Color.rgb(72, 88, 57));
            c.drawCircle(x, y, 24 + (i % 3) * 9, p);
        }

        // Harbor dock.
        fill(c, Color.rgb(105, 88, 65));
        c.drawRect(-1500, 420, 400, 620, p);
        fill(c, Color.rgb(148, 124, 88));
        for (int i = 0; i < 8; i++) {
            c.drawRect(-1380 + i * 215, 435, -1335 + i * 215, 610, p);
        }
        label(c, "נמל", -1260, 505, 28, Color.WHITE);

        // Airport runways and terminal.
        fill(c, Color.rgb(55, 60, 64));
        c.drawRect(-1350, -1090, 250, -770, p);
        c.drawRect(-1120, -1200, -930, -650, p);
        fill(c, Color.WHITE);
        for (int i = 0; i < 11; i++) {
            c.drawRect(-1290 + i * 125, -943, -1230 + i * 125, -927, p);
        }
        fill(c, Color.rgb(225, 225, 225));
        c.drawRoundRect(new RectF(-1150, -850, -700, -770), 16, 16, p);
        label(c, "שדה תעופה", -1080, -810, 24, Color.DKGRAY);

        // Scenic hill.
        fill(c, Color.rgb(76, 96, 61));
        Path hill = new Path();
        hill.moveTo(1100, -1050);
        hill.lineTo(1600, -1050);
        hill.lineTo(1850, -700);
        hill.lineTo(1100, -700);
        hill.close();
        c.drawPath(hill, p);

        // World vehicles.
        for (WorldVehicle v : vehicles) {
            drawWorldVehicle(c, v);
        }

        // Police.
        for (Police unit : police) {
            drawPolice(c, unit.x, unit.y);
        }
        for (Citizen person : citizens) {
            drawCitizen(c, person.x, person.y);
        }
        for (Shot shot : shots) {
            drawShot(c, shot.x, shot.y);
        }

        c.restore();
    }

    private void drawWaterLines(Canvas c) {
        p.setStrokeWidth(4);
        p.setColor(Color.rgb(55, 135, 165));
        for (int i = -2000; i < 2000; i += 130) {
            c.drawLine(i, 600, i + 70, 600, p);
            c.drawLine(i + 40, 760, i + 115, 760, p);
            c.drawLine(i - 20, 920, i + 50, 920, p);
        }
    }

    private void drawRoad(Canvas c, float x, float y, float width, float height, boolean horizontal) {
        fill(c, Color.rgb(47, 51, 53));
        c.drawRect(x, y, x + width, y + height, p);
        p.setStrokeWidth(7);
        p.setColor(Color.rgb(216, 190, 75));
        if (horizontal) {
            for (float xx = x + 20; xx < x + width; xx += 85) {
                c.drawLine(xx, y + height / 2f, xx + 42, y + height / 2f, p);
            }
        } else {
            for (float yy = y + 20; yy < y + height; yy += 85) {
                c.drawLine(x + width / 2f, yy, x + width / 2f, yy + 42, p);
            }
        }
    }

    private void drawBuilding(Canvas c, float x, float y, float bw, float bh) {
        fill(c, Color.rgb(149, 145, 136));
        c.drawRoundRect(new RectF(x, y, x + bw, y + bh), 15, 15, p);
        fill(c, Color.rgb(67, 77, 86));
        for (int i = 0; i < 4; i++) {
            c.drawRect(x + 25 + i * 50, y + 28, x + 57 + i * 50, y + 62, p);
            c.drawRect(x + 25 + i * 50, y + 90, x + 57 + i * 50, y + 124, p);
        }
        fill(c, Color.rgb(112, 105, 97));
        c.drawRect(x + 70, y + bh - 48, x + 160, y + bh, p);
    }

    private void drawWorldVehicle(Canvas c, WorldVehicle v) {
        switch (v.type) {
            case 3: drawTruck(c, v.x, v.y, v.color); break;
            case 4:
            case 11: drawBike(c, v.x, v.y, v.color); break;
            case 7:
            case 8: drawBoat(c, v.x, v.y, v.color); break;
            case 9: drawHelicopter(c, v.x, v.y, v.color); break;
            case 10: drawPlane(c, v.x, v.y, v.color); break;
            case 5: drawArmored(c, v.x, v.y, v.color); break;
            default: drawCar(c, v.x, v.y, v.color); break;
        }
    }

    private void drawCar(Canvas c, float x, float y, int color) {
        fill(c, color);
        c.drawRoundRect(new RectF(x - 52, y - 30, x + 52, y + 30), 15, 15, p);
        fill(c, Color.rgb(35, 43, 50));
        c.drawRoundRect(new RectF(x - 25, y - 22, x + 28, y + 18), 10, 10, p);
        fill(c, Color.rgb(18, 18, 18));
        c.drawCircle(x - 38, y + 26, 10, p);
        c.drawCircle(x + 38, y + 26, 10, p);
        fill(c, Color.WHITE);
        c.drawCircle(x - 38, y - 26, 5, p);
        c.drawCircle(x + 38, y - 26, 5, p);
    }

    private void drawTruck(Canvas c, float x, float y, int color) {
        fill(c, color);
        c.drawRect(x - 72, y - 34, x + 70, y + 34, p);
        fill(c, Color.rgb(50, 58, 65));
        c.drawRect(x - 96, y - 28, x - 70, y + 31, p);
        fill(c, Color.rgb(18, 18, 18));
        c.drawCircle(x - 55, y + 40, 11, p);
        c.drawCircle(x + 48, y + 40, 11, p);
        c.drawCircle(x + 76, y + 40, 11, p);
    }

    private void drawBike(Canvas c, float x, float y, int color) {
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(8);
        p.setColor(color);
        c.drawCircle(x - 28, y + 18, 16, p);
        c.drawCircle(x + 28, y + 18, 16, p);
        c.drawLine(x - 28, y + 18, x, y - 12, p);
        c.drawLine(x, y - 12, x + 28, y + 18, p);
        p.setStyle(Paint.Style.FILL);
        fill(c, color);
        c.drawCircle(x, y - 20, 8, p);
    }

    private void drawArmored(Canvas c, float x, float y, int color) {
        fill(c, color);
        c.drawRoundRect(new RectF(x - 68, y - 37, x + 68, y + 37), 10, 10, p);
        fill(c, Color.rgb(36, 43, 39));
        c.drawRoundRect(new RectF(x - 25, y - 24, x + 30, y + 18), 8, 8, p);
        fill(c, Color.rgb(18, 18, 18));
        c.drawCircle(x - 45, y + 33, 12, p);
        c.drawCircle(x + 45, y + 33, 12, p);
    }

    private void drawBoat(Canvas c, float x, float y, int color) {
        fill(c, color);
        Path hull = new Path();
        hull.moveTo(x - 65, y);
        hull.lineTo(x + 58, y);
        hull.lineTo(x + 33, y + 36);
        hull.lineTo(x - 42, y + 36);
        hull.close();
        c.drawPath(hull, p);
        fill(c, Color.rgb(40, 55, 65));
        c.drawRoundRect(new RectF(x - 20, y - 32, x + 30, y + 4), 6, 6, p);
    }

    private void drawHelicopter(Canvas c, float x, float y, int color) {
        fill(c, color);
        c.drawRoundRect(new RectF(x - 48, y - 18, x + 50, y + 20), 18, 18, p);
        fill(c, Color.rgb(35, 42, 45));
        c.drawCircle(x + 14, y, 18, p);
        p.setStrokeWidth(5);
        p.setColor(Color.DKGRAY);
        c.drawLine(x - 65, y - 27, x + 66, y - 27, p);
        c.drawLine(x - 38, y + 30, x + 60, y + 30, p);
    }

    private void drawPlane(Canvas c, float x, float y, int color) {
        fill(c, color);
        Path plane = new Path();
        plane.moveTo(x - 88, y);
        plane.lineTo(x - 20, y - 12);
        plane.lineTo(x + 78, y - 42);
        plane.lineTo(x + 88, y - 28);
        plane.lineTo(x + 20, y);
        plane.lineTo(x + 88, y + 28);
        plane.lineTo(x + 78, y + 42);
        plane.lineTo(x - 20, y + 12);
        plane.close();
        c.drawPath(plane, p);
        fill(c, Color.rgb(70, 90, 100));
        c.drawRoundRect(new RectF(x - 10, y - 8, x + 62, y + 8), 7, 7, p);
    }

    private void drawPolice(Canvas c, float x, float y) {
        drawCar(c, x, y, Color.rgb(240, 240, 240));
        fill(c, Color.rgb(40, 75, 205));
        c.drawRect(x - 16, y - 43, x + 2, y - 29, p);
        fill(c, Color.rgb(215, 55, 55));
        c.drawRect(x + 2, y - 43, x + 20, y - 29, p);
    }

    private void drawPlayer(Canvas c) {
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        if (inVehicle) {
            VehicleSpec spec = VEHICLES[ownedVehicle];
            drawWorldVehicleAtScreen(c, cx, cy, spec.type, spec.color);
        } else {
            fill(c, Color.rgb(30, 30, 30));
            c.drawCircle(cx, cy - 22, 14, p);
            fill(c, Color.rgb(80, 105, 145));
            c.drawRoundRect(new RectF(cx - 15, cy - 10, cx + 15, cy + 34), 11, 11, p);
            fill(c, Color.rgb(32, 32, 32));
            c.drawRect(cx - 18, cy + 27, cx - 5, cy + 52, p);
            c.drawRect(cx + 5, cy + 27, cx + 18, cy + 52, p);
        }
    }

    private void drawWorldVehicleAtScreen(Canvas c, float x, float y, int type, int color) {
        switch (type) {
            case 3: drawTruck(c, x, y, color); break;
            case 4:
            case 11: drawBike(c, x, y, color); break;
            case 7:
            case 8: drawBoat(c, x, y, color); break;
            case 9: drawHelicopter(c, x, y, color); break;
            case 10: drawPlane(c, x, y, color); break;
            case 5: drawArmored(c, x, y, color); break;
            default: drawCar(c, x, y, color); break;
        }
    }

    private void drawHud(Canvas c) {
        float w = getWidth();
        float h = getHeight();

        fill(c, 0xD912171C);
        c.drawRect(0, 0, w, 82, p);
        label(c, "ROAD LEGENDS", 26, 35, 25, Color.WHITE);
        label(c, VEHICLES[ownedVehicle].name, 26, 65, 16, Color.LTGRAY);
        label(c, "₪" + cash, w - 145, 38, 21, Color.WHITE);

        if (wanted > 0) {
            String stars = "";
            for (int i = 0; i < wanted; i++) stars += "★";
            label(c, "חיפוש " + stars, w - 185, 66, 17, Color.rgb(255, 214, 80));
        } else {
            label(c, "הכול רגוע", w - 150, 66, 16, Color.LTGRAY);
        }

        // Left control cluster.
        button(c, 28, h - 152, 76, 54, "◀");
        button(c, 112, h - 190, 76, 54, "▲");
        button(c, 112, h - 114, 76, 54, "▼");
        button(c, 196, h - 152, 76, 54, "▶");

        // Action cluster.
        button(c, w - 332, h - 152, 96, 54, inVehicle ? "יציאה" : "כניסה");
        button(c, w - 228, h - 152, 96, 54, "מוסך");
        button(c, w - 124, h - 152, 96, 54, "תפריט");
        button(c, w - 228, h - 90, 96, 50, "זום");
        button(c, w - 124, h - 90, 96, 50, "אירוע");
        button(c, w - 332, h - 90, 96, 50, "אקשן");

        label(c, inVehicle ? "נהיגה" : "ברגל", w / 2f - 30, h - 28, 16, Color.WHITE);
        label(c, "עולם פתוח  •  עיר  •  נמל  •  שטח  •  שדה תעופה", 28, 112, 16, Color.WHITE);
    }

    private void drawGarage(Canvas c) {
        dim(c);
        float w = getWidth();
        float h = getHeight();

        panel(c, 45, 55, w - 45, h - 55);
        label(c, "מוסך", 80, 100, 32, Color.WHITE);
        label(c, "בחר רכב לרכישה או החלפה", 80, 130, 17, Color.LTGRAY);

        for (int i = 0; i < VEHICLES.length; i++) {
            int col = i % 3;
            int row = i / 3;
            float x = 75 + col * 250;
            float y = 155 + row * 78;
            int price = VEHICLES[i].price;
            String cost = price == 0 ? "שלך" : "₪" + price;
            button(c, x, y, 225, 62, VEHICLES[i].name + "  " + cost);
            label(c, VEHICLES[i].subtitle, x, y + 56, 13, Color.LTGRAY);
        }

        button(c, w - 170, h - 110, 110, 55, "סגור");
    }

    private void drawMenu(Canvas c) {
        dim(c);
        float w = getWidth();
        float h = getHeight();

        panel(c, 90, 70, w - 90, h - 70);
        label(c, "הגדרות משחק", w / 2f - 90, 125, 30, Color.WHITE);
        button(c, w / 2f - 160, 175, 320, 62, "ממשק עברי מלא");
        button(c, w / 2f - 160, 250, 320, 62, "שליטה פשוטה");
        button(c, w / 2f - 160, 325, 320, 62, cameraZoom > 1f ? "זום: רחוק" : "זום: רגיל");
        button(c, w / 2f - 160, h - 145, 320, 62, "חזרה למשחק");
    }

    private void drawJail(Canvas c) {
        dim(c);
        float w = getWidth();
        float h = getHeight();

        panel(c, 120, 110, w - 120, h - 110);
        label(c, "נעצרת!", w / 2f - 55, 165, 34, Color.WHITE);
        label(c, "המרדף הסתיים והשוטרים לקחו אותך לתחנה.", w / 2f - 230, 210, 19, Color.LTGRAY);
        label(c, "המשחק ממשיך אחרי השחרור.", w / 2f - 145, 242, 17, Color.LTGRAY);
        button(c, w / 2f - 120, h - 205, 240, 70, "שחרור");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;

        float x = event.getX();
        float y = event.getY();
        float w = getWidth();
        float h = getHeight();

        if (mode == MODE_JAIL) {
            if (y > h - 230) {
                wanted = 0;
                wantedUntil = 0;
                mode = MODE_GAME;
                playerX = 0;
                playerY = -50;
            }
            return true;
        }

        if (mode == MODE_GARAGE) {
            if (y > h - 145) {
                mode = MODE_GAME;
                return true;
            }
            for (int i = 0; i < VEHICLES.length; i++) {
                int col = i % 3;
                int row = i / 3;
                float bx = 75 + col * 250;
                float by = 155 + row * 78;
                if (x >= bx && x <= bx + 225 && y >= by && y <= by + 62) {
                    if (VEHICLES[i].price == 0 || cash >= VEHICLES[i].price) {
                        if (i != ownedVehicle && VEHICLES[i].price > 0) {
                            cash -= VEHICLES[i].price;
                        }
                        ownedVehicle = i;
                    }
                    mode = MODE_GAME;
                    return true;
                }
            }
            return true;
        }

        if (mode == MODE_MENU) {
            if (y > h - 175) {
                mode = MODE_GAME;
            } else if (y >= 315 && y <= 400) {
                cameraZoom = cameraZoom > 1f ? 1f : 1.28f;
            }
            return true;
        }

        // Direction buttons.
        if (y >= h - 215 && y <= h - 72) {
            if (x < 105) inputX = -1f;
            else if (x >= 195 && x < 285) inputX = 1f;
            else if (x >= 100 && x < 195 && y < h - 130) inputY = -1f;
            else if (x >= 100 && x < 195) inputY = 1f;
        }

        // Exit / enter nearest vehicle.
        if (x > w - 345 && x < w - 230 && y > h - 175) {
            if (inVehicle) {
                inVehicle = false;
            } else {
                WorldVehicle nearest = nearestUsableVehicle();
                if (nearest != null) {
                    ownedVehicle = nearest.type >= 0 && nearest.type < VEHICLES.length ? nearest.type : ownedVehicle;
                    inVehicle = true;
                }
            }
        } else if (x > w - 240 && x < w - 125 && y > h - 175) {
            mode = MODE_GARAGE;
        } else if (x > w - 140 && y > h - 175) {
            mode = MODE_MENU;
        } else if (x > w - 240 && x < w - 125 && y > h - 112) {
            cameraZoom = cameraZoom > 1f ? 1f : 1.28f;
        } else if (x > w - 140 && y > h - 112) {
            wanted = Math.min(5, wanted + 1);
            wantedUntil = System.currentTimeMillis() + 4500L;
        } else if (x > w - 350 && x < w - 230 && y > h - 112) {
            fireArcadeShot();
        }

        return true;
    }

    private WorldVehicle nearestUsableVehicle() {
        WorldVehicle best = null;
        float bestDistance = 165f;
        for (WorldVehicle v : vehicles) {
            float dx = v.x - playerX;
            float dy = v.y - playerY;
            float d = (float)Math.sqrt(dx * dx + dy * dy);
            if (d < bestDistance && (v.type >= 0 && v.type < VEHICLES.length)) {
                bestDistance = d;
                best = v;
            }
        }
        return best;
    }

    private void fireArcadeShot() {
        long now = System.currentTimeMillis();
        if (now - lastShot < 280L) return;
        lastShot = now;
        shots.add(new Shot(playerX, playerY - 38f, 0f, -1f));
        wanted = Math.min(5, Math.max(1, wanted + 1));
        wantedUntil = now + 4500L;
        cash += 25;
    }

    private void updateShots(float dt) {
        Iterator<Shot> it = shots.iterator();
        while (it.hasNext()) {
            Shot s = it.next();
            s.x += s.dx * 760f * dt;
            s.y += s.dy * 760f * dt;
            boolean remove = Math.abs(s.x) > 2050 || Math.abs(s.y) > 1300;
            if (!remove) {
                for (Police unit : police) {
                    float d = (float)Math.hypot(unit.x - s.x, unit.y - s.y);
                    if (d < 44f) { unit.x -= s.dx * 18f; unit.y -= s.dy * 18f; cash += 50; remove = true; break; }
                }
            }
            if (!remove) {
                for (Citizen person : citizens) {
                    float d = (float)Math.hypot(person.x - s.x, person.y - s.y);
                    if (d < 34f) { person.dx *= -1f; person.dy *= -1f; cash += 50; remove = true; break; }
                }
            }
            if (remove) it.remove();
        }
    }

    private void drawCitizen(Canvas c, float x, float y) {
        fill(c, Color.rgb(28, 32, 36));
        c.drawCircle(x, y - 18, 10, p);
        fill(c, Color.rgb(63, 116, 175));
        c.drawRoundRect(new RectF(x - 10, y - 6, x + 10, y + 28), 7, 7, p);
        fill(c, Color.rgb(34, 34, 34));
        c.drawRect(x - 12, y + 25, x - 3, y + 47, p);
        c.drawRect(x + 3, y + 25, x + 12, y + 47, p);
    }

    private void drawShot(Canvas c, float x, float y) {
        fill(c, Color.rgb(255, 214, 75));
        c.drawCircle(x, y, 7, p);
        p.setStrokeWidth(4);
        p.setColor(Color.argb(160, 255, 240, 120));
        c.drawLine(x, y + 10, x, y + 28, p);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void fill(Canvas c, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
    }

    private void label(Canvas c, String text, float x, float y, float size, int color) {
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(size);
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawText(text, x, y, p);
    }

    private void button(Canvas c, float x, float y, float width, float height, String text) {
        fill(c, 0xE51A2128);
        c.drawRoundRect(new RectF(x, y, x + width, y + height), 15, 15, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        p.setColor(0xFF738091);
        c.drawRoundRect(new RectF(x, y, x + width, y + height), 15, 15, p);
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.create("sans", Typeface.BOLD));
        p.setTextSize(16);
        p.setColor(Color.WHITE);
        float tw = p.measureText(text);
        c.drawText(text, x + (width - tw) / 2f, y + height / 2f + 6f, p);
    }

    private void panel(Canvas c, float l, float t, float r, float b) {
        fill(c, 0xF02A3038);
        c.drawRoundRect(new RectF(l, t, r, b), 25, 25, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2);
        p.setColor(0xFF7B8796);
        c.drawRoundRect(new RectF(l, t, r, b), 25, 25, p);
        p.setStyle(Paint.Style.FILL);
    }

    private void dim(Canvas c) {
        fill(c, 0xAA000000);
        c.drawRect(0, 0, getWidth(), getHeight(), p);
    }

    private static class VehicleSpec {
        final String name;
        final String subtitle;
        final int price;
        final int color;
        final int type;

        VehicleSpec(String name, String subtitle, int price, int color, int type) {
            this.name = name;
            this.subtitle = subtitle;
            this.price = price;
            this.color = color;
            this.type = type;
        }
    }

    private static class WorldVehicle {
        final String name;
        float x;
        float y;
        final int color;
        final int type;
        final boolean traffic;
        final float dirX;
        final float dirY;

        WorldVehicle(String name, float x, float y, int color, int type, boolean traffic) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.color = color;
            this.type = type;
            this.traffic = traffic;
            this.dirX = traffic ? (x < 0 ? 1f : -1f) : 0f;
            this.dirY = traffic ? (y < 0 ? 1f : -1f) : 0f;
        }
    }

    private static class Shot {
        float x, y, dx, dy;
        Shot(float x, float y, float dx, float dy) { this.x=x; this.y=y; this.dx=dx; this.dy=dy; }
    }

    private static class Citizen {
        float x, y, dx, dy;
        Citizen(float x, float y) { this.x=x; this.y=y; this.dx=(x%2==0?0.7f:-0.6f); this.dy=(y%2==0?0.35f:-0.3f); }
    }

    private static class Police {
        float x;
        float y;
        final float dirX;
        final float dirY;

        Police(float x, float y) {
            this.x = x;
            this.y = y;
            this.dirX = 0.25f;
            this.dirY = 0.18f;
        }
    }
}
