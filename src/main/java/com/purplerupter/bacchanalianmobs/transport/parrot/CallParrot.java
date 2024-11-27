package com.purplerupter.bacchanalianmobs.transport.parrot;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.transport.parrot.CheckIfAbyss.checkIfAbyss;

// Эта ИИ задача вызывает транспортного попугая (кастомный entity, наследуемый от ванильного попугая)
public class CallParrot extends EntityAIBase {

    private final EntityLiving mob;
    private final EntityPlayer target;
    private int wait = 0; // ticks

    private int flyingTicksCounter;
    private int flyingTicksMax = 100;

    private boolean cancel = false;

    public CallParrot(EntityLiving mob) { // вызывается не один раз для моба, но редко // или один раз??
        if (debug) { System.out.println("CallParrot called. The passenger mob is: " + mob); }
        this.mob = mob;
        this.target = (EntityPlayer) mob.getAttackTarget();

        this.flyingTicksCounter = 0;
        this.wait = 0;
    }

    @Override
    public boolean shouldExecute() {
        if (debug) { System.out.println("Should execute the CallParrot?"); }

//        if (this.cancel) {
//            return false; }

        if (this.mob.isRiding()) {
            return false; }

        if (this.mob.getAttackTarget() == null || !(this.mob.getAttackTarget() instanceof EntityPlayer)) {
            return false; }

        // TODO стоит ли?
        if (!this.mob.world.isAirBlock(new BlockPos(this.mob.posX, this.mob.posY + 2, this.mob.posZ)) ||
                !this.mob.world.isAirBlock(new BlockPos(this.mob.posX, this.mob.posY + 3, this.mob.posZ))) {
            if (debug) { System.out.println("A roof layer is too close to the mob! Cannot trigger parrot riding."); }
            return false;
        }

        if (this.mob.isInWater() || this.mob.isInLava()) {
            if (debug) { System.out.println("The mob: " + this.mob + " is in lava or in water. Cannot call transport parrot for this mob."); }
        return false; }
        if (this.mob.isOnLadder()) {
            if (debug) { System.out.println("The mob: " + this.mob + " is on ladder. Cannot call transport parrot for this mob."); }
        return false; }

//        if (flyingTicksCounter >= flyingTicksMax) {
//            flyingTicksCounter = 0;
//            return true;
//        }
//        if (isFlying(this.target)) {
//            flyingTicksCounter++;
//            if (debug) { System.out.println("The flying ticks counter is: " + flyingTicksCounter); }
//        }

        if (this.wait == 40) {
            if (checkIfAbyss(this.mob)) {
                return true; }
            }
        if (this.wait < 40) { this.wait++; }
//        else { this.wait = 0; }
//
//        if (debug) { System.out.println("Something went wrong... Any 'if' conditions in shouldExecute() was not triggered!"); }
        return false;
    }

//    private boolean isFlying(EntityPlayer target) {
//        if (target.capabilities.isFlying) {
////            if (debug) { System.out.println("The mob's target, player " + target + " flying!"); }
//            return true;
//        }
//
//        return false;
//    }

    @Override
    public void startExecuting() {
        if (debug) { System.out.println("startExecuting..."); }

        EntityTransportParrot parrot = new EntityTransportParrot(this.mob.world);
        if (debug) { System.out.println("The parrot entity is: " + parrot); }
        parrot.setLocationAndAngles(this.mob.posX, this.mob.posY, this.mob.posZ, this.mob.rotationYaw, this.mob.rotationPitch);
        if (debug) { System.out.println("setLocationAndAngles processed."); }
        this.mob.world.spawnEntity(parrot);
        if (debug) { System.out.println("spawnEntity (parrot) processed"); }
        this.mob.startRiding(parrot);
        if (debug) { System.out.println("startRiding (parrot) processed"); }

        if (debug) { System.out.println("this.wait is: " + this.wait); }
        this.wait = 0;
        if (debug) { System.out.println("this.wait now is 0: " + this.wait); }

//        resetTask();
    }

    @Override
    public void resetTask() {
        this.cancel = true;
    }
}
