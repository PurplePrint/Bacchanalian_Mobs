package com.purplerupter.bacchanalianmobs.dynamicdifficulty.srp;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.GlobalActionDifficultyChanger.changePlayerPointsByGlobal;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;
import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.TIME_SPENT_TAG;
import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.updateTimeSpent;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class EvolutionPhaseTracker {


    public static final Map<Short, Byte> dimensionPhases = new HashMap<>();
    private static final String CONFIG_FILE_NAME = "points_per_phases.cfg";
    private static File configPath;
    private static final int defaultMinimumTime = 300; // seconds
    private static Map<Short, Integer> minimumTime = new HashMap<>();

    public EvolutionPhaseTracker(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
    }

    public static void setMinimumTime(short dimension, int amount) {
        if (minimumTime.equals(null)) { if (debug) { System.out.println("The minimumTime Map is null!"); } return; }
        if (minimumTime.containsKey(dimension)) {
            if (debug) { System.out.println("The minimumTime map already contains the minimum time amount for dimension " + dimension + ".");
            System.out.println("Can not put new key (" + amount + ") instead of the old one (" + minimumTime.get(dimension) + ")");}
            return;
        }
        minimumTime.put(dimension, amount);
    }

    public static void onPhaseChange(World world, short dimensionId, byte oldPhase, byte newPhase) {

        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            updateTimeSpent(player); // TODO надо ли???

            long timeInDimension = 0L;

            NBTTagCompound playerData = player.getEntityData();
            NBTTagCompound persistentData;

            if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
                persistentData = new NBTTagCompound();
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentData);
            } else {
                persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            }

            if (persistentData.hasKey(TIME_SPENT_TAG)) {
                NBTTagCompound timeSpentTag = persistentData.getCompoundTag(TIME_SPENT_TAG);
                for (String key : timeSpentTag.getKeySet()) {
                    short dimId = Short.parseShort(key);
                    timeInDimension = timeSpentTag.getLong(key);
                    if (dimId == dimensionId) break;
                }

                long actualMinimumTime;
                if (minimumTime.isEmpty()) {
                    System.out.println("Error! The minimum time map is empty!"); return; }
                if (minimumTime.containsKey(dimensionId)) {
                    actualMinimumTime = minimumTime.get(dimensionId) * 20; }
                else {
                    if (debug) { System.out.println("Can not find minimum time for dimension " + dimensionId + ". use the default minimum time (" + defaultMinimumTime + ") instead.");
                    System.out.println("The minimum time Map is: " + minimumTime);}
                    actualMinimumTime = defaultMinimumTime * 20;
                }
                if (timeInDimension >= actualMinimumTime) {
                    if (debug) { System.out.println("Player " + player + " spend " + timeInDimension / 20 + " seconds in the " + dimensionId + " dimension. " +
                            "It is more than " + minimumTime.get(dimensionId) + " minimum time. Difficulty points will be changed."); }
                    double points = getPointsFromConfig(dimensionId, newPhase, oldPhase);
                    changePlayerPointsByGlobal(player, points, newPhase, oldPhase);
                } else {
                    player.sendMessage(new TextComponentString("You are novice in " + dimensionId + " dimension, and you do not get points for phase " + newPhase));
                }
            } else {
                player.sendMessage(new TextComponentString("Where are your tag???"));
            }
        }
    }

    private static double getPointsFromConfig(short dimensionId, byte newPhase, byte oldPhase) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length == 4) {
                    short configDimensionId = Short.parseShort(parts[0]);
                    byte configPhaseNumber = Byte.parseByte(parts[1]);
                    double pointsAmount = Double.parseDouble(parts[2]);
                    double pointsAmountDecrease = Double.parseDouble(parts[3]);

                    if (configDimensionId == dimensionId && configPhaseNumber == newPhase) {
                        return newPhase > oldPhase ? pointsAmount : pointsAmountDecrease;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
