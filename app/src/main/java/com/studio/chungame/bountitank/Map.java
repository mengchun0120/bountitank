package com.studio.chungame.bountitank;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 * Created by chun on 12/28/2017.
 */

public class Map {
    public final static int SMALL = 0;
    public final static int MEDIUM = 1;
    public final static int BIG = 2;

    private final static int BLOCK_FACTOR = 120;
    private final static int REGION_BREATH_IN_BLOCKS = 20;

    private final static int[] MAP_RANGE_IN_REGIONS = {
            10, 20, 30, 40
    }; // map size ranges in terms of regions

    private int screenWidth;
    private int screenHeight;
    private int blockInPixels;
    private int regionBreathInPixels;
    private int widthInRegions;
    private int heightInRegions;
    private int widthInPixels;
    private int heightInPixels;
    private RectF visibleArea = new RectF();
    private Tank player;
    private Tile[][] tiles;
    private LinkedList<Bullet> bullets;
    private LinkedList<Explosion> explosions;

    public Map(int scale, int screenWidth, int screenHeight)
    {
        if(scale < 0 || scale > BIG) {
            throw new RuntimeException("Invalid scale");
        }

        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        blockInPixels = screenWidth / BLOCK_FACTOR;
        regionBreathInPixels = REGION_BREATH_IN_BLOCKS * blockInPixels;

        Random r = new Random();

        widthInRegions = r.nextInt(MAP_RANGE_IN_REGIONS[scale + 1]
                                - MAP_RANGE_IN_REGIONS[scale])
                                + MAP_RANGE_IN_REGIONS[scale];
        heightInRegions = r.nextInt(MAP_RANGE_IN_REGIONS[scale + 1]
                                - MAP_RANGE_IN_REGIONS[scale])
                                + MAP_RANGE_IN_REGIONS[scale];

        widthInPixels = widthInRegions * regionBreathInPixels;
        heightInPixels = heightInRegions * regionBreathInPixels;

        Bullet.prepare(blockInPixels);
        Tile.prepare(blockInPixels);
        Tank.prepare(blockInPixels);
        Explosion.prepare(blockInPixels);

        int heightInTiles = heightInPixels / Tile.getTileBreathInPixels();
        if(heightInPixels % Tile.getTileBreathInPixels() != 0) {
            heightInTiles++;
        }

        int widthInTiles = widthInPixels / Tile.getTileBreathInPixels();
        if(widthInPixels % Tile.getTileBreathInPixels() != 0) {
            widthInTiles++;
        }

        tiles = new Tile[heightInTiles][widthInTiles];
        bullets = new LinkedList<Bullet>();
        explosions = new LinkedList<Explosion>();

        genMap();
        initPlayer();
        updateVisibleArea();
    }

    public int getWidthInPixels()
    {
        return widthInPixels;
    }

    public int getHeightInPixels()
    {
        return heightInPixels;
    }

    public boolean rectOutsideVisibleArea(float top, float left, float bottom, float right)
    {
        return top > visibleArea.bottom || bottom < visibleArea.top ||
               left > visibleArea.right || right < visibleArea.left;
    }

    public void update()
    {
        ListIterator<Bullet> bulletIt = bullets.listIterator();
        while(bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update(this);
            if(bullet.checkFlag(GameObject.DEAD)) {
                bulletIt.remove();
            }
        }

        player.update(this);

        ListIterator<Explosion> explosionIt = explosions.listIterator();
        while(explosionIt.hasNext()) {
            Explosion e = explosionIt.next();
            e.update(this);
            if(e.checkFlag(GameObject.DEAD)) {
                explosionIt.remove();
            }
        }

        updateVisibleArea();
    }

    public void draw(Canvas canvas)
    {
        drawVisibleTiles(canvas);

        player.draw(canvas, visibleArea.left, visibleArea.top);

        ListIterator<Bullet> bulletIt = bullets.listIterator();
        while(bulletIt.hasNext()) {
            bulletIt.next().draw(canvas, visibleArea.left, visibleArea.top);
        }

        ListIterator<Explosion> explosionIt = explosions.listIterator();
        while(explosionIt.hasNext()) {
            explosionIt.next().draw(canvas, visibleArea.left, visibleArea.top);
        }
    }

