package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purplerupter.bacchanalianmobs.etc.conditions.Conditions;
import net.minecraft.entity.Entity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class ScalingPropsConfig {
    private static final String CONFIG_FILE_NAME = "scaling_props_health.json";
    private static File configPath;

//    private Map<String, JsonObject> rules = new HashMap<>();
    private static JsonObject configData;


    public ScalingPropsConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig(configPath);
        if (debug) { System.out.println("The config path is: " + configPath); }
    }

    private void loadConfig(File configFile) {
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }

        try (FileReader reader = new FileReader(configPath)) {
            configData = new JsonParser().parse(reader).getAsJsonObject();
            if (debug) { System.out.println("Config was loaded."); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getRuleForMob(String mobID) { return getRuleForMob(mobID, null, (byte)99, null); }

    public JsonObject getRuleForMob(String mobID, Entity entity, byte index, ArrayList<String> processedRules) {
        if (configData == null) {
            if (debug) { System.out.println("The rules object is null!!!"); }
            return null;
        }

        int rulesCount = configData.getAsJsonObject("Rules").entrySet().size();
        int ruleIndex = 0;
        boolean lastRule = false;

        for (Map.Entry<String, JsonElement> entry : configData.getAsJsonObject("Rules").entrySet()) {
            if (debug) { System.out.println("'for'..."); }
            ruleIndex++;
            if (debug) { System.out.println("The ruleIndex is: " + ruleIndex); }

            if (ruleIndex >= rulesCount) {
                if (debug) { System.out.println("This is the last rule from config"); }
                lastRule = true;
            }

            String ruleName = entry.getKey().toString();
            if (processedRules != null && !processedRules.isEmpty()) {
                if (processedRules.contains(ruleName)) {
                    if (debug) { System.out.println("The rule " + ruleName + " already processed. Skip it"); }
                    continue;
                }
            }

            JsonObject rule = entry.getValue().getAsJsonObject();
            if (debug) { System.out.println("The rule is: " + rule); }
            rule.addProperty("Rule name", ruleName);

            JsonArray mobs = rule.getAsJsonArray("Mob list");
            if (arrayContains(mobs, mobID)) {
                if (debug) { System.out.println("Match mobID! It's: " + mobID); }

                if (entity == null && index == 99) {
                    JsonObject fakeObject = new JsonObject();
                    fakeObject.addProperty("Return", true);
                    return fakeObject;
                }

                if (Conditions.checkConditions(rule.getAsJsonObject("Conditions"), entity)) {
                    if (debug) { System.out.println("Conditions passed for this Scaling Props rule"); }

                    if (rule.has("ChangeByDifficulty")) {
                        if (debug) { System.out.println("That rule has \"ChangeByDifficulty\"!"); }
                        JsonObject changeByDifficulty = rule.getAsJsonObject("ChangeByDifficulty");
                        JsonElement targetMultiplier = changeByDifficulty.get("TargetMultiplier");

                        if (targetMultiplier.isJsonArray()) {
                            if (debug) { System.out.println("The targetMultipliers is json array"); }
                            JsonArray multipliers = targetMultiplier.getAsJsonArray();

                            if (index >= 0 && index < multipliers.size()) {
                                changeByDifficulty.addProperty("TargetMultiplier", multipliers.get(index).getAsFloat());
                            } else {
                                if (debug) { System.out.println("Error! Index for array is invalid"); }
                            }
                        } else {
                            if (debug) { System.out.println("The targetMultipliers is not json array"); }
                        }
                    }

                    if (rule.has("ChangeByTime")) {
                        JsonObject changeByTime = rule.getAsJsonObject("ChangeByTime");
                        JsonElement targetMultiplier = changeByTime.get("TargetMultiplier");

                        if (targetMultiplier.isJsonArray()) {
                            if (debug) { System.out.println("The targetMultipliers is json array"); }
                            JsonArray multipliers = targetMultiplier.getAsJsonArray();

                            if (index >= 0 && index < multipliers.size()) {
                                changeByTime.addProperty("TargetMultiplier", multipliers.get(index).getAsFloat());
                            } else {
                                if (debug) { System.out.println("Error! Index for array is invalid"); }
                            }
                        } else {
                            if (debug) { System.out.println("The targetMultipliers is not json array"); }
                        }
                    }

//                    // Some optimizations...
//                    rule.remove("Mobs");
//                    rule.remove("Conditions");

                    if (lastRule) {
                        rule.addProperty("Last rule", true);
                    }

                    return rule;
                }
            }
        }

        if (lastRule) {
            JsonObject lastRuleObject = new JsonObject();
            lastRuleObject.addProperty("Last rule EMPTY", true);
            return lastRuleObject;
        }

        return null;
    }
}
