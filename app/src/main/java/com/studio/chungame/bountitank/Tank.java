package com.studio.chungame.bountitank;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ListIterator;

/**
 * Created by chun on 12/30/2017.
 */

public class Tank extends GameObject {
    public final static int DEER = 0;
    public final static int TYPE_COUNT = 1;

    public final static int PLAYER = 0;
    public final static int ENEMY = 1;
    public final static int SIDE_COUNT = 2;

    public final static int TANK_BREATH_IN_BLOCKS = 9;

    private final static int STROKE_COLOR = Color.rgb(0,0,0);
    private final static int[] BODY_COLORS = {
            Color.rgb(127,255,0), //green
            Color.rgb(255,160,122) //red
    };

    private final static int[] TURRET_COLORS = {
            Color.rgb(50,205,50),
            Color.rgb(255,69,0)
    };

    private final static int[] WHEEL_COLORS = {
            Color.rgb(107,142,35),
            Color.rgb(255,0,0)
    };

    private final static int[] GUN_COLORS = {
            Color.rgb(0,255,255),
            Color.rgb(205,92,92)
    };

    private static Paint[] gunFillPaints;
    private static Paint strokePaint;
    private static int tankBreathInPixels;
    private static float turretX;
    private static float turretY;
    private static Bitmap[][][] baseBitmaps;
    private static Bitmap[] turretBitmaps;
    private static PointF[] baseGun;
    private static PointF baseFirePoint;

    private int type;
    private int side;
    private int moveDirection;
    private float moveSpeed;
    private PointF attackDirection;
    private int attackSpeed;
    private int attackCountDown;
    private PointF[] gun;
    private Path gunPath;
    private PointF firePoint;
    private boolean firing;

    public static int getTankBreathInPixels()
    {
        return tankBreathInPixels;
    }

    public static void prepare(int unit)
    {
        if(baseBitmaps != null) return;

        tankBreathInPixels = unit * TANK_BREATH_IN_BLOCKS;

        TankBitmapGenerator bmpGen = new TankBitmapGenerator(unit);

        baseBitmaps = bmpGen.generateBaseBitmaps();
        turretBitmaps = bmpGen.generateTurretBitmaps();
        turretX = ((float)tankBreathInPixels - turretBitmaps[0].getWidth()) / 2.0f;
        turretY = ((float)tankBreathInPixels - turretBitmaps[0].getHeight()) / 2.0f;

        baseGun = new PointF[] {
                new PointF(2.0f*unit, -unit),
                new PointF(2.0f*unit, unit),
                new PointF(4.5f*unit, unit),
                new PointF(4.5f*unit, -unit)
        };

        baseFirePoint = new PointF(4.5f*unit+1.0f, 0.0f);

        gunFillPaints = new Paint[2];
        for(int side = 0; side < SIDE_COUNT; ++side) {
            gunFillPaints[side] = new Paint();
            gunFillPaints[side].setStyle(Paint.Style.FILL);
            gunFillPaints[side].setColor(GUN_COLORS[side]);
        }

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(1.0f);
    }

    public Tank(int type, int side, int moveDirection, float moveSpeed, float attackDirectionX,
                float attackDirectionY, int attackSpeed, float top, float left)
    {
        if(type < 0 || type >= TYPE_COUNT) {
            throw new RuntimeException("Invalid type");
        }

        if(side < 0 || side >= SIDE_COUNT) {
            throw new RuntimeException("Invalid side");
        }

        this.type = type;
        this.side = side;
        this.moveDirection = moveDirection;
        this.moveSpeed = moveSpeed;

        setBound(top, left, top + tankBreathInPixels - 1.0f,
                left + tankBreathInPixels - 1.0f);

        this.attackDirection = new PointF(attackDirectionX, attackDirectionY);
        this.attackSpeed = attackSpeed;
        firePoint = new PointF();

        gun = new PointF[4];
        for(int i = 0; i < gun.length; ++i) {
            gun[i] = new PointF();
        }
        gunPath = new Path();

        updateGun();
    }

    public void setMoveDirection(int moveDirection)
    {
        this.moveDirection = moveDirection;
    }

    @Override
    public void draw(Canvas canvas, float refX, float refY) {
        float left = bound.left - refX;
        float top = bound.top - refY;

        canvas.drawBitmap(baseBitmaps[side][type][moveDirection], left, top, null);

        UIUtility.createPolygonPathRef(gunPath, gun, refX, refY);
        canvas.drawPath(gunPath, gunFillPaints[side]);
        canvas.drawPath(gunPath, strokePaint);

        canvas.drawBitmap(turretBitmaps[side], left + turretX, top + turretY, null);
    }

