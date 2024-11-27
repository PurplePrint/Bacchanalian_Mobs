package com.purplerupter.bacchanalianmobs.features.general.avoidexplosions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.features.general.avoidexplosions.HandleTNT.AVOID_RADIUS;

public class MobsInArea {
    public static void callMobsInArea(Entity explosive, World world, int fuse) {

        AxisAlignedBB area = new AxisAlignedBB(
                explosive.posX - AVOID_RADIUS, explosive.posY - AVOID_RADIUS, explosive.posZ - AVOID_RADIUS,
                explosive.posX + AVOID_RADIUS, explosive.posY + AVOID_RADIUS, explosive.posZ + AVOID_RADIUS
        );

        // Находим всех мобов вокруг и назначаем задачу убегать
        world.getEntitiesWithinAABB(EntityLiving.class, area).forEach(entity -> {
            if (entity instanceof EntityLiving
                    && (entity.getEntityData().getBoolean("AvoidExplosions"))
                    && !(entity.getEntityData().hasKey("kamikazeCreeper")) ) {
                System.out.println("EntityLiving in the radius of TNT primed: " + entity);
                System.out.println("Tasks: " + entity.tasks.taskEntries);
                entity.tasks.addTask(1, new EntityAIRunAway(entity, explosive, fuse));
                System.out.println("New tasks: " + entity.tasks.taskEntries);
            }
        });
    }
}