    public void drawVisibleTiles(Canvas canvas)
    {
        int startRow = (int)Math.floor(visibleArea.top / Tile.getTileBreathInPixels());
        int endRow = (int)Math.floor(visibleArea.bottom / Tile.getTileBreathInPixels());
        int startCol = (int)Math.floor(visibleArea.left / Tile.getTileBreathInPixels());
        int endCol = (int)Math.floor(visibleArea.right / Tile.getTileBreathInPixels());

        for(int row = startRow; row <= endRow; ++row) {
            for(int col = startCol; col <= endCol; ++col) {
                if(tiles[row][col] != null) {
                    tiles[row][col].draw(canvas, visibleArea.left, visibleArea.top);
                }
            }
        }
    }

    public void genMap()
    {
         genTiles();
         genTanks();
    }

    public void genTiles()
    {
        final int MIN_TILES = 3;
        final int MAX_TILES = 8;
        final double TILE_PROB = 0.2;
        float top = 0.0f;
        int row;
        Random r = new Random(System.nanoTime());

        for(row = 0; row < tiles.length - 2; ++row, top += Tile.getTileBreathInPixels()) {
            int tileType = Tile.NO_TILE;
            int tileIndex = 0, tileCount = 0;
            float left = 0.0f;

            for(int col = 0; col < tiles[row].length; ++col) {
                if(tileIndex == tileCount) {
                    double dice = r.nextDouble();
                    if(dice <= TILE_PROB) {
                        tileType = r.nextInt(Tile.TYPE_COUNT);
                    } else {
                        tileType = Tile.NO_TILE;
                    }
                    tileCount = r.nextInt(MAX_TILES - MIN_TILES + 1) + MIN_TILES;
                    tileIndex = 0;
                }

                tiles[row][col] = (tileType == Tile.NO_TILE) ?
                                    null : new Tile(tileType, top, left);
                ++tileIndex;
                left += Tile.getTileBreathInPixels();
            }
        }

        for(; row < tiles.length; ++row, top += Tile.getTileBreathInPixels()) {
            for(int col = 0; col < tiles[row].length; ++col) {
                tiles[row][col] = null;
            }
        }
    }

    public void genTanks()
    {
    }

    public void addBullet(Bullet bullet)
    {
        bullets.addLast(bullet);
    }

    public void initVisibleArea()
    {
        visibleArea.bottom = heightInPixels - 1.0f;
        visibleArea.top = visibleArea.bottom - screenHeight + 1.0f;
        visibleArea.left = (widthInPixels - screenWidth) / 2.0f;
        visibleArea.right = visibleArea.left + screenWidth - 1.0f;
    }

    public void initPlayer()
    {
        float top = heightInPixels - Tank.getTankBreathInPixels();
        float left = (widthInPixels - Tank.getTankBreathInPixels()) / 2.0f;
        player = new Tank(Tank.DEER, Tank.ENEMY, DrivingWheel.UP, 5f,
                        TurretController.DEF_ATTACK_DIRECTION_X,
                        TurretController.DEF_ATTACK_DIRECTION_Y,
                        50, top, left);
    }

    public Tank getPlayer()
    {
        return player;
    }

    public void updateVisibleArea()
    {
        updateVisibleAreaVertical();
        updateVisibleAreaHorizontal();
    }

    private void updateVisibleAreaVertical() {
        float newTop = player.getBound().centerY() - screenHeight / 2.0f;

        if (newTop < 0) {
            visibleArea.top = 0.0f;
            visibleArea.bottom = visibleArea.top + screenHeight - 1.0f;
            return;
        }

        float newBottom = newTop + screenHeight - 1.0f;
        float upperBound = heightInPixels - 1.0f;

        if (newBottom > upperBound) {
            visibleArea.bottom = upperBound;
            visibleArea.top = visibleArea.bottom - screenHeight + 1.0f;
            return;
        }

        visibleArea.top = newTop;
        visibleArea.bottom = newBottom;
    }

    private void updateVisibleAreaHorizontal()
    {
        float newLeft = player.getBound().centerX() - screenWidth/2.0f;

        if(newLeft < 0) {
            visibleArea.left = 0.0f;
            visibleArea.right = visibleArea.left + screenWidth - 1.0f;
            return;
        }

        float newRight = newLeft + screenWidth - 1.0f;
        float upperBound = widthInPixels - 1.0f;

        if(newRight > upperBound) {
            visibleArea.right = upperBound;
            visibleArea.left = visibleArea.right - screenWidth + 1.0f;
            return;
        }

        visibleArea.left = newLeft;
        visibleArea.right = newRight;
    }

