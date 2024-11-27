/*
    Триггер 1: Моб не может добраться до цели (path == null, то есть либо между мобом и целью бездна, либо нечастый сбой)
    Триггер 2: Моб слишком далеко от цели
    Триггер 3: Цель летает N тиков или дольше
 */

package com.purplerupter.bacchanalianmobs.transport.parrot;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.transport.parrot.CheckIfAbyss.checkIfAbyss;

// Эта ИИ задача непосредственно назначается мобу. Это триггер для вызова других ИИ задач связанных с оседланием попугая
public class ParrotRidingTrigger extends EntityAIBase {

    private final EntityLiving mob;
    private final World world;
    private EntityPlayer target;

//    private short farDistance = 24;
    private short farDistance = 32;


    private byte flyingBoost = 6; // максимальное количество блоков, на которое оседланный попугай взлетит вверх сразу после вызова

    public ParrotRidingTrigger(EntityLiving mob) {
        if (debug) { System.out.println("ParrotRidingTrigger called!"); }

        this.mob = mob;
        if (debug) { System.out.println("The EntityLiving is: " + mob); }
        this.world = mob.world;
    }

    @Override
    public boolean shouldExecute() {
        if (debug) { System.out.println("should execute the trigger?"); }

        if (this.mob.getAttackTarget() == null) {
            if (debug) { System.out.println("The mob has not any attack target. Cannot trigger parrot riding"); }
            return false;
        }

        // TODO стоит ли?
        if (!world.isAirBlock(new BlockPos(this.mob.posX, this.mob.posY + 2, this.mob.posZ)) ||
                !world.isAirBlock(new BlockPos(this.mob.posX, this.mob.posY + 3, this.mob.posZ))) {
            if (debug) { System.out.println("A roof layer is too close to the mob! Cannot trigger parrot riding."); }
            return false;
        }

        if (!(this.mob.getAttackTarget() instanceof EntityPlayer)) {
            return false;
        }
        this.target = (EntityPlayer) this.mob.getAttackTarget();

        if (checkIfAbyss(this.mob)) {
            if (debug) { System.out.println("Trigger the parrot riding because checkIfAbyss (available path to target is only by air or for some reason the mob's Path is null)"); }
            return true;
        }

        if (tooFar()) {
            if (debug) { System.out.println("Trigger the parrot riding because tooFar"); }
            return true;
        }

        return false;
    }

//    @Override
//    public void updateTask() {
//
//    }

    private boolean tooFar() {
        double distanceToTarget = this.mob.getDistanceSq(this.mob.getAttackTarget());
        if (distanceToTarget > farDistance * farDistance) {
            if (debug) { System.out.println("The distance to target is: " + distanceToTarget); }
            if (debug) { System.out.println("The far distance is: " + farDistance + " (or square: " + farDistance * farDistance + ")"); }
            return true;
        }

        return false;
    }

    @Override
    public void startExecuting() {
        mob.tasks.addTask(6, new CallParrot(mob));
    }

    @Override
    public boolean shouldContinueExecuting() {
        return true;
    }
}
