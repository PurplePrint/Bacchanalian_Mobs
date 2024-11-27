package com.purplerupter.bacchanalianmobs.breakblocks.config;

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

public class BreakBlocksConfigHandler {
    private static final String CONFIG_FILE_NAME = "Break_Blocks_config.json";
    private static File configPath;
    private static JsonObject configData;

    public BreakBlocksConfigHandler(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
        loadConfig();
    }

    private static void loadConfig() {
        try (FileReader reader = new FileReader(configPath)) {
//            configData = JsonParser.parseReader(reader).getAsJsonObject(); // java.lang.NoSuchMethodError
            configData = new JsonParser().parse(reader).getAsJsonObject();
            if (debug) { System.out.println("Config was loaded."); }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JsonObject getDiggingRuleForMob(EntityLiving entity) {
        return getDiggingRuleForMob(entity, (byte)0);
    }

    public static JsonObject getDiggingRuleForMob(EntityLiving entity, byte mode) {
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

                    switch (mode) {
                        case 0:
                        default:
                            return ruleObject;
                        case 1:
                            if (debug) { System.out.println("Optimizing the returnable json object for turret destroying mode"); }

                            JsonObject turretDestroyConfig = new JsonObject();
                            boolean allowTurretDestroying = ruleObject.get("Allow turret blocks destroying").getAsBoolean();
                            if (!allowTurretDestroying) { return null; } else {

                                turretDestroyConfig.addProperty("Turret destroying", allowTurretDestroying);
                                turretDestroyConfig.addProperty("Turret destroying (no tool)", ruleObject.get("Required tool for turret destroying").getAsBoolean());
                                turretDestroyConfig.addProperty("Digging speed", ruleObject.get("Turret blocks break speed").getAsShort());
                                turretDestroyConfig.addProperty("Use source hardness", ruleObject.get("Use turret block hardness").getAsBoolean());
                                return turretDestroyConfig;
                            }

                    }
                }
            }
        }

        return null;
    }
}
