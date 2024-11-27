package com.purplerupter.bacchanalianmobs.etc.utils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class DimensionTimeTracker {

    public static final String TIME_SPENT_TAG = "TimeSpentInDimensions";
    public static final String LAST_ENTERED_TAG = "LastEnteredDimensions";

    public static void updatePlayerTime(EntityPlayerMP player, short amount) {
        NBTTagCompound persistentData = getOrCreatePersistentData(player);
        Map<Short, Long> timeSpent = getTimeSpentMap(persistentData);
        short dimension = (short) player.dimension;

//        System.out.println("The amount is: " + amount);
        timeSpent.put(dimension, timeSpent.getOrDefault(dimension, 0L) + amount);

        persistentData.setTag(TIME_SPENT_TAG, serializeMap(timeSpent));
    }

    public static void updateTimeSpent(EntityPlayerMP player) {
        updateTimeSpent(player, (short)player.dimension);
    }

    public static void updateTimeSpent(EntityPlayerMP player, short dimensionId) {
        NBTTagCompound persistentData = getOrCreatePersistentData(player);
        Map<Short, Long> timeSpent = getTimeSpentMap(persistentData);
        Map<Short, Long> lastEntered = getLastEnteredMap(persistentData);

        short dimId = dimensionId;
        long currentTime = player.world.getTotalWorldTime();

        if (lastEntered.containsKey(dimId)) {
            long timeInDimension = currentTime - lastEntered.get(dimId);
            timeSpent.put(dimId, timeSpent.getOrDefault(dimId, 0L) + timeInDimension);
        }

        persistentData.setTag(TIME_SPENT_TAG, serializeMap(timeSpent));
        player.getEntityData().setTag(EntityPlayerMP.PERSISTED_NBT_TAG, persistentData);
    }

    private static NBTTagCompound getOrCreatePersistentData(EntityPlayerMP player) {
        NBTTagCompound playerData = player.getEntityData();
        if (!playerData.hasKey(EntityPlayerMP.PERSISTED_NBT_TAG)) {
            NBTTagCompound persistentData = new NBTTagCompound();
            playerData.setTag(EntityPlayerMP.PERSISTED_NBT_TAG, persistentData);
            return persistentData;
        } else {
            return playerData.getCompoundTag(EntityPlayerMP.PERSISTED_NBT_TAG);
        }
    }

    private static Map<Short, Long> getTimeSpentMap(NBTTagCompound persistentData) {
        Map<Short, Long> timeSpent = new HashMap<>();
        if (persistentData.hasKey(TIME_SPENT_TAG)) {
            NBTTagCompound timeSpentTag = persistentData.getCompoundTag(TIME_SPENT_TAG);
            for (String key : timeSpentTag.getKeySet()) {
                short dimensionId = Short.parseShort(key);
                long time = timeSpentTag.getLong(key);
                timeSpent.put(dimensionId, time);
            }
        }
        return timeSpent;
    }

    private static Map<Short, Long> getLastEnteredMap(NBTTagCompound persistentData) {
        Map<Short, Long> lastEntered = new HashMap<>();
        if (persistentData.hasKey(LAST_ENTERED_TAG)) {
            NBTTagCompound lastEnteredTag = persistentData.getCompoundTag(LAST_ENTERED_TAG);
            for (String key : lastEnteredTag.getKeySet()) {
                short dimensionId = Short.parseShort(key);
                long time = lastEnteredTag.getLong(key);
                lastEntered.put(dimensionId, time);
            }
        }
        return lastEntered;
    }

    private static NBTTagCompound serializeMap(Map<Short, Long> map) {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<Short, Long> entry : map.entrySet()) {
            tag.setLong(entry.getKey().toString(), entry.getValue());
        }
        return tag;
    }

    public static long getTimeInDimension(EntityPlayerMP player) {
        short playerDimension = (short)player.dimension;
        NBTTagCompound persistentData = getOrCreatePersistentData(player);
        updateTimeSpent(player);
        NBTTagCompound timeSpentTag = persistentData.getCompoundTag(TIME_SPENT_TAG);

        long dimensionTimeAmount = 0;

        for (String key : timeSpentTag.getKeySet()) {
            if (debug) { System.out.println("The key is: " + key + " // The value is: " + timeSpentTag.getLong(key)); }
            if (debug) { System.out.println("The player " + player + " // " + player.getName() + " is in dimension: " + playerDimension); }
            short dimId = Short.parseShort(key);
            if (dimId == playerDimension) {
                if (debug) { System.out.println("The player's dimension equals to a one of dimensions from NBT!"); }
                dimensionTimeAmount = timeSpentTag.getLong(key);
                if (debug) { System.out.println("The player " + player.getName() + " spent " + dimensionTimeAmount + " ticks in dimension " + dimId); }
            }
            if (debug) { System.out.println("The 'for' block of code is end"); }
        }

        return dimensionTimeAmount;
    }
}
