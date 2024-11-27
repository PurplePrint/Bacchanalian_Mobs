package com.purplerupter.bacchanalianmobs.equipment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.purplerupter.bacchanalianmobs.etc.conditions.Conditions;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.GetNearbyDifficulty.getNearbyDifficulty;
import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;
import static com.purplerupter.bacchanalianmobs.etc.utils.isRuleForMob.isRuleForMob;

public class EquipmentConfigHandler {


    private static final String CONFIG_FILE_NAME = "equipment.json";
    private static File configPath;
    private static JsonObject configData;

    public EquipmentConfigHandler(File configDir) {
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

    public static boolean isRuleEquipmentForMob(String mobID) {
        return isRuleForMob(configData, mobID);
    }

    public static JsonObject getEquipmentDataForMob(EntityLiving entity, ArrayList<String> processedRules) {
        String mobID = EntityList.getKey(entity).toString();
        int ruleIndex = 0;
        int rulesCount = configData.getAsJsonObject("Rules").entrySet().size();
        for (Map.Entry<String, JsonElement> entry : configData.getAsJsonObject("Rules").entrySet()) {
            ruleIndex++;
            String ruleName = entry.getKey();
            if (debug) { System.out.println("The ruleName is: " + ruleName); }
            if (debug) { System.out.println("The ruleIndex is: " + ruleIndex); }
            if (debug) { System.out.println("All rules count: " + rulesCount); }

            if (processedRules != null && !processedRules.isEmpty()) {
                if (debug) { System.out.println("The list of all rules processed before is: " + processedRules); }
                if (processedRules.contains(ruleName)) {
                    if (debug) { System.out.println("The rule: " + ruleName + " already processed and applied"); }
                    continue;
                }
            }

            JsonObject ruleObject = entry.getValue().getAsJsonObject();
            if (debug) { System.out.println("The ruleObject is: " + ruleObject); }
            ruleObject.addProperty("Rule name", ruleName);

            if (ruleIndex >= rulesCount) {
                if (debug) { System.out.println("This is the last rule from config"); }
                ruleObject.addProperty("Last rule", true);
            }

            JsonArray mobs = ruleObject.getAsJsonArray("Mob List");
            if (arrayContains(mobs, mobID)) {

                if (debug) { System.out.println("Match rule for this mobID: " + mobID + " // this rule called: " + ruleName); }
                boolean passed = true;
                if (ruleObject.has("Conditions")) {
                    if (!Conditions.checkConditions(ruleObject.getAsJsonObject("Conditions"), entity)) {
                        if (debug) { System.out.println("Conditions not passed, mob is " + entity); }
                        passed = false;
                    }
                    else { System.out.println("Conditions passed, return the rule object"); }
                }
                if (passed) {
                    ruleObject.remove("Mob list");
                    ruleObject.remove("Conditions");
                    return ruleObject;
                }
            }
            // TODO
        }
        return null;
    }

    public static String getItemGroupFromAppointment(String appointmentName, String slotType, EntityLiving entity) {
        JsonObject listOfAllAppointments = configData.getAsJsonObject("Appointments");
        for (Map.Entry<String, JsonElement> entry : listOfAllAppointments.entrySet()) {
            if (appointmentName.equals(entry.getKey())) {
                if (debug) { System.out.println("Match the appointment name"); }

                JsonObject appointment = entry.getValue().getAsJsonObject();
                String appointmentSlotType = appointment.get("Type").getAsString();
                if (appointmentSlotType.equals(slotType)) {
                    if (debug) { System.out.println("The slot type matched"); }
                    JsonObject appointmentRule = entry.getValue().getAsJsonObject();

                    if (appointmentRule.has("Conditions")) {
                        if (debug) { System.out.println("This appointment rule has Conditions"); }
                        if (debug) { System.out.println(appointmentRule.get("Conditions").getAsJsonObject()); }
                        if (!Conditions.checkConditions(appointmentRule.get("Conditions").getAsJsonObject(), entity)) { continue; }
                        if (debug) { System. out.println("Conditions passed for " + entry.getKey() + " rule"); }
                    }

                    if (appointmentRule.has("Conditions To Force")) {
                        if (debug) { System.out.println("This appointment rule has Conditions To Force"); }
                        if (Conditions.checkConditions(appointmentRule.get("Conditions To Force").getAsJsonObject(), entity)) {
                            if (debug) { System.out.println("Conditions To Force passed! This appointments will be chose ignoring the chance"); }
                            return appointmentRule.get("Item Group Name").getAsString();
                        }
                    }

                    byte chance;
                    int basicChance = appointmentRule.get("Basic Chance").getAsInt();
                    if (basicChance > 100) {
                        if (debug) { System.out.println("Error! The chance is " + basicChance + " and it's more than 100 percent!"); }
                        basicChance = 100;
                    }

                    if (basicChance == 100) {
                        if (debug) { System.out.println("The chance is " + basicChance + ", so the only way is choose this appointment"); }
                        return appointmentRule.get("Item Group Name").getAsString();
                    }

                    chance = (byte)basicChance;

                    if (appointmentRule.has("Difficulty Multiplier")) {
                        if (debug) { System.out.println("This appointment rule has Difficulty Multiplier"); }
                        JsonObject multiplier = appointment.getAsJsonObject("Difficulty Multiplier");

                        double difficultyMin = multiplier.get("Difficulty Min").getAsDouble();
                        double difficultyMax = multiplier.get("Difficulty Max").getAsDouble();
                        double targetDifficulty = multiplier.get("Target Difficulty").getAsDouble();

                        double nearbyDifficulty = getNearbyDifficulty(entity, (byte)1);
                        if (nearbyDifficulty > difficultyMax) {
                            if (debug) { System.out.println("The nearby difficulty is: " + nearbyDifficulty + ". It's more than the maximum difficulty, so maximum difficulty will be chose."); }
                        }
                        if (nearbyDifficulty < difficultyMin) {
                            if (debug) { System.out.println("The nearby difficulty is: " + nearbyDifficulty + ". It's less than the minimum difficulty for this multiplier, so multiplier will not process."); }
                        } else {
                            int newChance = (int)((double)chance * (nearbyDifficulty / targetDifficulty));
                            if (newChance < 0) { if (debug) { System.out.println("Error, the chance is less than 0!"); } return null; }
                            if (newChance > 100) { chance = 100; } else { chance = (byte)newChance; }
                            if (debug) { System.out.println("The chance is: " + chance + " (it was " + newChance + ", but maximum is 100%)."); }
                        }

                        if (chance > 100) { return appointmentRule.get("Item Group Name").getAsString(); }
                    }

                    Random random = new Random();
                    int randomNumber = random.nextInt(100);
                    if (debug) { System.out.println("Random number for chance is " + randomNumber); }
                    if (randomNumber < chance) {
                        if (debug) { System.out.println("Chance passed"); }
                        return appointmentRule.get("Item Group Name").getAsString();
                    } else { if (debug) { System.out.println("Chance not passed"); } }
                }
            }
        }
        return null;
    }

    public static ItemStack getItemStackFromItemGroup(String itemGroupName, EntityLiving entity) {
        JsonObject listOfAllItemGroups = configData.getAsJsonObject("Item Groups");
        for (Map.Entry<String, JsonElement> entry : listOfAllItemGroups.entrySet()) {
            if (entry.getKey().equals(itemGroupName)) {
                if (debug) { System.out.println("Item Group name matched: " + itemGroupName); }

                for (JsonElement itemElement : entry.getValue().getAsJsonArray()) {
                    JsonArray itemArray;
                    try {
                        itemArray = itemElement.getAsJsonArray();
                    } catch (Exception e) {
                        if (debug) { System.out.println("Error when try to parse one of item line in item group as json array!"); } e.printStackTrace(); continue;
                    }

                    if (!((itemArray.size() == 2) || (itemArray.size() == 4) || (itemArray.size() == 6) || (itemArray.size() == 8))) {
                        if (debug) { System.out.println("Error! The item array length is invalid! It is: " + itemArray.size()); } continue;
                    }

                    boolean simpleMode = false; boolean difficultyMode = false;

                    String itemName = itemArray.get(0).getAsString(); byte chance = itemArray.get(1).getAsByte();
                    double difficultyMin = 0; double difficultyMax = 0; double targetDifficulty = 0; byte targetChance = 0;
                    String enchantmentString = null; short enchantmentLevel = 1;

                    switch (itemArray.size()) {
                        case 2:
                            simpleMode = true;
                            break;
                        case 4:
                            simpleMode = true;
                            enchantmentString = itemArray.get(2).getAsString();
                            enchantmentLevel = itemArray.get(3).getAsShort();
                            break;
                        case 6:
                            difficultyMode = true;
                            difficultyMin = itemArray.get(2).getAsDouble();
                            difficultyMax = itemArray.get(3).getAsDouble();
                            targetDifficulty = itemArray.get(4).getAsDouble();
                            targetChance = itemArray.get(5).getAsByte();
                            break;
                        case 8:
                            difficultyMode = true;
                            difficultyMin = itemArray.get(2).getAsDouble();
                            difficultyMax = itemArray.get(3).getAsDouble();
                            targetDifficulty = itemArray.get(4).getAsDouble();
                            targetChance = itemArray.get(5).getAsByte();
                            enchantmentString = itemArray.get(6).getAsString();
                            enchantmentLevel = itemArray.get(7).getAsShort();
                            break;
                    }

                    if (!difficultyMode && !simpleMode) { if (debug) { System.out.println("Error! 'difficultyMode' and 'simpleMode' are false, can not process the chance of this item!"); } continue; }

                    if (difficultyMode) {
                        if (debug) { System.out.println("The chance of this item group is based on difficulty points"); }
                        double nearbyDifficulty = getNearbyDifficulty(entity, (byte)1);
                        if (nearbyDifficulty > difficultyMax) {
                            if (debug) { System.out.println("The nearby difficulty is: " + nearbyDifficulty + ". It's more than the maximum difficulty, so difficultyMax will be chose."); }
                        }
                        if (nearbyDifficulty < difficultyMin) {
                            if (debug) { System.out.println("The nearby difficulty is: " + nearbyDifficulty + ". It's less than the minimum difficulty for this item multiplier, so basic chance will be chose."); }
                        } else {
                            int newChance = (int)((double)targetChance * (nearbyDifficulty / targetDifficulty));
                            if (newChance < 0) { if (debug) { System.out.println("Error, the chance is less than 0!"); } continue; }
                            if (newChance > 100) { chance = 100; } else { chance = (byte)newChance; }
                            if (debug) { System.out.println("The chance is: " + chance + " (it was " + newChance + ", but maximum is 100%)."); }
                        }
                    }

                    if (simpleMode) {
                        if (debug) { System.out.println("The chance of this item group is a simple chance"); }
                    }

                    Random random = new Random();
                    int randomNumber = random.nextInt(100); if (debug) { System.out.println("The random number is: " + randomNumber); }
                    if (randomNumber < chance) {
                        if (debug) { System.out.println("Chance is: " + chance + ", chance passed"); }

                        if (debug) { System.out.println("The itemName is: " + itemName); }
                        if (debug) { System.out.println("The Item is: " + Item.getByNameOrId(itemName)); }
                        ItemStack stack = new ItemStack(Item.getByNameOrId(itemName), 1);
                        if (debug) { System.out.println("The ItemStack is: " + stack); }
                        if (enchantmentString != null) {
                            if (debug) { System.out.println("Applying enchantment " + enchantmentString + " with level " + enchantmentLevel); }
                            Enchantment enchantment = Enchantment.getEnchantmentByLocation(enchantmentString);
                            stack.addEnchantment(enchantment, enchantmentLevel);
                        }
                        return stack;
                    } else {
                        if (debug) { System.out.println("Chance is: " + chance + ", chance not passed"); }
                    }
                }
            }
        }
        return null;
    }

}
