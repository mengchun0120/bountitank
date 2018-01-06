package com.studio.chungame.bountitank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by chun on 1/1/2018.
 */

public class Bullet extends GameObject {
    public final static int BASIC = 0;
    public final static int TYPE_COUNT = 1;

    public final static int RADIUS_IN_BLOCKS = 1;

    public final static int[] COLORS = {
            Color.rgb(0, 255, 0),
            Color.rgb(255, 0, 0)
    };

    public final static float[][] SPEEDS = {
            {8.0f, 4.0f}
    };

    private static Bitmap[][] bitmaps;
    private static float radiusInPixels;

    private int type;
    private int side;
    private float dx;
    private float dy;
    private int power;

    public static void prepare(int blockInPixels)
    {
        radiusInPixels = RADIUS_IN_BLOCKS * blockInPixels;

        int width = (int)radiusInPixels * 2;
        int height = (int)radiusInPixels * 2;

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);

        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setColor(Color.BLACK);

        Canvas canvas = new Canvas();

        bitmaps = new Bitmap[TYPE_COUNT][Tank.SIDE_COUNT];
        for(int type = 0; type < TYPE_COUNT; ++type) {
            for(int side = 0; side < Tank.SIDE_COUNT; ++side) {
                bitmaps[type][side] = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmaps[type][side]);
                fillPaint.setColor(COLORS[side]);
                canvas.drawCircle(radiusInPixels, radiusInPixels, radiusInPixels, fillPaint);
                canvas.drawCircle(radiusInPixels, radiusInPixels, radiusInPixels, strokePaint);
            }
        }
    }

    public static float getRadiusInPixels()
    {
        return radiusInPixels;
    }

    public Bullet(int type, int side, float directionX, float directionY,
                  int power, float top, float left)
    {
        this.type = type;
        this.side = side;
        this.power = power;

        dx = SPEEDS[type][side] * directionX;
        dy = SPEEDS[type][side] * directionY;

        float bottom = top + bitmaps[type][side].getHeight() - 1.0f;
        float right = left + bitmaps[type][side].getWidth() - 1.0f;
        setBound(top, left, bottom, right);
    }

    @Override
    public void draw(Canvas canvas, float refX, float refY) {
        canvas.drawBitmap(bitmaps[type][side], bound.left-refX,
                        bound.top-refY, null);
    }

    @Override
    public void update(Map map) {
        if(checkFlag(DEAD)) {
            return;
        }

        float newTop = bound.top + dy;
        float newLeft = bound.left + dx;
        float newBottom = bound.bottom + dy;
        float newRight = bound.right + dx;

        if(map.rectOutsideVisibleArea(newTop, newLeft, newBottom, newRight)) {
            setFlag(true, DEAD);
            return;
        }

        bound.set(newLeft, newTop, newRight, newBottom);

        if(map.checkBulletClash(newTop, newLeft, newBottom, newRight, power)) {
            setFlag(true, DEAD);
            map.addExplosion(side, bound.centerX(), bound.centerY());
        }
    }

    @Override
    public void onHit(int hitPower)
    {
    }
}
