package com.purplerupter.bacchanalianmobs.etc.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DifficultyConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.purplerupter.bacchanalianmobs.etc.conditions.GameStageCondition.isGameStageNearby;
import static com.purplerupter.bacchanalianmobs.etc.conditions.NearbyPlayers.getNearbyPlayers;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.TIME_SPENT_TAG;
import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.updateTimeSpent;
import static com.purplerupter.bacchanalianmobs.etc.utils.GetEvolutionPhase.getCurrentPhase;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class Conditions {


    public static boolean checkConditions(JsonObject conditions, Entity entity) {
        if (debug) { System.out.println("checkConditions..."); }
        if (debug) { System.out.println(conditions); }
        for (Map.Entry<String, JsonElement> entry : conditions.entrySet()) {
            if (debug) { System.out.println("Checking conditions for a one of groups..."); }
            JsonObject conditionSet = entry.getValue().getAsJsonObject();

            if (!checkRandom(conditionSet)) continue;
            if (!checkDimensions(conditionSet, entity)) continue;
            if (!checkBiomes(conditionSet, entity)) continue;
            if (!checkYRange(conditionSet, entity)) continue;

            if (BacchanalianMobs.srParasitesIntegration) { if (!checkPhase(conditionSet, entity)) continue; }

            if (BacchanalianMobs.gameStagesIntegration) { if (!checkStages(conditionSet, entity)) continue; }

            if (!checkDifficulty(conditionSet, entity)) continue;
            if (!checkTime(conditionSet, entity)) continue;

            if (debug) { System.out.println("All conditions return true or not processed!"); }
            return true;
        }
        if (debug) { System.out.println("All conditions return false. Conditions check failed!"); }
        return false;
    }

    private static boolean checkRandom(JsonObject conditionSet) {
        if (debug) { System.out.println("checkRandom..."); }
        if (conditionSet.has("chance")) {
            if (debug) { System.out.println("That condition group has 'chance'!"); }
            Random random = new Random();
            int chance = conditionSet.get("chance").getAsInt();
            if (debug) { System.out.println("The chance from config is: " + chance); }
            int randomNumber = random.nextInt(100);
            if (debug) { System.out.println("A random number from 0 to 100 is: " + randomNumber); }
            return (randomNumber < chance);
        }
        return true;
    }

    private static boolean checkDimensions(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkDimensions..."); }
        if (conditionSet.has("Dimensions")) {
            if (debug) { System.out.println("That condition group has Dimensions!"); }

            try {
                if (conditionSet.get("Dimensions").getAsString().equals("ALL")) {
                    if (debug) { System.out.println("That condition applies to all dimensions"); } return true;
                } } catch (Exception e) {}

            JsonArray dimensionIDs;
            try {
                dimensionIDs = conditionSet.get("Dimensions").getAsJsonArray();
            } catch (Exception e) {
                if (debug) { System.out.println("Error! The 'Dimensions' is not json array. It is " + conditionSet.get("Dimensions")); }
                e.printStackTrace();
                return false;
            }

            short currentDimension = (short) entity.getEntityWorld().provider.getDimension();
            if (debug) { System.out.println("The entity dimension is: " + currentDimension + ", the Dimensions from config is: " + dimensionIDs); }


            boolean arrayContains = arrayContains(dimensionIDs, currentDimension);
            if (arrayContains) {
                if (debug) { System.out.println("The entity " + entity + " dimension is " + currentDimension + ", and it equals to one of dimension from the 'Dimensions' condition!"); }
                return true;
            }

            if (debug) { System.out.println("The entity " + entity + " dimension is " + currentDimension + ", and the 'Dimensions' list does not match.");
                System.out.println("Dimension condition failed!"); }
            return false;
        }
        return true;
    }

    private static boolean checkBiomes(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkBiomes..."); }
        if (conditionSet.has("Biomes")) {
            if (debug) { System.out.println("That condition group has 'Biomes'!"); }

            try {
                if (conditionSet.get("Biomes").getAsString().equals("ALL")) {
                    if (debug) { System.out.println("That condition applies to all biomes"); } return true;
            } } catch (Exception e) {}

            JsonArray biomeIDs;
            try {
                biomeIDs = conditionSet.get("Biomes").getAsJsonArray();
            } catch (Exception e) {
                if (debug) { System.out.println("Error! The biomeIDs is not json array. It is " + conditionSet.get("Biomes")); }
                e.printStackTrace();
                return false;
            }

            BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            String currentBiome = entity.world.getBiome(pos).getRegistryName().toString();
            if (debug) { System.out.println("The entity biome is: " + currentBiome + ", the biomeIDs from config is: " + biomeIDs); }
            boolean arrayContains = arrayContains(biomeIDs, currentBiome);
            if (arrayContains) {
                if (debug) { System.out.println("The entity " + entity + " biome is " + currentBiome + ", and it equals to one of biomes from the 'Biomes' condition!"); }
                return true;
            }

            if (debug) { System.out.println("The entity " + entity + " biome is " + currentBiome + ", and the biomeIDs list does not match.");
            System.out.println("Biome condition failed!"); }
            return false;
        }
        return true;
    }

    private static boolean checkYRange(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkYRange..."); }
        if (conditionSet.has("yRange")) {
            if (debug) { System.out.println("That condition group has yRange!"); }
            JsonArray yRange = conditionSet.get("yRange").getAsJsonArray();
            short yMin = 0;
            short yMax = 255;
            try {
                yMin = yRange.get(0).getAsShort();
                if (debug) { System.out.println("The first (minimum) Y coordinate in that Y Range condition successfully parsed as a short number"); }
            } catch (NumberFormatException e1) {
                if (debug) { System.out.println("The first (minimum) Y coordinate in that Y Range condition is not a number. Try to parse it as 'x' (minimum value)..."); }
                try {
                    if (yRange.get(0).getAsString().equals("x")) {
                        if (debug) { System.out.println("Apply the minimum value (0) to the first Y coordinate in that Y Range"); }
                        yMin = 0;
                    } else {
                        if (debug) { System.out.println("Wrong config! The first (minimum) Y coordinate in that Y Range condition is not a number and not the 'x' (minimum value)!"); }
                    }
                } catch (Exception e2) {
                    if (debug) { System.out.println("The first (minimum) Y coordinate in that Y Range is not a String. Error!"); }
                }
            }
            try {
                yMax = yRange.get(1).getAsShort();
                if (debug) { System.out.println("The second (maximum) Y coordinate in that Y Range condition successfully parsed as a short number"); }
            } catch (NumberFormatException e1) {
                if (debug) { System.out.println("The second (maximum) Y coordinate in that Y Range condition is not a number. Try to parse it as 'x' (maximum value)..."); }
                try {
                    if (yRange.get(1).getAsString().equals("x")) {
                        if (debug) { System.out.println("Apply the maximum value (255) to the second Y coordinate in that Y Range"); }
                        yMax = 255;
                    } else {
                        if (debug) { System.out.println("Wrong config! The second (maximum) Y coordinate in that Y Range condition is not a number and not the 'x' (maximum value)!"); }
                    }
                } catch (Exception e2) {
                    if (debug) { System.out.println("The second (maximum) Y coordinate in that Y Range is not a String. Error!"); }
                }
            }
            if (debug) { System.out.println("The yMin is: " + yMin + ", the yMax is: " + yMax); }
            if (debug) { System.out.println("Y coordinate of the entity is: " + entity.posY); }
            if (entity.posY > 255) {
                if (debug) { System.out.println("Achtung! The entity is out of the build limit: Y coordinate is " + entity.posY + " and it's above 255!"); }
                if (debug) { System.out.println("Any Y Range condition is always FALSE for such huge Y coordinate!"); }
            }
            return entity.posY >= yMin && entity.posY <= yMax;
        }
        return true;
    }

    private static boolean checkPhase(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkPhase..."); }
        if (conditionSet.has("Phases")) {
            if (debug) { System.out.println("That condition group has phase!"); }

            if (conditionSet.get("Phases").getAsString().equals("ALL")) {
                if (debug) { System.out.println("That condition applies to all phases."); }
                return true;
            }

            JsonArray phases;
            try {
                phases = conditionSet.get("Phases").getAsJsonArray();
            } catch (Exception e) {
                if (debug) { System.out.println("Error! The Phases is not json array. It is " + conditionSet.get("Phases")); } e.printStackTrace(); return false;
            }

            byte phase = getCurrentPhase(entity);
            boolean arrayContains = arrayContains(phases, phase);
            if (arrayContains) {
                if (debug) { System.out.println("The evolution phase in entity dimension is: " + phase + ", and it's match for one of phase from 'Phases' condition"); }
                return true;
            }
        }
        if (debug) { System.out.println("That condition group has no Phase condition. Phase condition return true!"); }
        return true;
    }

    private static boolean checkStages(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkStages..."); }
        if (conditionSet.has("stages")) {
            if (debug) { System.out.println("That condition group has stages!"); }
            JsonArray gameStagesArray = conditionSet.getAsJsonArray("stages");

            List<String> hasStages = new ArrayList<>();
            List<String> hasNotStages = new ArrayList<>();
            for (int i = 0; i < gameStagesArray.size(); i++) {
                String stage = gameStagesArray.get(i).getAsString();
                if (debug) { System.out.println("stage " + stage + "..."); }

                if (stage.startsWith("+")) {
                    hasStages.add(stage.substring(1));
//                    if (debug) { System.out.println("must have"); }
                } else if (stage.startsWith("-")) {
                    hasNotStages.add(stage.substring(1));
//                    if (debug) { System.out.println("must have not"); }
                }
            }
            if (debug) { System.out.println("Player must have stages: " + hasStages); }
            if (debug) { System.out.println("Player must have not stages: " + hasNotStages); }

            for (String stage : hasNotStages) {
                if (isGameStageNearby(entity, stage)) {
                    if (debug) { System.out.println("A player in the small radius (server-side render distance) near the entity have a stage " + stage + " that (s)he should not have"); }
                    if (debug) { System.out.println("Stage condition failed"); }
                    return false;
                }
            }
            boolean hasStagesBoolean = isGameStageNearby(entity, hasStages);
            if (hasStagesBoolean) {
                if (debug) { System.out.println("A player in the small radius (server-side render distance) near the entity have all stages that (s)he should have"); }
                if (debug) { System.out.println("Stage condition passed!"); }
            } else {
                if (debug) { System.out.println("A player in the small radius (server-side render distance) near the entity have not one of stages that (s)he should have"); }
            }
            return hasStagesBoolean;
        }
        if (debug) { System.out.println("That condition group has no game stages. Stages condition return true!"); }
        return true;
    }

    private static boolean checkTime(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkTime..."); }
        return bigMethod("time", conditionSet, entity);
    }

    private static boolean checkDifficulty(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkDifficulty..."); }
        return bigMethod("difficulty", conditionSet, entity);
    }

    private static boolean bigMethod(String type, JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("Called a big method for check difficulty or time condition..."); }
        if (conditionSet.has(type)) {
            if (debug) { System.out.println("That condition group has " + type + "!"); }
            boolean validTypeConfig = true;  // Флаг для проверки корректности данных

            double typeDataMin = 0;
            double typeDataMax;
            if (type.equals("difficulty")) {
                if (debug) { System.out.println("The 'type' equals " + type + "!"); }
                typeDataMax = DifficultyConfig.pointsLimit;
            } else if (type.equals("time")) {
                if (debug) { System.out.println("The 'type' equals " + type + "!"); }
                typeDataMax = Long.MAX_VALUE;
            } else {
                if (debug) { System.out.println("Error! The bigMethod call is not the difficulty and not the time! It's: " + type); }
                typeDataMax = 0;
                return false;
            }

            JsonArray typeDataRange = conditionSet.getAsJsonArray(type);
            if (typeDataRange.size() != 2) {
                if (debug) { System.out.println("Config file is wrong! The '" + type + "' condition is wrong, array size is not 2, it's not a range!"); }
                return false;
            }

            // The minimum value in a range
            if (typeDataRange.get(0).isJsonPrimitive()) {
                JsonPrimitive primitiveMin = typeDataRange.get(0).getAsJsonPrimitive();
                if (primitiveMin.isNumber()) {
                    typeDataMin = (type.equals("difficulty") ? primitiveMin.getAsDouble() : primitiveMin.getAsLong());
                    if (debug) { System.out.println("The minimum " + type + " value is: " + typeDataMin); }
                } else if (primitiveMin.isString()) {
                    String minValue = primitiveMin.getAsString();
                    if (minValue.equals("x")) {
                        typeDataMin = 0;
                        if (debug) { System.out.println("The first element in that " + type + " array is 'x', setting minimum value to 0."); }
                    } else {
                        if (debug) { System.out.println("Error! That " + type + " array in that condition is wrong! It's not a number and not the 'x' (the minimum value)! It's: " + primitiveMin); }
                        validTypeConfig = false;
                    }
                }
            } else {
                if (debug) { System.out.println("Error! " + typeDataRange.get(0) + ", the first element in that " + type + " array is not a primitive type."); }
                validTypeConfig = false;
            }

            // The maximum value in a range
            if (typeDataRange.get(1).isJsonPrimitive()) {
                JsonPrimitive primitiveMax = typeDataRange.get(1).getAsJsonPrimitive();
                if (primitiveMax.isNumber()) {
                    typeDataMax = (type.equals("difficulty") ? primitiveMax.getAsDouble() : primitiveMax.getAsLong());
                    if (debug) { System.out.println("The maximum " + type + " value is " + typeDataMax); }
                } else if (primitiveMax.isString()) {
                    String maxValue = primitiveMax.getAsString();
                    if (maxValue.equals("x")) {
                        if (debug) { System.out.println("The second element in that " + type + " array is 'x', setting maximum value to " + typeDataMax + "."); }
                    } else {
                        if (debug) { System.out.println("Error! That " + type + " array in that condition is wrong! It's not a number and not the 'x' (the maximum value)!"); }
                        validTypeConfig = false;
                    }
                }
            } else {
                if (debug) { System.out.println("Error! " + typeDataRange.get(1) + ", the second element in that " + type + " array is not a primitive type."); }
                validTypeConfig = false;
            }

            if (!validTypeConfig) {
                if (debug) { System.out.println("Invalid " + type + " values. Exiting check."); }
                return false;
            }

            // Check nearby players
            List<EntityPlayerMP> nearbyPlayers = getNearbyPlayers(entity);
            Object playerTypeDataAmount = 0;
            for (EntityPlayerMP player : nearbyPlayers) {
                NBTTagCompound playerData = player.getEntityData();
                NBTTagCompound persistentData;

                if (playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
                    persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
//                    playerTypeDataAmount = persistentData.getDouble("PlayerDifficultyPoints");
                    if (type.equals("difficulty")) {
                        if (debug) { System.out.println("Try to get data from the NBT for the PlayerDifficultyPoints tag..."); }
                        playerTypeDataAmount = persistentData.getDouble("PlayerDifficultyPoints");
                        if (debug) { System.out.println("The amount of player's difficulty points is: " + playerTypeDataAmount); }
                    } else if (type.equals("time")) {
                        if (debug) { System.out.println("Try to get data from the NBT for the " + TIME_SPENT_TAG + " tag for the current dimension..."); }

                        updateTimeSpent(player);
                        NBTTagCompound timeSpentTag = persistentData.getCompoundTag(TIME_SPENT_TAG);

                        for (String key : timeSpentTag.getKeySet()) {
                            if (debug) { System.out.println("The key is: " + key + " // The value is: " + timeSpentTag.getLong(key)); }
                            if (debug) { System.out.println("The player " + player + " // " + player.getName() + " is in dimension: " + player.dimension); }
                            short dimId = Short.parseShort(key);
                            if (dimId == player.dimension) {
                                if (debug) { System.out.println("The player's dimension equals to a one of dimensions from NBT!"); }
                                playerTypeDataAmount = timeSpentTag.getLong(key);
                                if (debug) { System.out.println("The player " + player.getName() + " spent " + playerTypeDataAmount + " ticks in dimension " + dimId); }
                            }
                            if (debug) { System.out.println("The 'for' block of code is end"); }
                        }
                    }
                    if (debug) { System.out.println("Player " + player.getName() + " has " + type + " amount: " + playerTypeDataAmount); }

                    if ((type.equals("difficulty") ? (double)playerTypeDataAmount : (long)playerTypeDataAmount) >= (double)typeDataMin
                            && (type.equals("difficulty") ? (double)playerTypeDataAmount : (long)playerTypeDataAmount) <= (double)typeDataMax) {
                        if (debug) { System.out.println(type + " condition passed, return true!"); }
                        return true;
                    } else {
                        if (debug) { System.out.println("Condition not passed: the 'playerTypeDataAmount' is: " + playerTypeDataAmount + ", it's not in the range between minimum " + typeDataMin + " and maximum " + typeDataMax); }
                    }
                } else {
                    if (debug) { System.out.println("Error!!! Player " + player.getName() + " does not have " + (type.equals("difficulty") ? "DifficultyPoints" : "TimeSpentInDimensions") + " tag in NBT!!!"); }
                }
            }
            return false;
        } else {
            if (debug) { System.out.println("That condition group has no " + type + " condition."); }
            return true;
        }
    }
}
