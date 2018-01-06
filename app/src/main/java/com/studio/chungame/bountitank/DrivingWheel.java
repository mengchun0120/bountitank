package com.studio.chungame.bountitank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by chun on 12/20/2017.
 */

public class DrivingWheel {
    public static final int DOWN = 0;
    public static final int LEFT = 1;
    public static final int UP = 2;
    public static final int RIGHT = 3;
    public static final int DIRECTION_COUNT = 4;

    private float radius;
    private float radiusSquare;
    private float width;
    private float height;
    private float x;
    private float y;
    private float centerX;
    private float centerY;
    public Bitmap[] normalWheels;
    public Bitmap[] pressedWheels;
    private int direction;
    private Map map;

    public DrivingWheel(float radius, float x, float y, Map map) {
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.map = map;

        radiusSquare = radius * radius;
        width = radius * 2.0f;
        height = radius * 2.0f;
        centerX = x + radius;
        centerY = y + radius;

        genBitmaps();

        direction = -1;
    }

    public boolean containPoint(float tx, float ty)
    {
        float dx = tx - centerX;
        float dy = ty - centerY;
        return dx*dx + dy*dy <= radiusSquare;
    }

    public void draw(Canvas canvas) {
        for(int i = 0; i < DIRECTION_COUNT; ++i) {
            Bitmap bmp = (i == direction) ? pressedWheels[i] : normalWheels[i];
            canvas.drawBitmap(bmp, x, y, null);
        }
    }

    public void onPressed(float tx, float ty) {
        float dx = tx - centerX;
        float dy = ty - centerY;
        calculateDirection(dx, dy);
        setPlayerMobility();
    }

    public void onReleased() {
        direction = -1;
        setPlayerMobility();
    }

    private void calculateDirection(float dx, float dy)
    {
        if(dx >= 0.0f && Math.abs(dy) <= dx) {
            direction = RIGHT;
        } else if(dy >= 0.0f && Math.abs(dx) <= dy) {
            direction = DOWN;
        } else if(dx <= 0.0f && Math.abs(dy) <= -dx) {
            direction = LEFT;
        } else if(dy <= 0.0f && Math.abs(dx) <= -dy) {
            direction = UP;
        }
    }

    private void setPlayerMobility()
    {
        if(map.getPlayer() != null && !map.getPlayer().checkFlag(GameObject.DEAD)) {
            if(direction != -1) {
                map.getPlayer().setFlag(true, GameObject.MOVABLE);
                map.getPlayer().setMoveDirection(direction);
            } else {
                map.getPlayer().setFlag(false, GameObject.MOVABLE);
            }
        }
    }

    private void genBitmaps()
    {
        final int ALPHA = 150;
        final int NORMAL_WHEEL_COLOR = Color.argb(ALPHA, 255, 255, 255);
        final int PRESSED_WHEEL_COLOR = Color.argb(ALPHA, 0, 255, 0);
        final int NORMAL_ARROW_COLOR = Color.argb(ALPHA, 255, 168, 0);
        final int PRESSED_ARROW_COLOR = Color.argb(ALPHA, 255, 215, 0);
        final int STROKE_COLOR = Color.argb(255, 0, 0, 0);

        normalWheels = new Bitmap[DrivingWheel.DIRECTION_COUNT];
        pressedWheels = new Bitmap[DrivingWheel.DIRECTION_COUNT];
        for(int i = 0; i < DrivingWheel.DIRECTION_COUNT; ++i) {
            normalWheels[i] = Bitmap.createBitmap((int)width, (int)height,
                    Bitmap.Config.ARGB_8888);
            pressedWheels[i] = Bitmap.createBitmap((int)width, (int)height,
                    Bitmap.Config.ARGB_8888);
        }

        genWheelBitmaps(normalWheels, NORMAL_WHEEL_COLOR, NORMAL_ARROW_COLOR, STROKE_COLOR);
        genWheelBitmaps(pressedWheels, PRESSED_WHEEL_COLOR, PRESSED_ARROW_COLOR, STROKE_COLOR);
    }

    private void genWheelBitmaps(Bitmap[] bitmaps, int wheelColor, int arrowColor, int strokeColor)
    {
        float sqrt2 = (float)Math.sqrt(2.0);
        float[] arrowX = {
                (1.0f - sqrt2/4.0f) * radius,
                radius,
                (1.0f + sqrt2/4.0f) * radius
        };
        float[] arrowY = {
                (1.0f + sqrt2/2.0f) * radius,
                (1.5f + sqrt2/4.0f) * radius,
                (1.0f + sqrt2/2.0f) * radius
        };

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);

        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(strokeColor);
        strokePaint.setStrokeWidth(1.0f);
        strokePaint.setAntiAlias(true);

        RectF rectF = new RectF(0.0f, 0.0f, width, height);
        float startAngle = 45.0f;

        for(int i = 0; i < bitmaps.length; ++i) {
            Canvas canvas = new Canvas(bitmaps[i]);

            // Draw wheel
            fillPaint.setColor(wheelColor);
            canvas.drawArc(rectF, startAngle, 90.0f, true, fillPaint);
            canvas.drawArc(rectF, startAngle, 90.0f, true, strokePaint);

            Path path = new Path();
            path.moveTo(arrowX[0], arrowY[0]);
            for(int j = 1; j < arrowX.length; ++j) {
                path.lineTo(arrowX[j], arrowY[j]);
            }
            path.lineTo(arrowX[0], arrowY[0]);

            // Draw arrow
            fillPaint.setColor(arrowColor);
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);

            for(int j = 0; j < arrowX.length; ++j) {
                // Rotate arrow
                float tmp = arrowY[j];
                arrowY[j] = arrowX[j];
                arrowX[j] = height - tmp;
            }

            startAngle += 90.0f;
        }
    }
}
