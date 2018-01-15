package com.studio.chungame.bountitank;

/**
 * Created by chun on 1/10/2018.
 */

public class TunnelMapGenerator extends MapGenerator {
    public TunnelMapGenerator()
    {
        super();
    }

    @Override
    public MapConfig generate(int scale, int tileBreath)
    {
        if(scale < Scale.SMALL || scale > Scale.BIG) {
            throw new RuntimeException("Invalid scale");
        }

        int widthInTiles = randomSize(scale);
        int heightInTiles = randomSize(scale);
        MapConfig mapConfig = new MapConfig(widthInTiles, heightInTiles);

        mapConfig.fillTiles(1);

        return null;
    }

    private class TileRemover {
        public int[] availableDirections;
        public int curDirections;
        public int stepsLeftCurDirections;
        public int stepsLeft;
        public boolean isMainRemover;

        public TileRemover(int mainDirection, boolean isMainRemover)
        {

        }
    }
}
