package com.studio.chungame.bountitank;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by chun on 12/26/2017.
 */

public abstract class GameObject {
    public final static int INDESTRUCTABLE = 0x00000001;
    public final static int EXPLOSIVE      = 0x00000002;
    public final static int DEAD           = 0x00000004;
    public final static int MOVABLE        = 0x00000008;

    protected RectF bound = new RectF();
    protected Rect clashMapRegion = new Rect();
    protected int flag;
    protected int type;

    public GameObject()
    {
        type = 0;
        flag = 0;
    }

    public RectF getBound()
    {
        return bound;
    }

    public void setBound(float top, float left, float bottom, float right)
    {
        bound.top = top;
        bound.left = left;
        bound.bottom = bottom;
        bound.right = right;
    }

    public Rect getClashMapRegion()
    {
        return clashMapRegion;
    }

    public void setClashMapRegion(int top, int left, int bottom, int right)
    {
        clashMapRegion.top = top;
        clashMapRegion.left = left;
        clashMapRegion.bottom = bottom;
        clashMapRegion.right = right;
    }

    public boolean withinClashMapRegion(int x, int y)
    {
        return clashMapRegion.left <= x && x <= clashMapRegion.right &&
               clashMapRegion.top <= y && y <=clashMapRegion.bottom;
    }

    public void setFlag(boolean enabled, int f)
    {
        if(enabled) {
            flag |= f;
        } else {
            flag &= ~f;
        }
    }

    public boolean checkFlag(int f)
    {
        return (flag & f) != 0;
    }

    public int getType()
    {
        return type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

    public boolean contains(float x, float y)
    {
        return bound.contains(x, y);
    }

    public boolean intersect(RectF rect)
    {
        return bound.intersect(rect);
    }

    public abstract void draw(Canvas canvas, float refX, float refY);

    public abstract void update(Map map);

    public abstract void onHit(int hitPower);
}
