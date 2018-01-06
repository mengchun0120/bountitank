package com.studio.chungame.bountitank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by chun on 1/4/2018.
 */

public class Explosion extends GameObject {
    private static Bitmap[][] bitmaps;
    private static float width;
    private static float height;

    private int side;
    private int frame;

    public static float getWidth()
    {
        return width;
    }

    public static float getHeight()
    {
        return height;
    }

    public static void prepare(int blockInPixels)
    {
        class Spark {
            PointF pos = new PointF();
            public float speed;
            PointF direction = new PointF();

            public void draw(Canvas canvas, Paint paint)
            {
                canvas.drawCircle(pos.x, pos.y, 1.0f, paint);
            }
        };

        final int[] COLORS = {
                Color.argb(255, 0, 255, 0),
                Color.argb(255, 255, 0, 0)
        };
        final int SPARK_SPAWN_COUNT = 36;
        final float INIT_SPEED = 5.0f;
        final float DECELERATION = 0.5f;
        final int FRAME_COUNT = (int)Math.ceil(INIT_SPEED/DECELERATION) + 1;

        float radius = 2.0f * blockInPixels;
        width = 2.0f * radius;
        height = 2.0f * radius;

        LinkedList<Spark> sparks = new LinkedList<Spark>();
        PointF direction = new PointF(1.0f, 0.0f);
        double angleDelta = Math.PI/18;
        float initSpeed = INIT_SPEED;

        bitmaps = new Bitmap[Tank.SIDE_COUNT][FRAME_COUNT];
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        Canvas canvas = new Canvas();

        for(int frame = 0; frame < FRAME_COUNT; ++frame) {
            if(initSpeed > 1e-7f) {
                double angle = 0.0;
                for (int i = 0; i < SPARK_SPAWN_COUNT; ++i, angle+=angleDelta) {
                    Spark s = new Spark();
                    s.pos.x = radius;
                    s.pos.y = radius;
                    s.speed = initSpeed;
                    UIUtility.transformPoint(s.direction, direction, (float)Math.cos(angle),
                                            (float)Math.sin(angle), 0.0f, 0.0f);
                    sparks.addLast(s);
                }
            }

            for(int i = 0; i < Tank.SIDE_COUNT; ++i) {
                bitmaps[i][frame] = Bitmap.createBitmap((int)width, (int)height,
                                                        Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmaps[i][frame]);
                paint.setColor(COLORS[i]);

                ListIterator<Spark> sparkIt = sparks.listIterator();
                while(sparkIt.hasNext()) {
                    sparkIt.next().draw(canvas, paint);
                }
            }

            ListIterator<Spark> sparkIt = sparks.listIterator();
            while(sparkIt.hasNext()) {
                Spark s = sparkIt.next();
                if (s.speed < 1e-6f) {
                    sparkIt.remove();
                } else {
                    s.pos.x += s.speed * s.direction.x;
                    s.pos.y += s.speed * s.direction.y;
                    s.speed -= DECELERATION;
                }
            }

            initSpeed -= DECELERATION;
        }
    }

    public Explosion(int side, float centerX, float centerY)
    {
        this.side = side;
        frame = 0;
        bound.top = centerY - height/2.0f;
        bound.left = centerX - width/2.0f;
        bound.bottom = bound.top + height - 1.0f;
        bound.right = bound.left + width - 1.0f;
    }

    @Override
    public void draw(Canvas canvas, float refX, float refY)
    {
        if(checkFlag(DEAD)) {
            return;
        }
        canvas.drawBitmap(bitmaps[side][frame], bound.left-refX, bound.top-refY,
                        null);
    }

    @Override
    public void update(Map map)
    {
        if(checkFlag(DEAD)) {
            return;
        }

        frame++;
        if(frame >= bitmaps[side].length) {
            setFlag(true, DEAD);
        }
    }

    @Override
    public void onHit(int hitPower)
    {

    }
}
