package com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.conditions.NearbyPlayers.getNearbyPlayers;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.MultiplayerDifficultyModeConfig.*;
import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.getTimeInDimension;

public class GetNearbyDifficulty {


//    public static double getNearbyDifficulty(Entity entity, double difficultyMin, double difficultyMax) {
    public static double getNearbyDifficulty(Entity entity, byte mode) {
        List<EntityPlayerMP> nearbyPlayers = getNearbyPlayers(entity);
        List<Double> nearbyPlayersDifficultyList = new ArrayList<>();
        for (EntityPlayerMP player : nearbyPlayers) {
            NBTTagCompound playerData = player.getEntityData();
            NBTTagCompound persistentData;
            if (playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
                persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
                double playerDifficultyAmount = persistentData.getDouble("PlayerDifficultyPoints");
                if (debug) { System.out.println("Player: " + player + " // Difficulty points: " + playerDifficultyAmount); }

                nearbyPlayersDifficultyList.add(playerDifficultyAmount);
            } else {
                if (debug) { System.out.println("Error! Player " + player + " does not have difficulty tag in NBT!"); }
            }
        }

        if (nearbyPlayersDifficultyList.isEmpty() || nearbyPlayersDifficultyList == null) {
            if (debug) { System.out.println("The nearbyPlayersDifficultyList is empty or null. It will be replaced with default value (0)"); }
            nearbyPlayersDifficultyList.add(0D);
        }

        if (debug) { System.out.println("The nearbyPlayersDifficultyList is: " + nearbyPlayersDifficultyList); }

        String nearbyMode;
        switch (mode) {
            case 1:
                nearbyMode = equipmentDifficultyMode; break;
            case 2:
                nearbyMode = scalingPropsHealthDifficultyMode; break;
//            case 3: // TODO
//                nearbyMode = scalingPropsHealthTimeMode; break;
            case 4: // Features config mode
                nearbyMode = featuresScaleDifficultyMode; break;
            default:
                if (debug) { System.out.println("Error! nearbyMode not specified"); } nearbyMode = null; break;
        }

        if (nearbyMode != null) {
            double returnValue;
            switch (nearbyMode) {
                case "MAX":
                    returnValue = Collections.max(nearbyPlayersDifficultyList);
                    if (debug) { System.out.println("The 'MAX' difficulty selection mode chosen. Return this amount of points: " + returnValue); }
                    return returnValue;
                case "MIN":
                    returnValue = Collections.min(nearbyPlayersDifficultyList);
                    if (debug) { System.out.println("The 'MIN' difficulty selection mode chosen. Return this amount of points: " + returnValue); }
                    return returnValue;
                case "RANDOM":
                    Random random = new Random();
                    returnValue = nearbyPlayersDifficultyList.get(random.nextInt(nearbyPlayersDifficultyList.size()));
                    if (debug) { System.out.println("The 'RANDOM' difficulty selection mode chosen. Return this amount of points: " + returnValue); }
                    return returnValue;
                case "AVERAGE":
                    int size = nearbyPlayersDifficultyList.size();
                    double sum = 0;
                    for (double value : nearbyPlayersDifficultyList) {
                        sum += value;
                    }
                    returnValue = sum / (double)size;
                    if (debug) { System.out.println("The 'AVERAGE' difficulty selection mode chosen. Return this amount of points: " + returnValue); }
                    return returnValue;
                default:
                    if (debug) { System.out.println("Error! The nearby mode is invalid. It's: " + equipmentDifficultyMode + ". Valid values: " +
                            "\"MAX\", \"MIN\", \"RANDOM\", \"AVERAGE\"."); }
                    return 0;
            }
        } else { System.out.println("nearbyMode is null"); return 0; }
    }

    public static long getNearbyTime(Entity entity) {
        List<EntityPlayerMP> nearbyPlayers = getNearbyPlayers(entity);
        List<Long> nearbyPlayersTimeList = new ArrayList<>();
        long returnValue;

        if (!nearbyPlayers.isEmpty() && nearbyPlayers != null) { // anti-crash
            for (EntityPlayerMP player : nearbyPlayers) {
                if (debug) { System.out.println("A player: " + player); }
                long time = getTimeInDimension(player);
                if (debug) { System.out.println("Time amount for this player: " + time); }

                nearbyPlayersTimeList.add(time);
            }
        } else { if (debug) { System.out.println("The list of nearby players is empty or null! Return '0'"); } return 0; }

        if (debug) { System.out.println(nearbyPlayersTimeList); }
        switch (scalingPropsHealthTimeMode) {
            case "MAX":
                returnValue = Collections.max(nearbyPlayersTimeList);
                if (debug) { System.out.println("The 'MAX' time selection mode chosen. Return this amount of time: " + returnValue); }
                return returnValue;
            case "MIN":
                returnValue = Collections.min(nearbyPlayersTimeList);
                if (debug) { System.out.println("The 'MIN' time selection mode chosen. Return this amount of time: " + returnValue); }
                return returnValue;
            case "RANDOM":
                Random random = new Random();
                returnValue = nearbyPlayersTimeList.get(random.nextInt(nearbyPlayersTimeList.size()));
                if (debug) { System.out.println("The 'RANDOM' time selection mode chosen. Return this amount of time: " + returnValue); }
                return returnValue;
            case "AVERAGE":
                int size = nearbyPlayersTimeList.size();
//                double sum = 0;
                BigInteger sum = BigInteger.valueOf(0);
                for (long value : nearbyPlayersTimeList) {
                    sum.add(BigInteger.valueOf(value));
                }
                returnValue = sum.divide(BigInteger.valueOf(size)).longValue();
                if (debug) { System.out.println("The 'AVERAGE' time selection mode chosen. Return this amount of time: " + returnValue); }
                return returnValue;
            default:
                if (debug) { System.out.println("Error! The nearby mode is invalid. It's: " + equipmentDifficultyMode + ". Valid values: " +
                        "\"MAX\", \"MIN\", \"RANDOM\", \"AVERAGE\"."); }
                return 0;
        }
    }
}
