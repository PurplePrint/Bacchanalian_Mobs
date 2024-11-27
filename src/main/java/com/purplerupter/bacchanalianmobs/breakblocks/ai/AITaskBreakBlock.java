package com.purplerupter.bacchanalianmobs.breakblocks.ai;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.breakblocks.utils.BlockHardness.getBlockHardness;

public class AITaskBreakBlock extends EntityAIBase {
    private final EntityCreature entityTurret;
    private final EntityLiving destroyer;
    private final float reach;
    private final BlockPos blockPos;
    private final World world;
    private float breakProgress;
//    private final int breakThreshold;
    private final boolean toolRequires;
    private final float diggingSpeed;
    private boolean sourceHardness;
    private int digTick;
//    private final int totalBreakTicks = 50; //

    public AITaskBreakBlock(EntityCreature entityTurret, EntityLiving destroyer, float reach, BlockPos blockPos, World world, boolean toolRequires, float diggingSpeed, boolean sourceHardness) {
        if (debug) { System.out.println("AITaskBreakBlock called"); }

        this.entityTurret = entityTurret;
        this.destroyer = destroyer;
        this.reach = reach;
        this.blockPos = blockPos;
        this.world = world;
        this.breakProgress = 0;
//        this.breakThreshold = 10; // Порог для разрушения в процентах от завершённого процесса
        this.toolRequires = toolRequires;
        this.diggingSpeed = diggingSpeed;
        this.sourceHardness = sourceHardness;
        this.digTick = 0;
    }

    public boolean shouldExecute() {
        if (entityTurret.getHealth() > 0) {
            if (debug) { System.out.println("Entity turret's health is greater than 0. It is: " + entityTurret.getHealth() + ". Skipping execution."); }
            return false;
        }

        if (!canHarvest(this.destroyer, this.blockPos, this.toolRequires)) {
            if (debug) { System.out.println("The entity destroyer has not tool, but tool requires. Skipping execution."); }
            return false;
        }

        IBlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock().isAir(blockState, world, blockPos)) {
            if (debug) { System.out.println("Target block is air. Skipping execution."); }
            return false;
        }

        if (debug) { System.out.println("Execution condition met."); }
        return true;
    }

    public boolean shouldContinueExecuting() {
        float distance = this.destroyer.getDistance(this.entityTurret);
        if (debug) { System.out.println("The destroyer's distance from entity turret is: " + distance + " // the required distance is <= " + this.reach); }
        boolean shouldContinue = this.blockPos != null && distance <= this.reach && this.canHarvest(this.destroyer, this.blockPos, this.toolRequires);
        if (debug) { System.out.println("shouldContinueExecuting: " + shouldContinue);
            if (!shouldContinue) { System.out.println("Reasons for not continuing: blockPos null? " + (this.blockPos == null) +
                        ", destroyer too far? " + (this.destroyer.getDistanceSq(this.blockPos) > 3.0) +
                        ", canHarvest? " + this.canHarvest(this.destroyer, this.blockPos, this.toolRequires)); } }
        return shouldContinue;
    }

    public void updateTask() {
        if (this.destroyer.getDistance(this.entityTurret) <= this.reach) {
            if (debug) { System.out.println("Update task..."); }

            this.destroyer.getLookHelper().setLookPosition(this.blockPos.getX(), this.blockPos.getY(), this.blockPos.getZ(), (float)this.destroyer.getHorizontalFaceSpeed(), (float)this.destroyer.getVerticalFaceSpeed());
            this.destroyer.getNavigator().clearPath();

            ++this.digTick;

            IBlockState blockState = world.getBlockState(this.blockPos);
            float blockHardness = getBlockHardness((EntityLivingBase) this.destroyer, this.destroyer.world, this.blockPos);
            if (debug) { System.out.println("The block hardness (relative to the entity" + this.destroyer + " digging speed) is: " + blockHardness); }

            // Убедись, что твёрдость блока больше 0, иначе прогресс не изменится
            if (blockHardness > 0) {
                float progressPerTick = 1.0f;
                if (sourceHardness) {
                    progressPerTick /= (blockHardness * diggingSpeed); }
                else {
                    progressPerTick /= diggingSpeed; }
                this.breakProgress += progressPerTick;
                if (debug) { System.out.println("digTick: " + this.digTick + ", breakProgress: " + this.breakProgress + ", blockHardness: " + blockHardness); }

                if (this.breakProgress >= 1.0f) {
                    if (debug) { System.out.println("Block broken!"); }

                    this.world.destroyBlock(this.blockPos, false);

                    if (this.canHarvest(this.destroyer, this.blockPos, this.toolRequires) && this.world instanceof WorldServer) {
                        final FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer)this.world);
                        player.setHeldItem(EnumHand.MAIN_HAND, this.destroyer.getHeldItem(EnumHand.MAIN_HAND));
                        TileEntity tile = this.world.getTileEntity(this.blockPos);
                        blockState.getBlock().harvestBlock(this.world, player, this.blockPos, blockState, tile, player.getHeldItem(EnumHand.MAIN_HAND));
                    }

                    this.resetTask();

                } else if (this.digTick % 20 == 0) { // Каждые 20 тиков проигрывать звук и показывать прогресс
                    this.world.playSound((EntityPlayer)null, this.blockPos, blockState.getBlock().getSoundType(blockState, this.world, this.blockPos, (Entity)this.destroyer).getHitSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
                    this.destroyer.swingArm(EnumHand.MAIN_HAND);
                    this.world.sendBlockBreakProgress(this.destroyer.getEntityId(), this.blockPos, (int)(this.breakProgress * 10.0f));
                }

            } else { if (debug) { System.out.println("Error! The block hardness is less than 0! It's: " + blockHardness); } }

        } else {
            this.destroyer.getNavigator().setPath(this.destroyer.getNavigator().getPathToPos(this.blockPos), this.destroyer.getMoveHelper().getSpeed());
        }
    }

    public void resetTask() {
        if (debug) { System.out.println("resetTask called. Resetting breakProgress and digTick."); }
        this.breakProgress = 0;
        this.digTick = 0;
    }

    private boolean canHarvest(EntityLiving destroyer, BlockPos pos, boolean toolRequires) {
        IBlockState state = destroyer.world.getBlockState(pos);

        if (!toolRequires) {
            return true; }

        return state.getMaterial().isToolNotRequired() ||
                (!this.destroyer.getHeldItem(EnumHand.MAIN_HAND).isEmpty()
                        && this.destroyer.getHeldItem(EnumHand.MAIN_HAND).canHarvestBlock(state));

    }
}

