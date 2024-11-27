package com.purplerupter.bacchanalianmobs.features.spider.web;

import net.minecraft.util.math.BlockPos;

public class BlockBehind {
    public static BlockPos getDirectionBehind(int difX, int difY, int difZ) {
        float thresholdXZ = 0.2F; float thresholdY = 0.4F;

        boolean xNeg = (difX < 0); boolean yNeg = (difY < 0); boolean zNeg = (difZ < 0);
        byte xShiftAmount = 0; byte yShiftAmount = 0; byte zShiftAmount = 0;

        int xz = Math.abs(difX - difZ);

        float directionXZ = (float)xz / (float)Math.abs(difX);

        if ( directionXZ > (0.5 + thresholdXZ) ) {
            // по прямой X
            xShiftAmount = 1;
        }
        if ( directionXZ < (0.5 - thresholdXZ) ) {
            // По прямой Z
            zShiftAmount = 1;
        }
        if ( directionXZ >= (0.5 - thresholdXZ)
                || directionXZ <= (0.5 + thresholdXZ) ) {
            xShiftAmount = 1;
            zShiftAmount = 1;
        }

        int xy = Math.abs(difX - difY);
        float directionXY = (float)xy / (float)Math.abs(difX);

        if (difY >= 2) {

            if ( directionXY > (0.5 + thresholdY) ) {
                yShiftAmount = 1;
            }
            if ( directionXY < (0.5 - thresholdY) ) {
                yShiftAmount = 1;
            }
            if ( directionXY >= (0.5 - thresholdY)
                    || directionXY <= (0.5 + thresholdY) ) {
                // 'x' already processed
                yShiftAmount = 1;
            }
        }


        byte shiftX = xNeg ? xShiftAmount : (byte) -xShiftAmount;
        byte shiftY = yNeg ? yShiftAmount : (byte) -yShiftAmount;
        byte shiftZ = zNeg ? zShiftAmount : (byte) -zShiftAmount;

        return new BlockPos(shiftX, shiftY, shiftZ);
    }
}
