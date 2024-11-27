package com.purplerupter.bacchanalianmobs.dynamicdifficulty.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.ChangePoints;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.conditions.Conditions.checkConditions;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;
import static com.purplerupter.bacchanalianmobs.etc.utils.isRuleForMob.isRuleForMob;

public class PointsPerKill {


    private static File configPath;
    private static final String CONFIG_FILE_NAME = "points_per_kill.json";
    private static JsonObject configData;

    public PointsPerKill(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
        loadConfig();
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(configPath)) {
            configData = new JsonParser().parse(reader).getAsJsonObject();
            if (debug) { System.out.println("Config was loaded."); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonObject getRuleForMob(String mobId, EntityLiving entity) {
        for (Map.Entry<String, JsonElement> entry : configData.get("Rules").getAsJsonObject().entrySet()) {
            String ruleName = entry.getKey();
            if (debug) { System.out.println("The ruleName is: " + ruleName); }

            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            if (debug) { System.out.println("The ruleObject is: " + ruleObject); }

            if (arrayContains(ruleObject.getAsJsonArray("Mob List"), mobId)) {
                if (debug) { System.out.println("Match rule for this mob name: " + mobId + " // this rule called: " + ruleName); }

                if (checkConditions(ruleObject.get("Conditions").getAsJsonObject(), entity)) {
                    if (debug) { System.out.println("Conditions passed, return the rule object"); }
                    return ruleObject;
                }

            }
        }
        return null;
    }

    @SubscribeEvent
    public void onMobKilled(LivingDeathEvent event) {
        Entity source = event.getSource().getTrueSource();
        if (debug) { System.out.println("LivingDeathEvent. Entity: " + event.getEntity() + " // source: " + source); }
        if (source instanceof EntityPlayerMP &&
                (event.getEntity() instanceof EntityLiving && !(event.getEntity() instanceof EntityPlayerMP) && !(event.getEntity() instanceof EntityArmorStand))) {
            if (debug) { System.out.println("Source is EntityPlayerMP and event entity is EntityLiving"); }

            EntityLiving entity = (EntityLiving) event.getEntity();
            String mobId = EntityList.getKey(entity).toString();
            if (mobId != null) {
                if (debug) { System.out.println("mobId is: " + mobId); }

                if (isRuleForMob(configData, mobId)) {
                    JsonObject rule = getRuleForMob(mobId, entity);
                    if (rule != null) {
                        if (debug) { System.out.println("Rule is not null"); }

                        if (debug) { System.out.println("Condition passed, now get points amount..."); }
                        double pointsAmount = rule.get("Points").getAsDouble();
                        EntityPlayerMP player = (EntityPlayerMP) source;
                        if (debug) { System.out.println("Points amount is: " + pointsAmount + ", now apply it to player: " + player); }
                        ChangePoints.changeDifficultyPoints(player, pointsAmount, true);

                    }
                }

            }
        }
    }
}
