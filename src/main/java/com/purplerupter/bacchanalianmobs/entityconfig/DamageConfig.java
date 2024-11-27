package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purplerupter.bacchanalianmobs.etc.conditions.Conditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class DamageConfig {


    private static final String DAMAGE_CONFIG_FILENAME = "damage_config.json";
    private static File damageConfigPath;
    private JsonObject configData;

    public DamageConfig(File configDir) {
        damageConfigPath = new File(configDir, DAMAGE_CONFIG_FILENAME);
        if (!damageConfigPath.exists()) {
            createDefaultConfig(DAMAGE_CONFIG_FILENAME, damageConfigPath);
        }
        loadConfig();
    }

    private void loadConfig() {
        try (FileReader reader = new FileReader(damageConfigPath)) {
//            configData = JsonParser.parseReader(reader).getAsJsonObject(); // java.lang.NoSuchMethodError
            configData = new JsonParser().parse(reader).getAsJsonObject();
            if (debug) { System.out.println("Config was loaded."); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonObject getDamageSourceRule(List<String> processedRules, String mobID, EntityLivingBase entity, String damageSource, byte index) {

        int rulesCount = configData.getAsJsonObject("DamageSourceMultipliers").entrySet().size();
        int ruleIndex = 0;
        boolean lastRule = false;

        JsonObject damageSourceMultipliers = configData.getAsJsonObject("DamageSourceMultipliers");
        for (Map.Entry<String, JsonElement> entry : damageSourceMultipliers.entrySet()) {
            if (debug) { System.out.println("'for'..."); }
            ruleIndex++;

            if (ruleIndex >= rulesCount) {
                if (debug) { System.out.println("This is the last rule from config"); }
                lastRule = true;
            }

            String ruleName = entry.getKey().toString();
            if (debug) { System.out.println("The DamageConfig rule name is: " + ruleName); }
            if (processedRules != null && !processedRules.isEmpty()) {
                if (processedRules.contains(ruleName)) {
                    if (debug) { System.out.println("The rule " + ruleName + " already processed. Skip it"); }
                    continue;
                }
            }

            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            JsonArray mobs = ruleObject.getAsJsonArray("mobID"); // TODO

            if (arrayContains(mobs, mobID)) {
                JsonObject damageChanges = ruleObject.getAsJsonObject("DamageChanges");

                if (damageChanges.has(damageSource)) {
                    if (Conditions.checkConditions(ruleObject.getAsJsonObject("Conditions"), entity)) {
                        if (debug) { System.out.println("Conditions passed for a Damage rule"); }

                        if (lastRule) {
                            ruleObject.addProperty("Last rule", true);
                        }
                        ruleObject.addProperty("Rule name", ruleName);

                        // if 13 float numbers (from -2 to 10 SRP phase)
                        if (damageChanges.get(damageSource).isJsonArray()) {
                            return extractIndexedDamageRule(ruleObject, damageSource, index);
                        } else {
                            return ruleObject;
                        }

                    } else {
                        if (debug) { System.out.println("Conditions not passed for a Damage rule"); }
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

    public JsonObject getEntityDamageRule(List<String> processedRules, String attackerID, String victimID, Entity entity, byte index) {

        int rulesCount = configData.getAsJsonObject("EntityDamageMultipliers").entrySet().size();
        int ruleIndex = 0;
        boolean lastRule = false;

        JsonObject entityDamageMultipliers = configData.getAsJsonObject("EntityDamageMultipliers");
        for (Map.Entry<String, JsonElement> entry : entityDamageMultipliers.entrySet()) {
            if (debug) { System.out.println("'for'..."); }
            ruleIndex++;

            if (ruleIndex >= rulesCount) {
                if (debug) { System.out.println("This is the last rule from config"); }
                lastRule = true;
            }

            String ruleName = entry.getKey().toString();
            if (debug) { System.out.println("The DamageConfig rule name is: " + ruleName); }
            if (processedRules != null && !processedRules.isEmpty()) {
                if (processedRules.contains(ruleName)) {
                    if (debug) { System.out.println("The rule " + ruleName + " already processed. Skip it"); }
                    continue;
                }
            }

            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            if (arrayContains(ruleObject.getAsJsonArray("attackerID"), attackerID)
                    && arrayContains(ruleObject.getAsJsonArray("victimID"), victimID)) {
                if (debug) { System.out.println("Match!"); }

                if (Conditions.checkConditions(ruleObject.getAsJsonObject("Conditions"), entity)) {

                    // if 13 float numbers (from -2 to 10 SRP phase)
                    if (ruleObject.get("multiplier").isJsonArray()) {
                        ruleObject.addProperty("multiplier", ruleObject.getAsJsonArray("multiplier").get(index).getAsFloat());
                    }

                    if (lastRule) {
                        ruleObject.addProperty("Last rule", true);
                    }
                    ruleObject.addProperty("Rule name", entry.getKey());

                    return ruleObject;
                } else {
                    if (debug) { System.out.println("Conditions not passed for an EntityDamage rule"); }
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

    private JsonObject extractIndexedDamageRule(JsonObject ruleObject, String damageSource, byte index) {
        JsonArray damageArray = ruleObject.getAsJsonObject("DamageChanges").getAsJsonArray(damageSource);
        ruleObject.getAsJsonObject("DamageChanges").addProperty(damageSource, damageArray.get(index).getAsFloat());
        return ruleObject;
    }
}
