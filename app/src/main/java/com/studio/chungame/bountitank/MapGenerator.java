package com.studio.chungame.bountitank;

import java.util.Random;

/**
 * Created by chun on 1/10/2018.
 */

public abstract class MapGenerator {
    public class Scale {
        public final static int SMALL = 0;
        public final static int MEDIUM = 1;
        public final static int BIG = 2;
    }

    private final static int[] MAP_SIZE_IN_TILES = {
            40, 80, 120, 160
    };

    private Random r = new Random();

    public MapGenerator()
    {
    }

    public abstract MapConfig generate(int scale, int tileBreath);
    {
    }

    protected int randomSize(int scale)
    {
        int minSize = MAP_SIZE_IN_TILES[scale];
        int maxSize = MAP_SIZE_IN_TILES[scale+1];
        return r.nextInt(maxSize-minSize) + minSize;
    }
}
