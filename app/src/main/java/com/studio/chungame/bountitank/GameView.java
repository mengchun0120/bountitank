package com.studio.chungame.bountitank;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chun on 12/23/2017.
 */

public class GameView extends SurfaceView implements View.OnTouchListener {
    private final int DRIVING_WHEEL_ID = 0;
    private final int TURRET_CONTROLLER_ID = 1;
    private final int CONTROLLER_COUNT = 2;

    private int width;
    private int height;
    private Timer updateTimer;
    private UpdateTask updateTask;
    private DrivingWheel drivingWheel;
    private TurretController turretController;
    private Map map;
    private int[] controllerPointerMap;
//    private Bitmap backBuffer;
//    private Canvas backCanvas;

    public GameView(Context context, int width, int height)
    {
        super(context);

        this.width = width;
        this.height = height;

        initMap();
        initDrivingWheel();
        initTurretController();

        updateTimer = new Timer();
        updateTask = new UpdateTask();
        updateTimer.scheduleAtFixedRate(updateTask, 100, 16);
        //updateTimer.schedule(updateTask, 1200);

        controllerPointerMap = new int[CONTROLLER_COUNT];
        for(int i = 0; i < controllerPointerMap.length; ++i) {
            controllerPointerMap[i] = -1;
        }

        setOnTouchListener(this);
    }

    public void close()
    {
        updateTask.cancel();
        updateTimer.purge();
    }

    private int getControllerId(int pointerId)
    {
        for(int i = 0; i < controllerPointerMap.length; ++i) {
            if(controllerPointerMap[i] == pointerId) {
                return i;
            }
        }
        return -1;
    }

    private void initDrivingWheel()
    {
        float margin = 20.0f;
        float radius = (float)width/12.0f;
        float x = margin;
        float y = (float)height - 2*radius - margin;

        drivingWheel = new DrivingWheel(radius, x, y, map);
    }

    private void initMap()
    {
        map = new Map(Map.SMALL, width, height);
    }

    private void initTurretController()
    {
        float margin = 20.0f;
        float radius = (float)width/12.0f;
        float x = (float)width - 2*radius - margin;
        float y = (float)height - 2*radius - margin;

        turretController = new TurretController(radius, x, y, map.getPlayer());
    }

    private void update()
    {
        map.update();
    }

    private void draw()
    {
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();

        canvas.drawColor(Color.WHITE);
        map.draw(canvas);
        drivingWheel.draw(canvas);
        turretController.draw(canvas);

        holder.unlockCanvasAndPost(canvas);
    }

    private void onPressed(int pointerId, float x, float y)
    {
        int controllerId = -1;
        boolean pressed = true;

        if(drivingWheel.containPoint(x, y)) {
            controllerId = DRIVING_WHEEL_ID;
        } else if(turretController.containPoint(x, y)) {
            controllerId = TURRET_CONTROLLER_ID;
        } else {
            controllerId = getControllerId(pointerId);
            if(controllerId != -1) {
                pressed = false;
            }
        }

        switch(controllerId) {
            case DRIVING_WHEEL_ID:
                controllerPointerMap[controllerId] = pointerId;
                if(pressed) {
                    drivingWheel.onPressed(x, y);
                } else {
                    drivingWheel.onReleased();
                }
                break;
            case TURRET_CONTROLLER_ID:
                controllerPointerMap[controllerId] = pointerId;
                if(pressed) {
                    turretController.onPressed(x, y);
                } else {
                    turretController.onReleased();
                }
                break;
        }
    }

    private void onMove(MotionEvent motionEvent)
    {
        int count = motionEvent.getPointerCount();
        for(int i = 0; i < count; ++i) {
            int pointerId = motionEvent.getPointerId(i);
            float x = motionEvent.getX(i);
            float y = motionEvent.getY(i);

            onPressed(pointerId, x, y);
        }
    }

    private void onUp(int pointerId)
    {
        int controllerId = getControllerId(pointerId);
        switch(controllerId) {
            case DRIVING_WHEEL_ID:
                drivingWheel.onReleased();
                controllerPointerMap[controllerId] = -1;
                break;
            case TURRET_CONTROLLER_ID:
                turretController.onReleased();
                controllerPointerMap[controllerId] = -1;
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int maskedAction = motionEvent.getActionMasked();
        int pointerIndex = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(pointerIndex);

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                float x = motionEvent.getX(pointerIndex);
                float y = motionEvent.getY(pointerIndex);
                onPressed(pointerId, x, y);
                break;
            }

            case MotionEvent.ACTION_MOVE:
                onMove(motionEvent);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                onUp(pointerId);
                break;
        }

        return true;
    }

    private class UpdateTask extends TimerTask {
        @Override
        public void run()
        {
            update();
            draw();
        }
    }
}
