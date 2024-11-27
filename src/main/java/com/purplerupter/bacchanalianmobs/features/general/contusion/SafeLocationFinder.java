package com.purplerupter.bacchanalianmobs.features.general.contusion;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.ai.RandomPositionGenerator;

public class SafeLocationFinder {

    public static BlockPos findSafeLocation(EntityCreature mob, byte radius, byte minHeight) {
        Vec3d randomTarget = RandomPositionGenerator.findRandomTarget(mob, radius, minHeight);

        if (randomTarget != null) {
            BlockPos targetPos = new BlockPos(randomTarget);
//            System.out.println("BlockPos is: " + targetPos);

            return targetPos;
        } else {
//            System.out.println("RandomPositionGenerator failed!");
        }

        return null;
    }
}
