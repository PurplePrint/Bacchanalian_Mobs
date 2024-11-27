package com.purplerupter.bacchanalianmobs.features;

import com.google.gson.*;
import com.purplerupter.bacchanalianmobs.etc.conditions.Conditions;
import net.minecraft.entity.EntityLivingBase;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class FeaturesConfig {

    private static final String CONFIG_FILE_NAME = "Features.json";
    private static File configPath;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static JsonObject featuresConfig;
    private static final ArrayList<String> containMobsFeatures = new ArrayList<>();

    public FeaturesConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
        loadConfig();
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(configPath)) {
            if (debug) { System.out.println("try to parse..."); }
            featuresConfig = gson.fromJson(reader, JsonObject.class);
            if (debug) { System.out.println("success"); }

            parseConfig();

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void parseConfig() {
        for (Map.Entry<String, JsonElement> ruleEntry : featuresConfig.get("Rules").getAsJsonObject().entrySet()) {
            if (ruleEntry.getValue().getAsJsonObject().has("Mob list")) {
                for (JsonElement mob1 : ruleEntry.getValue().getAsJsonObject().get("Mob list").getAsJsonArray()) {

                    if (!containMobsFeatures.contains(mob1.toString())) {
                        containMobsFeatures.add(mob1.toString());
                    }
                }

            } else { System.out.println("Error: a rule from the Features config has no 'Mob list'! It is: " + ruleEntry); }
        }

        if (debug) { System.out.println("The features config file is: " + featuresConfig); }
    }

    public static JsonObject getFeaturesRule(List<String> processedRules, String mobID, EntityLivingBase entity) {
        int rulesCount = featuresConfig.getAsJsonObject("Rules").entrySet().size();
        int ruleIndex = 0;
        boolean lastRule = false;

        for (Map.Entry<String, JsonElement> entry : featuresConfig.getAsJsonObject("Rules").entrySet()) {
            if (debug) { System.out.println("'for'..."); }
            ruleIndex++;

            if (ruleIndex >= rulesCount) {
                if (debug) { System.out.println("This is the last rule from config"); }
                lastRule = true;
            }

            String ruleName = entry.getKey().toString();
            if (debug) { System.out.println("The Features Config rule name is: " + ruleName); }
            if (processedRules != null && !processedRules.isEmpty()) {
                if (processedRules.contains(ruleName)) {
                    if (debug) { System.out.println("The rule " + ruleName + " already processed. Skip it"); }
                    continue;
                }
            }

            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            JsonArray mobs = ruleObject.getAsJsonArray("Mob list");

            if (arrayContains(mobs, mobID)) {
                if (Conditions.checkConditions(ruleObject.getAsJsonObject("Conditions"), entity)) {
                    if (debug) { System.out.println("Conditions passed for a Features rule"); }

                    JsonObject features = ruleObject.getAsJsonObject("Features");

                    if (lastRule) {
                        features.addProperty("Last rule", true);
                    }
                    features.addProperty("Rule name", ruleName);

                    return features;

                } else {
                    if (debug) { System.out.println("Conditions not passed for a Features rule"); }
                }
            }
        }

        return null;
    }

}
