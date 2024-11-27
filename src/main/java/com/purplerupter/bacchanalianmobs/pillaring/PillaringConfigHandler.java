package com.purplerupter.bacchanalianmobs.pillaring;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purplerupter.bacchanalianmobs.etc.conditions.Conditions;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class PillaringConfigHandler {
    private static final String CONFIG_FILE_NAME = "Pillaring.json";
    private static File configPath;
    private static JsonObject configData;

    public PillaringConfigHandler(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
        loadConfig();
    }

    private static void loadConfig() {
        try (FileReader reader = new FileReader(configPath)) {
            configData = new JsonParser().parse(reader).getAsJsonObject();
            if (debug) { System.out.println("Config was loaded."); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonObject getPillaringRule(EntityLiving entity) {
        String mobId = EntityList.getKey(entity).toString();
        for (Map.Entry<String, JsonElement> entry : configData.entrySet()) {
            String ruleName = entry.getKey();
            if (debug) { System.out.println("The ruleName is: " + ruleName); }
            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            if (debug) { System.out.println("The ruleObject is: " + ruleObject); }

            if (arrayContains(ruleObject.getAsJsonArray("Mobs"), mobId)) {
                if (debug) { System.out.println("Match rule for this mobID: " + mobId + " // this rule called: " + ruleName); }
                if (Conditions.checkConditions(ruleObject.getAsJsonObject("Conditions"), entity)) {
                    if (debug) { System.out.println("Conditions passed, return the rule object"); }

                    boolean allowPillaring = ruleObject.get("Pillaring").getAsBoolean();
                    if (allowPillaring) {
                        JsonObject pillaringConfig = new JsonObject();
//                        pillaringConfig.addProperty("Pillaring", allowPillaring);
                        pillaringConfig.addProperty("Block", ruleObject.get("Pillaring block").getAsString());
                        return pillaringConfig;
                    }
                }
            }
        }

        return null;
    }

    public static String getPillaringBlock(EntityLiving entity) {
        return getPillaringRule(entity).get("Block").getAsString();
    }
}