    public GameObject checkTankClash(float top, float left, float bottom, float right,
                                     int direction)
    {
        GameObject clashingObj = checkTankClashingTile(top, left, bottom, right, direction);
        return clashingObj;
    }

    public boolean checkBulletClash(float top, float left, float bottom, float right,
                                    int attackPower)
    {
        if(checkBulletClashingTile(top, left, bottom, right, attackPower)) {
            return true;
        }

        return false;
    }

    public boolean checkBulletClashingTile(float top, float left, float bottom, float right,
                                           int attackPower)
    {
        int minTileRow = (int)Math.floor(top / Tile.getTileBreathInPixels());
        if(minTileRow < 0) {
            minTileRow = 0;
        }

        int maxTileRow = (int)Math.floor(bottom / Tile.getTileBreathInPixels());
        if(maxTileRow >= tiles.length) {
            maxTileRow = tiles.length - 1;
        }

        int minTileCol = (int)Math.floor(left / Tile.getTileBreathInPixels());
        if(minTileCol < 0) {
            minTileCol = 0;
        }

        int maxTileCol = (int)Math.floor(right / Tile.getTileBreathInPixels());
        if(maxTileCol >= tiles[0].length) {
            maxTileCol = tiles[0].length - 1;
        }

        boolean hit = false;

        for(int row = minTileRow; row <= maxTileRow; ++row) {
            for(int col = minTileCol; col <= maxTileCol; ++col) {
                if(tiles[row][col] != null) {
                    tiles[row][col].onHit(attackPower);
                    if(tiles[row][col].checkFlag(GameObject.DEAD)) {
                        tiles[row][col] = null;
                    }

                    hit = true;
                }
            }
        }

        return hit;
    }

    public Tile checkTankClashingTile(float top, float left, float bottom, float right, int direction)
    {
        // Check tiles
        int minTileRow = (int)Math.floor(top / Tile.getTileBreathInPixels());
        if(minTileRow < 0) {
            minTileRow = 0;
        }

        int maxTileRow = (int)Math.floor(bottom / Tile.getTileBreathInPixels());
        if(maxTileRow >= tiles.length) {
            maxTileRow = tiles.length - 1;
        }

        int minTileCol = (int)Math.floor(left / Tile.getTileBreathInPixels());
        if(minTileCol < 0) {
            minTileCol = 0;
        }

        int maxTileCol = (int)Math.floor(right / Tile.getTileBreathInPixels());
        if(maxTileCol >= tiles[0].length) {
            maxTileCol = tiles[0].length - 1;
        }

        switch(direction) {
            case DrivingWheel.UP: {
                for(int row = maxTileRow; row >= minTileRow; --row) {
                    for(int col = minTileCol; col <= maxTileCol; ++col) {
                        if(tiles[row][col] != null) {
                            return tiles[row][col];
                        }
                    }
                }
                break;
            }
            case DrivingWheel.DOWN: {
                for(int row = minTileRow; row <= maxTileRow; ++row) {
                    for(int col = minTileCol; col <= maxTileCol; ++col) {
                        if(tiles[row][col] != null) {
                            return tiles[row][col];
                        }
                    }
                }
                break;
            }
            case DrivingWheel.LEFT: {
                for(int col = maxTileCol; col >= minTileCol; --col) {
                    for(int row = minTileRow; row <= maxTileRow; ++row) {
                        if(tiles[row][col] != null) {
                            return tiles[row][col];
                        }
                    }
                }
                break;
            }
            case DrivingWheel.RIGHT: {
                for(int col = minTileCol; col <= maxTileCol; ++col) {
                    for(int row = minTileRow; row <= maxTileRow; ++row) {
                        if(tiles[row][col] != null) {
                            return tiles[row][col];
                        }
                    }
                }
            }
        }

        return null;
    }

    public void addExplosion(int side, float centerX, float centerY)
    {
        Explosion explosion = new Explosion(side, centerX, centerY);
        explosions.addLast(explosion);
    }
}