    @Override
    public void update(Map map)
    {
        if(checkFlag(DEAD)) {
            return;
        }

        if(checkFlag(MOVABLE)) {
            move(map);
        }

        updateGun();

        if(firing) {
            --attackCountDown;
            if(attackCountDown == 0) {
                Bullet bullet = new Bullet(Bullet.BASIC, side, attackDirection.x,
                                        attackDirection.y, 100,
                                        firePoint.y-Bullet.getRadiusInPixels(),
                                        firePoint.x-Bullet.getRadiusInPixels());
                map.addBullet(bullet);

                attackCountDown = attackSpeed;
            }
        }
    }

    public void move(Map map)
    {
        switch(moveDirection) {
            case DrivingWheel.UP: {
                float newTop = bound.top - moveSpeed;
                if(newTop < 0.0f) {
                    newTop = 0.0f;
                }

                GameObject clashObj = map.checkTankClash(newTop, bound.left, bound.top,
                                                         bound.right, DrivingWheel.UP);
                if(clashObj != null) {
                    newTop = clashObj.getBound().bottom + 1.0f;
                }

                bound.bottom += newTop - bound.top;
                bound.top = newTop;
                break;
            }
            case DrivingWheel.DOWN: {
                float newBottom = bound.bottom + moveSpeed;
                if(newBottom > map.getHeightInPixels() - 1.0f) {
                    newBottom = map.getHeightInPixels() - 1.0f;
                }

                GameObject clashObj = map.checkTankClash(bound.bottom, bound.left, newBottom,
                                                         bound.right, DrivingWheel.DOWN);

                if(clashObj != null) {
                    newBottom = clashObj.getBound().top - 1.0f;
                }

                bound.top += newBottom - bound.bottom;
                bound.bottom = newBottom;
                break;
            }
            case DrivingWheel.LEFT: {
                float newLeft = bound.left - moveSpeed;
                if(newLeft < 0.0f) {
                    newLeft = 0.0f;
                }

                GameObject clashObj = map.checkTankClash(bound.top, newLeft, bound.bottom,
                                                         bound.left, DrivingWheel.LEFT);

                if(clashObj != null) {
                    newLeft = clashObj.getBound().right + 1.0f;
                }

                bound.right += newLeft - bound.left;
                bound.left = newLeft;
                break;
            }
            case DrivingWheel.RIGHT: {
                float newRight = bound.right + moveSpeed;
                if(newRight > map.getWidthInPixels() - 1.0f) {
                    newRight = map.getWidthInPixels() - 1.0f;
                }

                GameObject clashObj = map.checkTankClash(bound.top, bound.right, bound.bottom,
                                                         newRight, DrivingWheel.RIGHT);

                if(clashObj != null) {
                    newRight = clashObj.getBound().left - 1.0f;
                }

                bound.left += newRight - bound.right;
                bound.right = newRight;
                break;
            }
        }
    }

    public void setAttackDirection(PointF direction)
    {
        attackDirection.set(direction);
    }

    public void setFiring(boolean firing)
    {
        if(firing && !this.firing) {
            attackCountDown = attackSpeed;
        }
        this.firing = firing;
    }

    public void updateGun()
    {
        float centerX = bound.centerX();
        float centerY = bound.centerY();

        for(int i = 0; i < gun.length; ++i) {
            UIUtility.transformPoint(gun[i], baseGun[i], attackDirection.x,
                                     attackDirection.y, centerX, centerY);
        }

        UIUtility.transformPoint(firePoint, baseFirePoint, attackDirection.x,
                                 attackDirection.y, centerX, centerY);
    }

    @Override
    public void onHit(int hitPower)
    {
    }

    private static class TankBitmapGenerator {
        private float width;
        private float height;
        private PointF center;
        private float turretRadius;
        private PointF[] base;
        private PointF[] leftWheelFrame;
        private PointF[] rightWheelFrame;
        private PointF[] leftFrontLight;
        private PointF[] rightFrontLight;
        private PointF[] rearLight;
        private Paint fillPaint;
        private Paint strokePaint;
        private Path path;
        
