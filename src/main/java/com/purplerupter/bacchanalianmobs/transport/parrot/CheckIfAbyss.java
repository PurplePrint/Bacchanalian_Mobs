package com.purplerupter.bacchanalianmobs.transport.parrot;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

// Этот класс решает, стоит ли вызвать попугая (если путь по земле невозможен/затруднён) или не стоит (если моб может настигнуть цель на своих двоих)
public class CheckIfAbyss {

    private static short timer = 0;

    public static boolean checkIfAbyss(EntityLiving mob) {
        if (!mob.onGround) { // TODO
            return false;
        }

        if (mob.getAttackTarget() == null) {
            return false;
        }
        EntityLivingBase target = mob.getAttackTarget();

        if (target.isAirBorne || target.isInWater() || target.isInLava() || target.isElytraFlying()) {
            return false;
        }

//        if (mob.getDistance(target) <= getAttackReach(mob)) {
//            return false;
//        }

//        if (mob.getNavigator() instanceof PathNavigateGround || mob.getNavigator() instanceof PathNavigateSwimmer) {
        if (mob.getNavigator() instanceof PathNavigateGround) {
            Path normalPath = mob.getNavigator().getPathToEntityLiving(target);
            if (normalPath == null) {

                timer++;
                if (timer >= 40) {

                    EntityLiving flyingChecker = createFlyingEntity(mob);
                    if (flyingChecker != null) {
                        Path airPath = flyingChecker.getNavigator().getPathToEntityLiving(target);
                        if (airPath != null) {

                            if (debug) { System.out.println("Mob cannot reach the target by ground - only by air"); }
//                        mob.tasks.addTask(6, new AITransportFlying(mob));
                            return true; // Воздушный путь найден, выводим сообщение
                        }
                    }
                }
            }
        }

        return false; // Путь по земле найден или фейковый проверяющий не смог найти воздушный путь
    }

    // Метод для создания фейковой сущности, которая может летать
    private static EntityLiving createFlyingEntity(EntityLiving originalMob) {
        EntityLiving flyingChecker = null;

        try {
            // Создаем копию моба, но с воздушной навигацией
            flyingChecker = new EntityLiving(originalMob.world) {
                @Override
                protected PathNavigateFlying createNavigator(World worldIn) {
                    return new PathNavigateFlying(this, worldIn);
                }
            };

            // Переносим фейковую сущность на координаты оригинального моба
            flyingChecker.setPosition(originalMob.posX, originalMob.posY, originalMob.posZ);
            flyingChecker.setAttackTarget(originalMob.getAttackTarget());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return flyingChecker;
    }

}
