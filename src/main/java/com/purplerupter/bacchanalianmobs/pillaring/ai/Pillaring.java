package com.purplerupter.bacchanalianmobs.pillaring.ai;

import net.minecraft.entity.ai.*;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.entity.*;
import net.minecraft.block.*;
import net.minecraft.init.*;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.pillaring.PillaringConfigHandler.getPillaringBlock;

public class Pillaring extends EntityAIBase
{
    public static ResourceLocation blockName;
    public static int blockMeta;
    public static boolean updateBlock;
    private static IBlockState pillarBlock;
    private static final EnumFacing[] placeSurface;
    private int placeDelay;
    private EntityLiving builder;
    public EntityLivingBase target;
    private BlockPos blockPos;
    private final String blockString;

    public Pillaring(final EntityLiving entity, String blockString) {
        this.placeDelay = 15;
        this.builder = entity;
        this.blockString = blockString;
        if (debug) { System.out.println("The pillaring entity is: " + this.builder + " // The pillaring block is: " + blockString); }
    }

    public boolean shouldExecute() {
        this.target = this.builder.getAttackTarget();
        if (this.target == null || !this.target.isEntityAlive()) {
            return false;
        }
        if (!this.builder.getNavigator().noPath() ||
                ((this.builder.getDistance(this.target.posX, this.builder.posY, this.target.posZ) >= 4.0 || !this.builder.onGround) && !this.builder.isInLava() && !this.builder.isInWater())) {
            return false;
        }
        final BlockPos orgPos;
        BlockPos tmpPos = orgPos = this.builder.getPosition();
        final int xOff = (int)Math.signum((float)(MathHelper.floor(this.target.posX) - orgPos.getX()));
        final int zOff = (int)Math.signum((float)(MathHelper.floor(this.target.posZ) - orgPos.getZ()));
        boolean canPlace = false;
        for (final EnumFacing dir : Pillaring.placeSurface) {
            if (this.builder.world.getBlockState(tmpPos.offset(dir)).isNormalCube()) {
                canPlace = true;
                break;
            }
        }
        if (this.target.posY - this.builder.posY < 16.0 && this.builder.world.getBlockState(tmpPos.add(0, -2, 0)).isNormalCube() && this.builder.world.getBlockState(tmpPos.add(0, -1, 0)).isNormalCube()) {
            if (this.builder.world.getBlockState(tmpPos.add(xOff, -1, 0)).getMaterial().isReplaceable()) {
                tmpPos = tmpPos.add(xOff, -1, 0);
            }
            else if (this.builder.world.getBlockState(tmpPos.add(0, -1, zOff)).getMaterial().isReplaceable()) {
                tmpPos = tmpPos.add(0, -1, zOff);
            }
            else if (this.target.posY <= this.builder.posY) {
                return false;
            }
        }
        else if (this.target.posY <= this.builder.posY) {
            return false;
        }
        if (!canPlace || this.builder.world.getBlockState(orgPos.add(0, 2, 0)).getMaterial().blocksMovement() || this.builder.world.getBlockState(tmpPos.add(0, 2, 0)).getMaterial().blocksMovement()) {
            return false;
        }
        this.blockPos = tmpPos;
        return true;
    }

    public void startExecuting() {
        this.placeDelay = 15;
        if (Pillaring.updateBlock) {
            this.updatePillarBlock();
            Pillaring.updateBlock = false;
        }
    }

    public boolean shouldContinueExecuting() {
        return this.shouldExecute();
    }

    public void updateTask() {
        if (this.placeDelay > 0 || this.target == null) {
            --this.placeDelay;
        }
        else if (this.blockPos != null) {
            this.placeDelay = 15;
            this.builder.setPositionAndUpdate(this.blockPos.getX() + 0.5, this.blockPos.getY() + 1.0, this.blockPos.getZ() + 0.5);
            if (this.builder.world.getBlockState(this.blockPos).getMaterial().isReplaceable()) {
                this.builder.world.setBlockState(this.blockPos, Pillaring.pillarBlock);
            }
            this.builder.getNavigator().setPath(this.builder.getNavigator().getPathToEntityLiving((Entity)this.target), this.builder.getMoveHelper().getSpeed());
        }
    }

    public boolean isInterruptible() {
        return false;
    }

    private void updatePillarBlock() {
        final String[] cfgSplit = this.blockString.split(":");
        if (cfgSplit.length == 2 || cfgSplit.length == 3) {
            Pillaring.blockName = new ResourceLocation(cfgSplit[0], cfgSplit[1]);
            if (cfgSplit.length == 3) {
                try {
                    Pillaring.blockMeta = Integer.parseInt(cfgSplit[2]);
                }
                catch (Exception e) {
                    if (debug) { System.out.println("Unable to parse pillar block metadata from: " + blockName); }
                    e.printStackTrace();
                    Pillaring.blockMeta = -1;
                }
            }
            else {
                Pillaring.blockMeta = -1;
            }
        }
        else {
            if (debug) { System.out.println("Incorrectly formatted pillar block config: " + blockName); }
            Pillaring.blockName = new ResourceLocation("minecraft:cobblestone");
            Pillaring.blockMeta = -1;
        }
        try {
            final Block b = (Block)Block.REGISTRY.getObject(Pillaring.blockName);
            if (b == Blocks.AIR) {
                Pillaring.pillarBlock = Blocks.COBBLESTONE.getDefaultState();
            }
            else {
                Pillaring.pillarBlock = ((Pillaring.blockMeta < 0) ? b.getDefaultState() : b.getStateFromMeta(Pillaring.blockMeta));
            }
        }
        catch (Exception e) {
            if (debug) { System.out.println("Unable to read pillaring block from config"); }
            e.printStackTrace();
            Pillaring.pillarBlock = Blocks.COBBLESTONE.getDefaultState();
        }
    }

    static {
        Pillaring.blockName = new ResourceLocation("minecraft:cobblestone");
        Pillaring.blockMeta = -1;
        Pillaring.updateBlock = false;
        Pillaring.pillarBlock = Blocks.COBBLESTONE.getDefaultState();
        placeSurface = new EnumFacing[] { EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST };
    }
}