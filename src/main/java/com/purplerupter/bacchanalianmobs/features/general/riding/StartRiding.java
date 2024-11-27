package com.purplerupter.bacchanalianmobs.features.general.riding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.AbstractHorse;

import java.util.Random;

public class StartRiding {
    private static final Random random = new Random();

    public static final float HORSE_SPEED = 1.5F;

    public static void spawnRider(EntityLiving mob, AbstractHorse horse) {
        horse.setPosition(mob.posX, mob.posY, mob.posZ);
        horse.setGrowingAge(0); // Делаем лошадь взрослой
        horse.setHorseTamed(true); // Делаем лошадь приручённой

        // Добавляем лошадь в мир и сажаем моба на неё
        mob.world.spawnEntity(horse);
        mob.startRiding(horse, true);

        // Устанавливаем нестандартную ИИ задачу, которая будет управлять лошадью
        horse.tasks.addTask(0, new EntityAIHorseControl(horse, mob));
    }
}
