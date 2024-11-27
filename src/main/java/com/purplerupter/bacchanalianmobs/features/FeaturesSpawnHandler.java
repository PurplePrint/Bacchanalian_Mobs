package com.purplerupter.bacchanalianmobs.features;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.features.creeper.Breach;
import com.purplerupter.bacchanalianmobs.features.enderman.swap.AIEnderSwap;
import com.purplerupter.bacchanalianmobs.features.general.leap.LeapAI;
import com.purplerupter.bacchanalianmobs.features.spider.web.ThrowWeb;
import com.purplerupter.bacchanalianmobs.features.spider.web.WebPlacement;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.GetNearbyDifficulty.getNearbyDifficulty;
import static com.purplerupter.bacchanalianmobs.features.DefaultSettings.*;
import static com.purplerupter.bacchanalianmobs.features.FeaturesConfig.getFeaturesRule;
import static com.purplerupter.bacchanalianmobs.features.general.contusion.ExplosionHandler.addMobToContusion;
import static com.purplerupter.bacchanalianmobs.features.general.riding.StartRiding.spawnRider;

public class FeaturesSpawnHandler {

    private static List<String> processedRules = new ArrayList<>();

    private static Random random = new Random();

    public static void processFeatures(EntityLiving livingentity, String entityID) {
        boolean endLoop = false;
        while (!endLoop) {

            JsonObject featuresRule = getFeaturesRule(processedRules, entityID, livingentity);

            if (featuresRule != null) {
                if (debug) { System.out.println("features rule is not null"); }

                if (featuresRule.has("Last rule EMPTY")) {
                    if (debug) { System.out.println("This is the last rule, and it's not match"); }
                    break;
                }
                if (featuresRule.has("Last rule")) {
                    if (debug) { System.out.println("This is the last rule"); }
                    endLoop = true;
                }

                parseFeatures(livingentity, entityID, featuresRule);

                processedRules.add(featuresRule.get("Rule name").getAsString());

            } else { if (debug) { System.out.println("features rule is null! MobID is: " + entityID); } break; }
        }
        processedRules.clear();
    }

