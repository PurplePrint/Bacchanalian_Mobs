package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.etc.conditions.Conditions;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;
import static com.purplerupter.bacchanalianmobs.etc.utils.GetEvolutionPhase.getCurrentPhase;

public class PotionEffectsConfig {

    private static final String POTION_EFFECTS_FILE_NAME = "potion_effects.json";
    private static File potionEffectsConfigPath;
    private static JsonObject configData;
    private static Gson gson;

    public PotionEffectsConfig(File configDir) {
        potionEffectsConfigPath = new File(configDir, POTION_EFFECTS_FILE_NAME);
        gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfig();
    }

    private static void loadConfig() {
        if (!potionEffectsConfigPath.exists()) {
            createDefaultConfig(POTION_EFFECTS_FILE_NAME, potionEffectsConfigPath);
        }
        try (FileReader reader = new FileReader(potionEffectsConfigPath)) {
            if (debug) { System.out.println("try to parse..."); }
            configData = gson.fromJson(reader, JsonObject.class);
            if (debug) { System.out.println("success"); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JsonObject getEffectsRule(List<String> processedRules, EntityLiving entity) {
        byte index = (byte) (getCurrentPhase(entity) + 2);
        String mobID = EntityList.getKey(entity).toString();

        int rulesCount = configData.getAsJsonObject("Rules").entrySet().size();
        int ruleIndex = 0;
        boolean lastRule = false;

        for (Map.Entry<String, JsonElement> entry : configData.getAsJsonObject("Rules").entrySet()) {
            if (debug) { System.out.println("'for'..."); }
            ruleIndex++;
            if (debug) { System.out.println("that rule element is: " + entry); }

            if (ruleIndex >= rulesCount) {
                if (debug) { System.out.println("This is the last rule from config"); }
                lastRule = true;
            }

            String ruleName = entry.getKey().toString();
            if (debug) { System.out.println("The Effects rule name is: " + ruleName); }
            if (processedRules != null && !processedRules.isEmpty()) {
                if (processedRules.contains(ruleName)) {
                    if (debug) { System.out.println("The rule " + ruleName + " already processed. Skip it"); }
                    continue;
                }
            }

            JsonObject rule = entry.getValue().getAsJsonObject();
            if (arrayContains(rule.get("Mob list").getAsJsonArray(), mobID)) {
                if (debug) { System.out.println("Matches MobID!"); }
                if (Conditions.checkConditions(rule.getAsJsonObject("Conditions"), entity)) {

                    if (lastRule) {
                        rule.addProperty("Last rule", true);
                    }
                    rule.addProperty("Rule name", ruleName);

                    return processRule(rule, index);
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

    private static JsonObject processRule(JsonObject rule, byte index) {
        JsonObject processedRule = new JsonObject();
        boolean addName = false;
        String name = "DEFAULT";
        if (rule.has("Rule name")) {
            addName = true;
            name = rule.get("Rule name").getAsString();
        }
        if (rule.has("Effects")) {
            rule = rule.get("Effects").getAsJsonObject();
        }
        if (debug) { System.out.println(rule); }
        processedRule = processByIndex(rule, index);
        if (addName) {
            processedRule.addProperty("Rule name", name);
        }

        return processedRule;
    }

    private static JsonObject processByIndex(JsonObject rule, byte index) {
        JsonObject returnObject = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : rule.entrySet()) {

            String effectID = "DEFAULT";
            short amplifier = 0;
            int duration = 60;
            boolean infinite = false;
            JsonObject entryObject = entry.getValue().getAsJsonObject();
            if (entryObject.has("EffectID")) {
                effectID = entry.getValue().getAsJsonObject().get("EffectID").getAsString();
            }

            if (entryObject.has("Level")) {
                if (entryObject.get("Level").isJsonArray()
                        && entryObject.get("Level").getAsJsonArray().size() == 13) {
                    amplifier = entryObject.get("Level").getAsJsonArray().get(index).getAsShort();
                } else {
                    amplifier = entryObject.get("Level").getAsShort();
                }
            }

            if (entryObject.has("Infinite")) {
                infinite = true;
            }
            else { if (entryObject.has("Duration")) {
                if (entryObject.get("Duration").isJsonArray()
                        && entryObject.get("Duration").getAsJsonArray().size() == 13) {
                    duration = entryObject.get("Duration").getAsJsonArray().get(index).getAsInt();
                }
            }
            }

            JsonObject object = new JsonObject();
            object.addProperty("Level", amplifier);
            if (infinite) {
                object.addProperty("Infinite", true);
            } else {
                object.addProperty("Duration", duration);
            }

            returnObject.add(effectID, object);
        }

        System.out.println("The returnable Json object is: ");
        System.out.println(returnObject);
        return returnObject;
    }
}
