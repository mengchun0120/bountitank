package com.studio.chungame.bountitank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by chun on 12/28/2017.
 */

public class Tile extends GameObject {
    public static final int NO_TILE = -1;
    public static final int ROCK = 0;
    public static final int WOOD = 1;
    public static final int METAL = 2;
    public static final int ICE = 3;
    public static final int TYPE_COUNT = 4;

    public static final int TILE_BREATH_IN_BLOCKS = 5;

    private static final int[] HITPOINTS = {
        4, 2, 0, 1
    };

    private static final int[] FLAGS = {
        0, 0, INDESTRUCTABLE, 0
    };

    private static Bitmap[] bitmaps = null;
    private static int tileBreathInPixels;

    private int hitPoints;

    public static int getTileBreathInPixels()
    {
        return tileBreathInPixels;
    }

    public static void prepare(int blockInPixels)
    {
        if(bitmaps != null) return;

        tileBreathInPixels = TILE_BREATH_IN_BLOCKS * blockInPixels;

        int[] FILL_COLOR = {
            Color.rgb(105,105,105),
            Color.rgb(222,184,135),
            Color.rgb(70,130,180),
            Color.rgb(240,248,255)
        };

        int[] STROKE_COLOR = {
            Color.rgb(47,79,79),
            Color.rgb(128,0,0),
            Color.rgb(0,0,128),
            Color.rgb(0,191,255)
        };

        bitmaps = new Bitmap[TYPE_COUNT];
        RectF rect = new RectF(0.0f, 0.0f,
                               tileBreathInPixels-1.0f, tileBreathInPixels-1.0f);

        Paint fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);

        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(1.0f);

        for(int i = 0; i < TYPE_COUNT; ++i) {
            bitmaps[i] = Bitmap.createBitmap(tileBreathInPixels, tileBreathInPixels,
                                        Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmaps[i]);

            fillPaint.setColor(FILL_COLOR[i]);
            canvas.drawRect(rect, fillPaint);

            strokePaint.setColor(STROKE_COLOR[i]);
            canvas.drawRect(rect, strokePaint);
        }
    }

    public Tile(int type, float top, float left)
    {
        if(type < 0 || type >= TYPE_COUNT) {
            throw new RuntimeException("Invalid type");
        }

        this.type = type;
        this.flag = FLAGS[type];
        this.hitPoints = HITPOINTS[type];
        setBound(top, left, top + tileBreathInPixels - 1.0f,
                left + tileBreathInPixels - 1.0f);
    }

    @Override
    public void draw(Canvas canvas, float refX, float refY)
    {
        if(!checkFlag(DEAD)) {
            canvas.drawBitmap(bitmaps[type], bound.left-refX, bound.top-refY, null);
        }
    }

    @Override
    public void update(Map map)
    {
    }

    @Override
    public void onHit(int hitPower)
    {
        if(checkFlag(INDESTRUCTABLE)) {
            return;
        }

        if(hitPower > 0) {
            hitPoints--;
        }

        if(hitPoints <= 0) {
            setFlag(true, DEAD);
        }
    }
}
