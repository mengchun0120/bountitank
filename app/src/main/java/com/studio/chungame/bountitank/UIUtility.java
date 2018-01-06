package com.studio.chungame.bountitank;

import android.app.Activity;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

/**
 * Created by chun on 12/23/2017.
 */

public class UIUtility {
    public static void enableImmersiveMode(Activity activity)
    {
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    public static void transformPoint(PointF dst, PointF src, PointF direct, PointF ref)
    {
        dst.x = src.x * direct.x - src.y * direct.y + ref.x;
        dst.y = src.x * direct.y + src.y * direct.x + ref.y;
    }

    public static void transformPoint(PointF dst, PointF src, float directX, float directY,
                                      float refX, float refY)
    {
        dst.x = src.x * directX - src.y * directY + refX;
        dst.y = src.x * directY + src.y * directX + refY;
    }

    public static void transformPoint(PointF p, PointF direct, PointF ref)
    {
        float newX = p.x * direct.x - p.y * direct.y + ref.x;
        float newY = p.x * direct.y + p.y * direct.x + ref.y;
        p.x = newX;
        p.y = newY;
    }


    public static void createPolygonPath(Path path, PointF[] points)
    {
        path.rewind();
        path.moveTo(points[0].x, points[0].y);
        for(int i = 1; i < points.length; ++i) {
            path.lineTo(points[i].x, points[i].y);
        }
        path.lineTo(points[0].x, points[0].y);
    }

    public static void createPolygonPathRef(Path path, PointF[] points, float refX, float refY)
    {
        path.rewind();
        path.moveTo(points[0].x - refX, points[0].y - refY);
        for(int i = 1; i < points.length; ++i) {
            path.lineTo(points[i].x - refX, points[i].y - refY);
        }
        path.lineTo(points[0].x - refX, points[0].y - refY);
    }

    public static void moveRectF(RectF rect, int direction, float delta)
    {
        switch(direction) {
            case DrivingWheel.LEFT: {
                rect.left -= delta;
                rect.right -= delta;
                break;
            }
            case DrivingWheel.RIGHT: {
                rect.left += delta;
                rect.right += delta;
                break;
            }
            case DrivingWheel.UP: {
                rect.top -= delta;
                rect.bottom -= delta;
                break;
            }
            case DrivingWheel.DOWN: {
                rect.top += delta;
                rect.bottom += delta;
                break;
            }
            default: {
                throw new RuntimeException("Invalid direction");
            }
        }
    }

    public static void moveRectF(RectF dst, RectF src, int direction, float delta)
    {
        dst.top = src.top;
        dst.left = src.left;
        dst.bottom = src.bottom;
        dst.right = src.right;
        moveRectF(dst, direction, delta);
    }
}