        public TankBitmapGenerator(float u)
        {
            width = 9.0f * u;
            height = 9.0f * u;
            center = new PointF(width/2.0f, height/2.0f);

            turretRadius = 2.5f * u;

            base = new PointF[]{
                    new PointF(u, 0.0f),
                    new PointF(u, 9.0f*u),
                    new PointF(8.0f*u, 9.0f*u),
                    new PointF(8.0f*u, 0.0f)
            };

            leftWheelFrame = new PointF[]{
                    new PointF(0.0f, 0.0f),
                    new PointF(0.0f, 9.0f*u),
                    new PointF(u, 9.0f*u),
                    new PointF(u, 0.0f)
            };

            rightWheelFrame = new PointF[]{
                    new PointF(8.0f*u, 0.0f),
                    new PointF(8.0f*u, 9.0f*u),
                    new PointF(9.0f*u, 9.0f*u),
                    new PointF(9.0f*u, 0.0f)
            };

            leftFrontLight = new PointF[] {
                    new PointF(2.0f*u, 7.0f*u),
                    new PointF(2.0f*u, 8.0f*u),
                    new PointF(3.0f*u, 8.0f*u)
            };

            rightFrontLight = new PointF[] {
                    new PointF(6.0f*u, 8.0f*u),
                    new PointF(7.0f*u, 8.0f*u),
                    new PointF(7.0f*u, 7.0f*u)
            };

            rearLight = new PointF[] {
                    new PointF(3.0f*u, 0.5f*u),
                    new PointF(3.0f*u, u),
                    new PointF(6.0f*u, u),
                    new PointF(6.0f*u, 0.5f*u)
            };

            fillPaint = new Paint();
            fillPaint.setStyle(Paint.Style.FILL);

            strokePaint = new Paint();
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setAntiAlias(true);
            strokePaint.setStrokeWidth(1.0f);
            strokePaint.setColor(STROKE_COLOR);

            path = new Path();
        }

        Bitmap[][][] generateBaseBitmaps()
        {
            Bitmap[][][] baseBitmaps =
                    new Bitmap[SIDE_COUNT][TYPE_COUNT][DrivingWheel.DIRECTION_COUNT];

            for(int side = 0; side < SIDE_COUNT; ++side) {
                for(int type = 0; type < TYPE_COUNT; ++type) {
                    for(int direct = 0; direct < DrivingWheel.DIRECTION_COUNT; ++direct) {
                        baseBitmaps[side][type][direct] =
                                Bitmap.createBitmap((int)width, (int)height,
                                        Bitmap.Config.ARGB_8888);

                        Canvas canvas = new Canvas(baseBitmaps[side][type][direct]);
                        drawBase(canvas, side);
                        rotate90degree();
                    }
                }
            }

            return baseBitmaps;
        }

        Bitmap[] generateTurretBitmaps()
        {
            Bitmap[] turretBitmaps = new Bitmap[SIDE_COUNT];

            for(int side = 0; side < SIDE_COUNT; ++side) {
                turretBitmaps[side] = Bitmap.createBitmap((int)(2*turretRadius),
                                        (int)(2*turretRadius), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(turretBitmaps[side]);
                drawTurret(canvas, side);
            }

            return turretBitmaps;
        }

        void rotate90degree()
        {
            PointF ref = new PointF(height, 0.0f);
            PointF direct = new PointF(0.0f, 1.0f);

            int i, j;
            for(i = 0; i < base.length; ++i) {
                UIUtility.transformPoint(base[i], direct, ref);
            }

            for(i = 0; i < leftWheelFrame.length; ++i) {
                UIUtility.transformPoint(leftWheelFrame[i], direct, ref);
            }

            for(i = 0; i < rightWheelFrame.length; ++i) {
                UIUtility.transformPoint(rightWheelFrame[i], direct, ref);
            }

            for(i = 0; i < rightFrontLight.length; ++i) {
                UIUtility.transformPoint(rightFrontLight[i], direct, ref);
            }

            for(i = 0; i < leftFrontLight.length; ++i) {
                UIUtility.transformPoint(leftFrontLight[i], direct, ref);
            }

            for(i = 0; i < rearLight.length; ++i) {
                UIUtility.transformPoint(rearLight[i], direct, ref);
            }

            float tmp;
            tmp = height;
            width = height;
            height = width;

            center.x = width / 2.0f;
            center.y = height / 2.0f;
        }

        void drawBase(Canvas canvas, int side)
        {
            UIUtility.createPolygonPath(path, base);
            fillPaint.setColor(BODY_COLORS[side]);
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);
            
            UIUtility.createPolygonPath(path, leftWheelFrame);
            fillPaint.setColor(WHEEL_COLORS[side]);
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);

            UIUtility.createPolygonPath(path, rightWheelFrame);
            fillPaint.setColor(WHEEL_COLORS[side]);
            canvas.drawPath(path, fillPaint);
            canvas.drawPath(path, strokePaint);

            UIUtility.createPolygonPath(path, leftFrontLight);
            canvas.drawPath(path, strokePaint);

            UIUtility.createPolygonPath(path, rightFrontLight);
            canvas.drawPath(path, strokePaint);

            UIUtility.createPolygonPath(path, rearLight);
            canvas.drawPath(path, strokePaint);
        }

        void drawTurret(Canvas canvas, int side)
        {
            fillPaint.setColor(TURRET_COLORS[side]);
            canvas.drawCircle(turretRadius, turretRadius, turretRadius, fillPaint);
            canvas.drawCircle(turretRadius, turretRadius, turretRadius, strokePaint);
        }
    }
}
