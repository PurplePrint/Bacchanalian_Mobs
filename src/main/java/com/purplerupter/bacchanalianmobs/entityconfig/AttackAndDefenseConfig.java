package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.*;
import com.purplerupter.bacchanalianmobs.etc.conditions.Conditions;
import net.minecraft.entity.Entity;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class AttackAndDefenseConfig {
    private static final String attackAndDefenseConfigFileName = "attack_and_defense_config.json";
    private static File attackAndDefenseConfigPath;
    private JsonObject configData;
    private Gson gson;



    public AttackAndDefenseConfig(File configDir) {
        attackAndDefenseConfigPath = new File(configDir, attackAndDefenseConfigFileName);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfig();
    }

    private void loadConfig() {
        if (!attackAndDefenseConfigPath.exists()) {
            createDefaultConfig(attackAndDefenseConfigFileName, attackAndDefenseConfigPath);
        }
        try (FileReader reader = new FileReader(attackAndDefenseConfigPath)) {
            if (debug) { System.out.println("try to parse..."); }
            this.configData = gson.fromJson(reader, JsonObject.class);
            if (debug) { System.out.println("success"); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject getMobOffenseRule(List<String> processedRules, String mobID, Entity entity, byte index) {
        return getRule("MobOffense", processedRules, mobID, entity, index);
    }

    public JsonObject getMobDefenseRule(List<String> processedRules, String mobID, Entity entity, byte index) {
        return getRule("MobDefense", processedRules, mobID, entity, index);
    }

    private JsonObject getRule(String section, List<String> processedRules, String mobID, Entity entity, byte index) {

        int rulesCount = configData.getAsJsonObject(section).entrySet().size();
        int ruleIndex = 0;
        boolean lastRule = false;

        if (configData.has(section)) {
            if (debug) { System.out.println("has section and it is: " + section); }

            for (Map.Entry<String, JsonElement> entry : configData.getAsJsonObject(section).entrySet()) {
                if (debug) { System.out.println("'for'..."); }
                ruleIndex++;
                if (debug) { System.out.println("that rule element is: " + entry); }

                if (ruleIndex >= rulesCount) {
                    if (debug) { System.out.println("This is the last rule from config"); }
                    lastRule = true;
                }

                String ruleName = entry.getKey().toString();
                if (debug) { System.out.println("The AttackAndDefense rule name is: " + ruleName); }
                if (processedRules != null && !processedRules.isEmpty()) {
                    if (processedRules.contains(ruleName)) {
                        if (debug) { System.out.println("The rule " + ruleName + " already processed. Skip it"); }
                        continue;
                    }
                }

                JsonObject rule = entry.getValue().getAsJsonObject();
                if (arrayContains(rule.get("Mob list").getAsJsonArray(), mobID)) {
                    if (debug) { System.out.println("matchesMobID!"); }
                    if (Conditions.checkConditions(rule.getAsJsonObject("Conditions"), entity)) {

                        if (lastRule) {
                            rule.addProperty("Last rule", true);
                        }
                        rule.addProperty("Rule name", ruleName);

                        return processRule(rule, index);
                    }
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

    private JsonObject processRule(JsonObject rule, byte index) {
        JsonObject processedRule = new JsonObject();
        if (rule.has("Damage")) {
            processedRule.add("Damage", processDamageOrEffect(rule.getAsJsonObject("Damage"), index));
        }
        if (rule.has("Effects")) {
            processedRule.add("Effects", processDamageOrEffect(rule.getAsJsonObject("Effects"), index));
        }
        if (rule.has("Conditions")) {
            processedRule.add("Conditions", rule.getAsJsonObject("Conditions"));
        }
        if (rule.has("Multipliers")) {
            processedRule.add("Multipliers", rule.getAsJsonObject("Multipliers"));
        }
        if (rule.has("Rule name")) {
            processedRule.addProperty("Rule name", rule.get("Rule name").getAsString());
        }
        return processedRule;
    }

    private JsonObject processDamageOrEffect(JsonObject source, byte index) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : source.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonArray() && value.getAsJsonArray().size() == 13) {
                result.addProperty(key, value.getAsJsonArray().get(index).getAsInt());
            } else {
                result.add(key, value);
            }
        }
        return result;
    }
}
