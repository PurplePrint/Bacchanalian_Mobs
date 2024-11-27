package com.purplerupter.bacchanalianmobs.features.general.avoidexplosions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.ai.RandomPositionGenerator;

import static com.purplerupter.bacchanalianmobs.features.general.avoidexplosions.HandleTNT.AVOID_RADIUS;

public class EntityAIRunAway extends EntityAIBase {
    private final EntityLiving entity;
    private final Entity danger;

    private BlockPos entityPos;
    private BlockPos dangerPos;

    private static final int AVOID_LIMIT = 30;

    private final int runDuration;
    private int runTicks;

    public EntityAIRunAway(EntityLiving entity, Entity danger, int runDuration) {
//        System.out.println("RunAway AI called");

        this.entity = entity;
//        System.out.println("Entity: " + entity);

        this.danger = danger;
        this.dangerPos = new BlockPos(danger);
//        System.out.println("Danger: " + danger);

        this.runDuration = runDuration;
        this.runTicks = 0;

        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute() {
        boolean dangerNotNull = this.danger != null;
//        System.out.println("Danger is not null: " + dangerNotNull);

        boolean isInDistance = this.entity.getDistance(this.danger) <= AVOID_RADIUS;
//        System.out.println("Is in distance: " + isInDistance);

        return dangerNotNull && isInDistance;
    }

    @Override
    public void startExecuting() {
        Vec3d runTarget = getRandomPosition();

        int bigCounter = 0;
        boolean exit = false;
        while (runTarget == null) {
//                || bigCounter > ( (int)AVOID_RADIUS * (int)AVOID_RADIUS * (int)(AVOID_RADIUS/3) )) { // Если доступных для спасения блоков слишком мало, моб может не заметить ни одного из них.
            while (bigCounter > ( (int)AVOID_RADIUS * (int)AVOID_RADIUS * (int)(AVOID_RADIUS/3) )) {

                runTarget = getRandomPosition();
                if (runTarget != null) {
//                    System.out.println("runTarget is not null! It is: " + runTarget);
                    exit = true;
                    break;
                }
                bigCounter++;
            }

            if (!exit) {
//                System.out.println("Cannot find a safety block in normal avoid radius. Radius will be expanded.");
                for (int y = (int)AVOID_RADIUS + 1; y <= AVOID_LIMIT; y++) {
                    for (int xz = (int)AVOID_RADIUS + 1; xz <= AVOID_LIMIT; xz++) {

                        runTarget = getRandomPosition(xz, y);
                        if (runTarget != null) {
                            break;
                        }
                    }

                    if (runTarget != null) {
                        break;
                    }
                }
            }

        }

        if (runTarget != null) {
            this.entity.getNavigator().tryMoveToXYZ(runTarget.x, runTarget.y, runTarget.z, 1.5);
//            System.out.println("Vec3d runTarget is: " + runTarget);
        } else {
//            System.out.println("Vec3d is null!");
        }

    }

    @Override
    public boolean shouldContinueExecuting() {
        BlockPos currentPos = new BlockPos(this.entity);
        if (currentPos.distanceSq(dangerPos) > AVOID_RADIUS*AVOID_RADIUS) {
            System.out.println("Entity is far enough!");
            return false;
        }

        boolean timer = this.runTicks < this.runDuration;
        System.out.println("runTicks: " + runTicks);
        if (!timer) { System.out.println("runTicks is not less than runDuration!"); }

        boolean dangerIsNotNull = this.danger != null;
        System.out.println("Danger is not null? " + dangerIsNotNull);

        return timer && dangerIsNotNull;
    }

    @Override
    public void updateTask() {
        System.out.println("updateTask");
        this.runTicks++;

        if (this.entity.getNavigator().noPath()) {
            System.out.println("updateTask: noPath!");
            Vec3d runTarget = getRandomPosition();
            if (runTarget != null) {
                System.out.println("updateTask: runTarget is not null!");
                this.entity.getNavigator().tryMoveToXYZ(runTarget.x, runTarget.y, runTarget.z, 1.5);

            } else { System.out.println("runTarget is null!"); }
        } else { System.out.println("Path exist for entity!"); }
    }

    @Override
    public void resetTask() {
        System.out.println("Reset task!");
        this.runTicks = 0;
        this.entity.getNavigator().clearPath();
        this.entity.tasks.removeTask(this);
    }

    private Vec3d getRandomPosition() {
        return getRandomPosition((int)AVOID_RADIUS, (int)AVOID_RADIUS);
    }

    private Vec3d getRandomPosition(int xz, int y) {
        if (this.danger != null) {
            Vec3d dangerPosition = this.danger.getPositionVector();
            System.out.println("The danger entity is at position: " + dangerPosition);

            Vec3d runTarget = RandomPositionGenerator.findRandomTargetBlockAwayFrom((EntityCreature) this.entity, xz, y, dangerPosition);
            return runTarget;
        }
        return null;
    }

}
