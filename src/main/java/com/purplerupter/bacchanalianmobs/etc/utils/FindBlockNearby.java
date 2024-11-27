package com.purplerupter.bacchanalianmobs.etc.utils;

import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.breakblocks.ai.AITaskBreakBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class FindBlockNearby {

    public static boolean isBlockNearby(int x, int y, int z, byte maxSearchRange, float reach, World world, Entity destroyer, EntityCreature turret,
                                        ArrayList<String> turretBlocks, JsonObject turretDestroyConfig) {
        return isBlockNearby(x, y, z, maxSearchRange, reach, world, destroyer, turret, null,
                turretBlocks, turretDestroyConfig);
    }

    public static boolean isBlockNearby(int x, int y, int z, byte maxSearchRange, World world, EntitySpider spider, Block block) {
        return isBlockNearby(x, y, z, maxSearchRange, 0.0F, world, spider, null, block, null, null);
    }


    public static boolean isBlockNearby(int x, int y, int z, byte maxSearchRange, float reach, World world, Entity entity, EntityCreature turret, Block block,
                                        ArrayList<String> turretBlocks, JsonObject turretDestroyConfig) {
        boolean modeTurret = false;
        boolean modeWeb = false;

        if (turret != null && turretBlocks != null && reach > 0 && turretDestroyConfig != null
                && block == null) {
            modeTurret = true;

        } else if (block != null
                && turret == null && turretBlocks == null && reach == 0 && turretDestroyConfig == null) {
            modeWeb = true;

        } else {
            System.out.println("Unknown error when try to process 'isBlockNearby' method!");
            return false;
        }

        EntityLiving destroyer = null;

        boolean toolRequires = false;
        short diggingSpeed = 0;
        boolean sourceHardness = false;

        if (modeTurret) {
            destroyer = (EntityLiving) entity;

            toolRequires = turretDestroyConfig.get("Turret destroying (no tool)").getAsBoolean();
            diggingSpeed = turretDestroyConfig.get("Digging speed").getAsShort();
            sourceHardness = turretDestroyConfig.get("Use source hardness").getAsBoolean();
        }

//        if (debug) { System.out.println("Starting 'for' cycle to find the block of a turret..."); }
        for (byte range = 1; range <= maxSearchRange; range++) {
            for (byte yOffset = (byte) -range; yOffset <= range; yOffset++) {
                for (byte xOffset = (byte) -range; xOffset <= range; xOffset++) {
                    for (byte zOffset = (byte) -range; zOffset <= range; zOffset++) {

                        BlockPos currentPos = new BlockPos(x + xOffset, y + yOffset, z + zOffset);
                        IBlockState currentBlockState = world.getBlockState(currentPos);
                        Block currentBlock = currentBlockState.getBlock();

                        if (modeWeb) {
                            if (currentBlock instanceof BlockWeb) {
                                return true;
                            }

                        } else if (modeTurret) {
                            int meta = currentBlock.getMetaFromState(currentBlockState);
                            // Block ID in <modid:itemname:meta>
                            String currentBlockID = currentBlock.getRegistryName().toString() + ":" + meta;

                            if (turretBlocks.contains(currentBlockID)) {
                                if (debug) { System.out.println("Match this block! " + currentBlockID + " at coordinates XYZ: "
                                        + xOffset + ", " + yOffset + ", " + zOffset + " relative to the turret entity."); }

                                AITaskBreakBlock breakBlockTask = new AITaskBreakBlock(turret, destroyer, reach, currentPos, world, toolRequires, diggingSpeed, sourceHardness);
                                if (debug) { System.out.println("Called AI task to start breaking the turret block..."); }
                                destroyer.tasks.addTask(3, breakBlockTask); // priority???
                                if (debug) { System.out.println("breakBlockTask added to the " + destroyer + " entity"); }

                                return true;
                            }
                        }

                    }
                }
            }
        }

        return false; // if unable to find web block nearby
    }
}
