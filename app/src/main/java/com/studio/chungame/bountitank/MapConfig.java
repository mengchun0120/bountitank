package com.studio.chungame.bountitank;

import android.graphics.PointF;
import java.util.LinkedList;

/**
 * Created by chun on 1/10/2018.
 */

public class MapConfig {
    public int[][] tiles;
    public PointF playerPosition;
    public LinkedList<PointF> enemyPositions;

    public MapConfig(int numTilesX, int numTilesY)
    {
        tiles = new int[numTilesY][numTilesX];
        playerPosition = new PointF();
        enemyPositions = new LinkedList<PointF>();
    }

    public void fillTiles(int tile)
    {
        for(int i = 0; i < tiles.length; ++i) {
            for(int j = 0; j < tiles[i].length; ++j) {
                tiles[i][j] = tile;
            }
        }
    }
}
