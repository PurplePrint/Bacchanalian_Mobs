package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class ScalingPropsEventHandler {

//    private ScalingPropsConfig config;
//    private static ArrayList<String> processedRulesScalingProps = new ArrayList<>();

    public ScalingPropsEventHandler(ScalingPropsConfig trueConfig) {
//        this.config = trueConfig;
    }

//    // TODO: Использовать только MobSpawnEventHandler для анализа производительности в миллисекундах
//    @SubscribeEvent
//    public void onMobSpawn(EntityJoinWorldEvent event) {
//        if (event.getEntity() == null || !(event.getEntity() instanceof EntityLivingBase)
//                || event.getEntity() instanceof EntityArmorStand || event.getEntity() instanceof EntityPlayerMP || event.getEntity() instanceof EntityPlayer) { return; }
//
//        if (debug) { System.out.println("====  ====  ====  ===="); System.out.println("====  ====  ====  ===="); System.out.println("EntityJoinWorldEvent event (Scaling Props)..."); }
//        if (event.getWorld().isRemote) { if (debug) { System.out.println("The event's world is remote"); } return; }
//
//        Entity entity = event.getEntity();
//        if (debug) { System.out.println(entity); }
//        String mobID = EntityList.getKey(entity).toString();
//        if (config.getRuleForMob(mobID) == null) { if (debug) { System.out.println("Don't find any rule for " + mobID + " mob"); } return; }
//
//        if (debug) { System.out.println("The mobID is: " + mobID); }
//        byte phase = getCurrentPhase(entity);
//        byte phaseIndex = (byte)(phase + 2); // +2 because SRP phases is from -2 to 10, but multipliers for phases is from 0 to 12
//        if (debug) { System.out.println("The phase is: " + phase); }
//
//        boolean endLoopScaleHealth = false;
//        while (!endLoopScaleHealth) {
//
//            JsonObject rule = config.getRuleForMob(mobID, entity, phaseIndex, processedRulesScalingProps);
//            if (debug) { System.out.println("The config rule is: " + rule); }
//
//            if (rule != null) {
//                if (debug) { System.out.println("The rule is not null"); }
//
//                if (rule.has("Last rule EMPTY")) {
//                    if (debug) { System.out.println("This is the last rule, and it's not match"); }
//                    break;
//                }
//                if (rule.has("Last rule")) {
//                    if (debug) { System.out.println("This is the last rule"); }
//                    endLoopScaleHealth = true;
//                }
//
//                double scalingFactor = 1;
//
//                if (rule.has("Simple scale")) {
//                    // По какой-то причине, множители 'Simple scale' повторно применяются при перезаходе в мир.
//                    // Это не влияет на множители по времени и сложности. Как показала отладка - множитель сложности находит '0' несмотря на наличие игрока со сложностью выше нуля поблизости.
//                    if (getNearbyPlayer(entity) != null) {
//                        float simpleScale = rule.get("Simple scale").getAsFloat();
//                        if (debug) { System.out.println("This rule has simple scale. It is: " + simpleScale); }
//                        scalingFactor *= simpleScale;
//                    }
//                }
//
//                if (rule.has("ChangeByDifficulty")) {
//                    if (debug) { System.out.println("ChangeByDifficulty..."); }
//                    JsonObject changeByDifficulty = rule.getAsJsonObject("ChangeByDifficulty");
//                    if (debug) { System.out.println("That rule is: "); System.out.println(changeByDifficulty); }
//
//                    double minDiff = changeByDifficulty.get("DifficultyMin").getAsDouble();
//                    double maxDiff = changeByDifficulty.get("DifficultyMax").getAsDouble();
//                    double targetDiff = changeByDifficulty.get("TargetDifficulty").getAsDouble();
//                    float targetMultiplier = getTargetMultiplier(changeByDifficulty, phaseIndex);
//                    if (debug) { System.out.println("Parsed: ");
//                        System.out.println(minDiff + " // " + maxDiff + " // " + targetDiff + " // " + targetMultiplier); }
//
//                    double nearbyDifficulty = getNearbyDifficulty(entity, (byte)2);
//                    if (debug) { System.out.println("The nearby difficulty is: " + nearbyDifficulty); }
//
//                    if (nearbyDifficulty >= minDiff) {
//                        if (debug) { System.out.println("The nearby difficulty is more than 'minDiff' (" + minDiff + ")"); }
//
//                        if (nearbyDifficulty > maxDiff) {
//                            if (debug) { System.out.println("The nearby difficulty is more than 'maxDiff' (" + maxDiff + ")"); }
//                            nearbyDifficulty = maxDiff;
//                        }
//
//                        if (debug) { System.out.println("Changing scalingFactor..."); }
//                        scalingFactor = (nearbyDifficulty / targetDiff) * targetMultiplier;
//                        if (debug) { System.out.println("scalingFactor is: " + scalingFactor); }
//
//                    } else { if (debug) { System.out.println("The nearby difficulty is less than 'minDiff' (" + minDiff + ")"); } }
//
//                } else { if (debug) { System.out.println("This rule has no ChangeByDifficulty"); } }
//
//                if (rule.has("ChangeByTime")) {
//                    JsonObject changeByTime = rule.getAsJsonObject("ChangeByTime");
//                    if (debug) { System.out.println("That rule is: "); System.out.println(changeByTime); }
//
//                    long minTime = changeByTime.get("TimeMin").getAsLong();
//                    long maxTime = getTimeMax(changeByTime);
//                    long targetTime = changeByTime.get("TargetTime").getAsLong();
//                    float targetMultiplier = getTargetMultiplier(changeByTime, phaseIndex);
//                    if (debug) { System.out.println("Parsed: ");
//                        System.out.println(minTime + " // " + maxTime + " // " + targetTime + " // " + targetMultiplier); }
//
//                    long nearbyTime = getNearbyTime(entity);
//
//                    if (nearbyTime >= minTime) {
//                        if (debug) { System.out.println("The time is more than 'minTime' (" + minTime + ")"); }
//
//                        if (nearbyTime > maxTime) {
//                            if (debug) { System.out.println("The time is more than 'maxTime' (" + maxTime + ")"); }
//                            nearbyTime = maxTime;
//                        }
//
//                        float scale = ((float)(nearbyTime / targetTime) * targetMultiplier);
//                        if (debug) { System.out.println("That ChangeByTime rule scale the scalingFactor of entity's health by: " + scale); }
//                        scalingFactor *= scale;
//
//                    } else { if (debug) { System.out.println("The time is less than 'minTime' (" + minTime + ")"); } }
//
//                } else { if (debug) { System.out.println("This rule has no ChangeByTime"); } }
//
//                if (debug) { System.out.println("Calling scaleHealth method..."); System.out.println("The scaling factor is: " + scalingFactor); }
//                scaleHealth((EntityLivingBase) entity, scalingFactor);
//
//                processedRulesScalingProps.add(rule.get("Rule name").getAsString());
//                if (debug) { System.out.println("The list of all processed rules is: " + processedRulesScalingProps); }
//
//            } else { if (debug) { System.out.println("The rule is null or conditions not passed!"); } }
//
//        }
//        processedRulesScalingProps.clear();
//        if (debug) { System.out.println("Processed rules list has been cleared. Now it is: " + processedRulesScalingProps); }
//
//    }

    public static void scaleHealth(EntityLivingBase entity, double multiplier) {
        if (debug) { System.out.println("scaleHealth..."); System.out.println(entity); }

        IAttributeInstance healthAttribute = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        double baseMaxHealth = healthAttribute.getBaseValue();  // Базовое значение здоровья
        if (debug) { System.out.println("Base Max Health: " + baseMaxHealth); }

        float newMaxHealth = (float)(baseMaxHealth * multiplier);
        if (debug) { System.out.println("New Max Health: " + newMaxHealth); }

        healthAttribute.setBaseValue(newMaxHealth);

        if (entity.getHealth() > newMaxHealth) {
            entity.setHealth(newMaxHealth);
        } else {
            entity.setHealth(newMaxHealth);
        }
    }

    public static long getTimeMax(JsonObject changeByTime) {
        // Получение TimeMax (либо число, либо строка "x")
        if (changeByTime.get("TimeMax").isJsonPrimitive()) {
            if (changeByTime.get("TimeMax").getAsJsonPrimitive().isNumber()) {
                return changeByTime.get("TimeMax").getAsLong();
            } else if ("x".equals(changeByTime.get("TimeMax").getAsString())) {
                return Long.MAX_VALUE;
            }
        }
        return 0;
    }

    public static float getTargetMultiplier(JsonObject ruleSection, byte index) {
        if (ruleSection.get("TargetMultiplier").isJsonArray()) {
            return ruleSection.get("TargetMultiplier").getAsJsonArray().get(index).getAsFloat();
        } else {
            return ruleSection.get("TargetMultiplier").getAsFloat();
        }
    }
}
