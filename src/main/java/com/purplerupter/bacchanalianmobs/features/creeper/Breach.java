package com.purplerupter.bacchanalianmobs.features.creeper;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class Breach extends EntityAIBase {
    private final EntityCreeper creeper;
    private final World world;
    private EntityLivingBase target;
    private int tickCounter;
    private boolean yes;

    private static final short interval = 20;
    private byte maxDistance;
    private int maxDistanceSq;

    public Breach(EntityLiving entity, byte maxDistance) {
        this.creeper = (EntityCreeper) entity;
        this.world = entity.world;
        this.yes = false;

        this.maxDistance = maxDistance;
        this.maxDistanceSq = maxDistance * maxDistance;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public void updateTask() {
        if (this.yes && this.target != null && this.creeper.getAttackTarget() == this.target) {
            this.creeper.setCreeperState(1);
        }

        if (++this.tickCounter >= interval) {
            this.target = this.creeper.getAttackTarget();
            if (this.target instanceof EntityPlayer && !this.target.isAirBorne && this.creeper.getNavigator().noPath()) {

                double distanceToTarget = this.creeper.getDistanceSq(target);
                if (distanceToTarget <= maxDistanceSq) {

                    Vec3d startVec = new Vec3d(creeper.posX, this.creeper.posY + this.creeper.getEyeHeight(), this.creeper.posZ);
                    Vec3d endVec = new Vec3d(target.posX, this.target.posY + this.target.getEyeHeight(), this.target.posZ);

                    if (checkExplosionResistance(startVec, endVec)) {
                        this.creeper.setCreeperState(1);
                        this.target.sendMessage(new TextComponentString("Breach!"));
                        this.yes = true;
                    }
                }

            }
            this.tickCounter = 0;
        }
    }

    private boolean checkExplosionResistance(Vec3d startVec, Vec3d endVec) {
        BlockPos currentPos = new BlockPos(startVec);

        while (currentPos.distanceSq(endVec.x, endVec.y, endVec.z) > 1) {
            Block block = this.world.getBlockState(currentPos).getBlock();

            float resistance = block.getExplosionResistance(this.world, currentPos, this.creeper, null);
            if (resistance < 6.0F) { // 6.0F - стандартное значение взрывной силы крипера
                return true;
            }

            // Переход к следующему блоку на линии
            RayTraceResult result = this.world.rayTraceBlocks(startVec, endVec, false, true, false);
            if (result == null) break;
            currentPos = result.getBlockPos().offset(result.sideHit);
            startVec = new Vec3d(currentPos.getX(), currentPos.getY(), currentPos.getZ());
        }

        return false;
    }
}