    private static void parseFeatures(EntityLiving livingentity, String entityID, JsonObject features) {
        System.out.println("Living Entity is: " + livingentity);

        if (features.has("AvoidExplosions")) {
            if (features.get("AvoidExplosions").getAsBoolean()) {
                livingentity.getEntityData().setBoolean("AvoidExplosions", true);
            }
        }

        if (features.has("Contusion")) {
            byte maxDistance = MAX_DISTANCE;
            try {
                maxDistance = features.get("Contusion").getAsJsonObject().get("MaxDistance").getAsByte();
            } catch (Exception e) { }

            int maxDuration = MAX_DURATION;
            try {
                maxDuration = features.get("Contusion").getAsJsonObject().get("MaxDuration").getAsInt();
            } catch (Exception e) { }

            float durationMultiplier = DURATION_MULTIPLIER;
            try {
                durationMultiplier = features.get("Contusion").getAsJsonObject().get("DurationMultiplier").getAsFloat();
            } catch (Exception e) { }

            addMobToContusion(entityID, durationMultiplier, maxDuration, maxDistance);
        }

        if (features.has("Leap")) {
            short maxFarDistance = MAX_FAR_DISTANCE;
            try {
                maxFarDistance = features.get("Leap").getAsJsonObject().get("MaxFarDistance").getAsShort();
            } catch (Exception e) { }

            short timeToFocus = TIME_TO_FOCUS;
            try {
                timeToFocus = features.get("Leap").getAsJsonObject().get("TimeToFocus").getAsShort();
            } catch (Exception e) { }

            short maxSightAngleToFocus = MAX_SIGHT_ANGLE_TO_FOCUS;
            try {
                maxSightAngleToFocus = features.get("Leap").getAsJsonObject().get("MaxSightAngleToFocus").getAsShort();
            } catch (Exception e) { }

            short maxMoveAngleToFocus = MAX_MOVE_ANGLE_TO_FOCUS;
            try {
                maxMoveAngleToFocus = features.get("Leap").getAsJsonObject().get("MaxMoveAngleToFocus").getAsShort();
            } catch (Exception e) { }

            livingentity.tasks.addTask(3, new LeapAI(
                    livingentity, maxFarDistance, timeToFocus, maxSightAngleToFocus, maxMoveAngleToFocus
            ));
        }

        if (features.has("Riding")) {
            System.out.println("Riding");
            byte chance = RIDING_CHANCE;
            byte basicChance = features.get("Riding").getAsJsonObject().get("chance_basic").getAsByte();
            if (features.get("Riding").getAsJsonObject().has("Difficulty_target")) {
                double difficultyTarget = features.get("Riding").getAsJsonObject().get("Difficulty_target").getAsDouble();
                double nearbyDifficultyMult = getNearbyDifficulty(livingentity, (byte)4);

                double difficultyMin = features.get("Riding").getAsJsonObject().get("Difficulty_min").getAsDouble();
                double difficultyMax = features.get("Riding").getAsJsonObject().get("Difficulty_max").getAsDouble();

                if (nearbyDifficultyMult < difficultyMin) {
                    nearbyDifficultyMult = 0;
                }
                if (nearbyDifficultyMult > difficultyMax) {
                    nearbyDifficultyMult = difficultyMax;
                }

                int temp = (int) (basicChance * (nearbyDifficultyMult / difficultyTarget) );
                chance = temp < 100 ? (byte)temp : (byte)100;
            }

            byte rnd = (byte) random.nextInt(100);
            System.out.println("Riding rnd: " + rnd + " // Riding chance: " + chance);
            if (rnd <= chance) {

                AbstractHorse horse = null;
                String horseConfig = features.get("Riding").getAsJsonObject().get("HorseType").getAsString();
                if (horseConfig.equals("Zombie")) {
                    horse = new EntityZombieHorse(livingentity.world); }
                if (horseConfig.equals("Skeleton")) {
                    horse = new EntitySkeletonHorse(livingentity.world); }
                if (horseConfig.equals("Living")) {
                    horse = new EntityHorse(livingentity.world); }

                spawnRider(livingentity, horse);
            }
        }

        if (features.has("ThrowWeb")
                && livingentity instanceof EntitySpider) { // TODO
            System.out.println("ThrowWeb");
            byte chance = THROW_WEB_CHANDE;
            try {
                chance = features.get("ThrowWeb").getAsJsonObject().get("chance").getAsByte();
            } catch (Exception e) { }

            short maxDistanceToThrow = MAX_DISTANCE_TO_THROW;
            try {
                maxDistanceToThrow = features.get("ThrowWeb").getAsJsonObject().get("maxDistanceToThrow").getAsShort();
            } catch (Exception e) { }

            float velocity = VELOCITY;
            try {
                velocity = features.get("ThrowWeb").getAsJsonObject().get("velocity").getAsFloat();
            } catch (Exception e) { }

            float inaccuracy = INACCURACY;
            try {
                inaccuracy = features.get("ThrowWeb").getAsJsonObject().get("inaccuracy").getAsFloat();
            } catch (Exception e) { }

            livingentity.tasks.addTask(3, new ThrowWeb(((EntitySpider) livingentity), livingentity.world, chance, maxDistanceToThrow, velocity, inaccuracy));
        }

        if (features.has("WebOnAttack_Attacker")
                && livingentity instanceof EntitySpider) {
            System.out.println("WebOnAttack_Attacker");

            if (features.get("WebOnAttack_Attacker").getAsBoolean()) {
                livingentity.getEntityData().setBoolean("WebOnAttack_Attacker", true);
            }
        }

        if (features.has("WebOnAttack_Victim")
                && livingentity instanceof EntitySpider) {
            System.out.println("WebOnAttack_Victim");

            if (features.get("WebOnAttack_Victim").getAsBoolean()) {
                livingentity.getEntityData().setBoolean("WebOnAttack_Victim", true);
            }
        }

        if (features.has("TempWebBehind")
                && livingentity instanceof EntitySpider) {
            System.out.println("TempWebBehind");

            byte webSearchRadius = WEB_SEARCH_RADIUS;
            try {
                webSearchRadius = features.get("TempWebBehind").getAsJsonObject().get("WebSearchRadius").getAsByte();
            } catch (Exception e) { }

            int lifeSpan = TMP_WEB_LIFESPAN;
            try {
                lifeSpan = features.get("TempWebBehind").getAsJsonObject().get("LifeSpan").getAsInt();
            } catch (Exception e) { }

            livingentity.tasks.addTask(3, new WebPlacement((EntitySpider) livingentity, webSearchRadius, lifeSpan));
        }

        if (features.has("ChargedCreeper")
                && livingentity instanceof EntityCreeper) {
            System.out.println("ChargedCreeper");

            byte chance = CHARGED_CHANCE_BASIC;
            try {
                chance = features.get("ChargedCreeper").getAsJsonObject().get("Chance_basic").getAsByte();
            } catch (Exception e) { }

            if (features.get("ChargedCreeper").getAsJsonObject().has("Difficulty_target")) {
                double nearbyDifficultyMult = getNearbyDifficulty(livingentity, (byte)4);
                double difficultyTarget = features.get("ChargedCreeper").getAsJsonObject().get("Difficulty_target").getAsDouble();

                double difficultyMin = features.get("ChargedCreeper").getAsJsonObject().get("Difficulty_min").getAsDouble();
                double difficultyMax = features.get("ChargedCreeper").getAsJsonObject().get("Difficulty_max").getAsDouble();

                if (nearbyDifficultyMult < difficultyMin) {
                    nearbyDifficultyMult = 0;
                }
                if (nearbyDifficultyMult > difficultyMax) {
                    nearbyDifficultyMult = difficultyMax;
                }

                int temp = (int) (chance * (nearbyDifficultyMult / difficultyTarget) );
                chance = temp < 100 ? (byte)temp : (byte)100;
            }

            byte rnd = (byte) random.nextInt(100);
            System.out.println("rnd: " + rnd + " // chance: " + chance);
            if (rnd <= chance) {

                EntityCreeper creeper = (EntityCreeper) livingentity;
                NBTTagCompound nbt = new NBTTagCompound();
                creeper.writeEntityToNBT(nbt);
                nbt.setBoolean("powered", true);
                creeper.readEntityFromNBT(nbt);
                System.out.println("powered!");
            }

        }

        if (features.has("Breach")
                && livingentity instanceof EntityCreeper) {
            byte maxDistanceToTarget = BREACH_MAX_DISTANCE;
            try {
                maxDistanceToTarget = features.get("Breach").getAsJsonObject().get("MaxDistanceToTarget").getAsByte();
            } catch (Exception e) { }

            livingentity.tasks.addTask(1, new Breach(livingentity, maxDistanceToTarget));
        }

        if (features.has("EnderSwap")
                && livingentity instanceof EntityEnderman) {

            short minDistance = SWAP_MIN_DISTANCE;
            try {
                minDistance = features.get("EnderSwap").getAsJsonObject().get("minDistance").getAsShort();
            } catch (Exception e) { }

            short maxDistance = SWAP_MAX_DISTANCE;
            try {
                maxDistance = features.get("EnderSwap").getAsJsonObject().get("maxDistance").getAsShort();
            } catch (Exception e) { }

            int cooldown = SWAP_COOLDOWN;
            try {
                cooldown = features.get("EnderSwap").getAsJsonObject().get("Cooldown").getAsShort();
            } catch (Exception e) { }

            boolean playSound = SWAP_SOUND;
            try {
                playSound = features.get("EnderSwap").getAsJsonObject().get("playSound").getAsBoolean();
            } catch (Exception e) { }

            List<PotionEffect> debuffs = new ArrayList<>();
            if (features.get("EnderSwap").getAsJsonObject().has("debuffEffects")) {
                for (JsonElement debuffElement : features.get("EnderSwap").getAsJsonObject().get("debuffEffects").getAsJsonArray()) {

                    PotionEffect effect;
                    if (debuffElement.getAsJsonArray().size() == 2) {
                        effect = new PotionEffect(
                                Potion.getPotionFromResourceLocation( debuffElement.getAsJsonArray().get(0).getAsString() ),
                                debuffElement.getAsJsonArray().get(1).getAsInt()
                        );
                    } else if (debuffElement.getAsJsonArray().size() == 3) {
                        effect = new PotionEffect(
                                Potion.getPotionFromResourceLocation( debuffElement.getAsJsonArray().get(0).getAsString() ),
                                debuffElement.getAsJsonArray().get(1).getAsInt(),
                                debuffElement.getAsJsonArray().get(2).getAsInt()
                        );
                    } else { effect = null; }
                    if (effect == null) {
                        if (debug) { System.out.println("Error: debuff potion effect from EnderSwap Features config is invalid! It is: " + debuffElement); }
                    }

                    debuffs.add(effect);
                }
            }

            livingentity.tasks.addTask(3, new AIEnderSwap((EntityEnderman) livingentity, minDistance, maxDistance, cooldown, playSound, debuffs));
        }

    }
}
