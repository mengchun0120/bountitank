package com.studio.chungame.bountitank;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chun on 12/20/2017.
 */

public class GameActivity extends Activity {
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        UIUtility.enableImmersiveMode(this);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        Resources resources = getResources();
        int id = resources.getIdentifier("navigation_bar_height",
                                         "dimen", "android");
        size.x += resources.getDimensionPixelSize(id);

        gameView = new GameView(this, size.x, size.y);
        setContentView(gameView);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        gameView.close();
    }
}
