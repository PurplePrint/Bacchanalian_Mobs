package com.purplerupter.bacchanalianmobs.transport.guardian;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class EntityGuardianBoat extends EntityGuardian {

    private int timeWithoutPassenger;
    private int jumpingTick;

    public EntityGuardianBoat(World worldIn) {
        super(worldIn);
//        System.out.println("EntityGuardianBoat called");
        this.experienceValue = 0;
        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D, 80));
        this.tasks.addTask(9, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        System.out.println("applyEntityAttributes...");
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(5.0D);
    }

    @Override
    public void onLivingUpdate() {
//        System.out.println("onLivingUpdate...");
        List<Entity> passengers = this.getPassengers();
//        System.out.println("passengers is: " + passengers);
        EntityLiving passenger = !passengers.isEmpty() && passengers.get(0) instanceof EntityLiving ? (EntityLiving) passengers.get(0) : null;
//        System.out.println("A passenger is: " + passenger);
        if (passenger == null || passenger.getAttackTarget() == null) {
//            System.out.println("A passenger is null or their attack target is null");
            this.timeWithoutPassenger++;
//            System.out.println("this.timeWithoutPassenger: " + this.timeWithoutPassenger);
            if (this.timeWithoutPassenger > 500) {
                System.out.println("Kill this boat");
                this.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            }
        } else {
            this.timeWithoutPassenger = 0;
        }
        if (passenger != null) {
//            System.out.println("A passenger is not null");
            EntityLivingBase target = passenger.getAttackTarget();
            passenger.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:water_breathing"), 10, 1, false, false));
            if(target != null){
                this.getNavigator().clearPath();
                this.getNavigator().tryMoveToXYZ(target.posX, target.posY, target.posZ, 1);
            }
        }
        if (this.isInWater()) { // TODO
            if (this.nearShore(0)) {
                this.jumpingTick = 20;
                this.motionY = 1;
                Vec3d facing = this.getLookVec().scale(0.5);
                this.motionX += facing.x;
                this.motionZ += facing.z;
            }
        }
        if (this.jumpingTick-- > 0) {
            Vec3d facing = this.getLookVec().scale(0.5);
            this.motionX = facing.x;
            this.motionZ = facing.z;
        }
        if (this.isOnLand()) {
            if (passenger != null) {
                passenger.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(
                        "minecraft:resistance"), 2, 4, false, false));
                passenger.getNavigator().clearPath();
                this.dismountEntity(passenger); // TODO
            }
            this.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE); // TODO
        }
        super.onLivingUpdate();
    }

    @Override
    public void fall (float distance, float damageMultiplier) {
    }

    private boolean nearShore (int cliffSize) {
        if(cliffSize < 3){
            BlockPos pos = this.getPosition().offset(this.getHorizontalFacing()).up(cliffSize);
            if(this.world.getBlockState(pos).getMaterial().isSolid() && this.world.getBlockState(pos.up()).getMaterial() == Material.AIR)
                return true;
            else
                return this.nearShore(cliffSize + 1);
        }
        return false;
    }

    @Override
    public boolean isMoving() { return true; }

    private boolean isOnLand() {
        if(this.world.getBlockState(this.getPosition()).getMaterial() != Material.WATER){
            return this.world.getBlockState(this.getPosition().down()).getMaterial().isSolid();
        }
        return false;
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!this.getPassengers().isEmpty())
            if (source.getTrueSource() == this.getPassengers().get(0))
                return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void onDeathUpdate() { this.setDead(); }

    @Override
    public void onDeath(DamageSource cause) {
    }
}
