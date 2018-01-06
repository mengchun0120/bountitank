package com.studio.chungame.bountitank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Path;
import android.util.Log;
import android.util.TimingLogger;
import android.view.MotionEvent;

/**
 * Created by chun on 12/23/2017.
 */

public class TurretController {
    public final static float DEF_ATTACK_DIRECTION_X = 0.0f;
    public final static float DEF_ATTACK_DIRECTION_Y = -1.0f;

    private final int LARGE_BASE_COLOR = Color.argb(180, 255, 255, 255);
    private final int STROKE_COLOR = Color.argb(255, 0, 0, 0);
    private final int GUNBODY_NORMAL_COLOR = Color.argb(180, 0, 0, 255);
    private final int GUNBODY_FIRING_COLOR = Color.argb(180, 255, 0, 0);
    private final int GUNHEAD_NORMAL_COLOR = Color.argb(180, 255, 69, 0);
    private final int GUNHEAD_FIRING_COLOR = Color.argb(180, 0, 191, 255);
    private float radius;
    private float radiusSquare;
    private float width;
    private float height;
    private PointF origin;
    private PointF center;
    private float baseGap;
    private float gunWidth;
    private PointF[] baseGunBody;
    private PointF[] baseGunHead;
    private PointF[] gunBody;
    private PointF[] gunHead;
    private Path gunBodyPath;
    private Path gunHeadPath;
    private Paint fillPaint;
    private Paint strokePaint;
    private Bitmap baseBitmap;
    private PointF direction;
    private boolean firing;
    private Tank player;

    public TurretController(float radius, float x, float y, Tank player)
    {
        this.radius = radius;
        radiusSquare = radius * radius;
        width = 2.0f * radius;
        height = 2.0f * radius;
        origin = new PointF(x, y);
        center = new PointF(x+radius, y+radius);
        baseGap = radius / 3.0f;
        gunWidth = radius / 4.0f;

        gunBody = new PointF[4];
        gunHead = new PointF[4];
        for(int i = 0; i < 4; ++i) {
            gunBody[i] = new PointF();
            gunHead[i] = new PointF();
        }
        gunBodyPath = new Path();
        gunHeadPath = new Path();

        initBaseGun();
        initPaint();
        genBaseBitmap();

        direction = new PointF(DEF_ATTACK_DIRECTION_X, DEF_ATTACK_DIRECTION_Y);
        calculateGunCoordinates();
        firing = false;

        this.player = player;
        this.player.setAttackDirection(direction);
        this.player.setFiring(firing);
    }

    public PointF getDirection()
    {
        return direction;
    }

    public boolean containPoint(float tx, float ty)
    {
        float dx = tx - center.x;
        float dy = ty - center.y;
        return dx*dx + dy*dy <= radiusSquare;
    }

    private void initBaseGun()
    {
        baseGunBody = new PointF[4];
        baseGunBody[0] = new PointF(0, -gunWidth/2.0f);
        baseGunBody[1] = new PointF(0, gunWidth/2.0f);
        baseGunBody[2] = new PointF(radius - baseGap, gunWidth/2.0f);
        baseGunBody[3] = new PointF(radius - baseGap, -gunWidth/2.0f);

        baseGunHead = new PointF[4];
        baseGunHead[0] = new PointF(radius - baseGap, -gunWidth/2.0f);
        baseGunHead[1] = new PointF(radius - baseGap, gunWidth/2.0f);
        baseGunHead[2] = new PointF(radius, gunWidth/2.0f);
        baseGunHead[3] = new PointF(radius, -gunWidth/2.0f);
    }

    private void initPaint()
    {
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(STROKE_COLOR);
    }

    private void genBaseBitmap()
    {
        baseBitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(baseBitmap);
        fillPaint.setColor(LARGE_BASE_COLOR);
        canvas.drawCircle(radius, radius, radius, fillPaint);
        canvas.drawCircle(radius, radius, radius, strokePaint);

        float smallRadius = radius - baseGap;
        canvas.drawCircle(radius, radius, smallRadius, strokePaint);
    }

    private void calculateGunCoordinates()
    {
        for(int i = 0; i < gunBody.length; ++i) {
            UIUtility.transformPoint(gunBody[i], baseGunBody[i], direction, center);
        }
        for(int i = 0; i < gunHead.length; ++i) {
            UIUtility.transformPoint(gunHead[i], baseGunHead[i], direction, center);
        }

        UIUtility.createPolygonPath(gunBodyPath, gunBody);
        UIUtility.createPolygonPath(gunHeadPath, gunHead);
    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(baseBitmap, origin.x, origin.y, null);

        int gunBodyColor = firing ? GUNBODY_FIRING_COLOR : GUNBODY_NORMAL_COLOR;
        fillPaint.setColor(gunBodyColor);
        canvas.drawPath(gunBodyPath, fillPaint);
        canvas.drawPath(gunBodyPath, strokePaint);

        int gunHeadColor = firing ? GUNHEAD_FIRING_COLOR : GUNHEAD_NORMAL_COLOR;
        fillPaint.setColor(gunHeadColor);
        canvas.drawPath(gunHeadPath, fillPaint);
        canvas.drawPath(gunHeadPath, strokePaint);
    }

    public void onPressed(float x, float y)
    {
        if(player.checkFlag(GameObject.DEAD)) {
            return;
        }

        float dx = x - center.x;
        float dy = y - center.y;
        float d = (float)Math.sqrt(dx*dx + dy*dy);

        if(d < 1.0e-7f) return;

        direction.x = dx/d;
        direction.y = dy/d;
        calculateGunCoordinates();
        firing = true;

        player.setFiring(firing);
        player.setAttackDirection(direction);
    }

    public void onReleased()
    {
        if(player.checkFlag(GameObject.DEAD)) {
            return;
        }

        firing = false;
        player.setFiring(firing);
    }
}
