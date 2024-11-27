package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.AddDistanceToNBT.DISTANCES_TAG;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.AddDistanceToNBT.WIDTH_TAG;
import static com.purplerupter.bacchanalianmobs.etc.events.HandleEntityAttack.recentEntitiesCount;
import static com.purplerupter.bacchanalianmobs.etc.utils.TagNBT.getOrCreatePersistentData;

// Оружие Techguns наносит разный урон на разной дальности. Это нужно учитывать при подсчёте DPS.
// Однако, очень сложно определить наверняка, на каком расстоянии от игрока будут враги...
// С некоторой точностью можно использовать биом, измерение и Y-координаты. Типа, в пещерах дальность стрельбы будет маленькой, в горах большой...
// Но такие расчёты не будут учитывать сложные особенности местности (огромный данж в небе над горным биомом, или кратер от взрыва доходящий до бедрока).

// Верным подходом будет хранить в NBT игрока расстояние до мобов, недавно атакуемых игроком.
public class RangeAndWidth {
    public static float getAverageFiringRange(EntityPlayerMP player) {
        NBTTagCompound persistentData = getOrCreatePersistentData(player);
        float[] distances = getDistances(persistentData);
        if (debug) { System.out.println("The recent distances array is: " + Arrays.toString(distances)); }

        byte entitiesCount = 0;
        float sum = 0F;
        float average = 0F;
        for (byte i = 0; i < distances.length; i++) {
            if (distances[i] > 0) { entitiesCount++; }
            sum += distances[i];
        }
        if (sum > 0) {
            average = (sum / entitiesCount);
        }

        return average;
    }

    public static float getAverageMobWidth(EntityPlayerMP player) {
        NBTTagCompound persistentData = getOrCreatePersistentData(player);
        float[] width = getWidth(persistentData);
        if (debug) { System.out.println("The recent mob width array is: " + Arrays.toString(width)); }

        byte entitiesCount = 0;
        float sum = 0F;
        float average = 0F;
        for (byte i = 0; i < width.length; i++) {
            if (width[i] > 0) { entitiesCount++; }
            sum += width[i];
        }
        if (sum > 0) {
            average = (sum / entitiesCount);
        }

        return average;
    }

    private static float[] getDistances(NBTTagCompound persistentData) {
        float[] distances = new float[recentEntitiesCount];
        if (persistentData.hasKey(DISTANCES_TAG)) {
            NBTTagCompound distancesTag = persistentData.getCompoundTag(DISTANCES_TAG);
            for (String key : distancesTag.getKeySet()) {
                distances[(Byte.parseByte(key) - 1)] = distancesTag.getFloat(key);
            }
        }
        return distances;
    }

    private static float[] getWidth(NBTTagCompound persistentData) {
        float[] width = new float[recentEntitiesCount];
        if (persistentData.hasKey(WIDTH_TAG)) {
            NBTTagCompound widthTag = persistentData.getCompoundTag(WIDTH_TAG);
            for (String key : widthTag.getKeySet()) {
                width[(Byte.parseByte(key) - 1)] = widthTag.getFloat(key);
            }
        }
        return width;
    }
}
