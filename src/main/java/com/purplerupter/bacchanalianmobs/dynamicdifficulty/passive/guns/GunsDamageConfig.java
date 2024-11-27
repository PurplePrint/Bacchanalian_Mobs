package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class GunsDamageConfig {
    private static final String GUNS_DAMAGE_CONFIG_FILE_NAME = "guns_damage.cfg";
    private static File gunsDamageConfigPath;

//    private static JsonObject configData = new JsonObject();

    public static Map<String, List<String>> magazineIDsForGun = new HashMap<>();
    public static Map<String, Short> roundsAmountForGun = new HashMap<>();

    public GunsDamageConfig(File configDir) {
        gunsDamageConfigPath = new File(configDir, GUNS_DAMAGE_CONFIG_FILE_NAME);
        loadConfig();
    }

    private void loadConfig() {
        if (!gunsDamageConfigPath.exists()) {
            createDefaultConfig(GUNS_DAMAGE_CONFIG_FILE_NAME, gunsDamageConfigPath);
        }
    }

    public static float getAverageDamage(String gun, float range) {
        for (String gunString[] : getConfigurations()) {
            if (debug) { System.out.println("Damage values from config for the gun " + gun + " is: " + Arrays.toString(gunString)); }
            if (gunString[0].equals(gun)) {

                float maxDamage = Float.parseFloat(gunString[1]);
                float minDamage = Float.parseFloat(gunString[2]);
                float minDistance = Float.parseFloat(gunString[3]);
                float maxDistance = Float.parseFloat(gunString[4]);

                float customMultiplier = Float.parseFloat(gunString[5]);

                if (customMultiplier !=0 ){
                    maxDamage *= customMultiplier;
                    minDamage *= customMultiplier;
                }

                if (range <= (minDistance * 1.1)) {
                    return maxDamage;
                }
                if (range >= (maxDistance * 0.95)) {
                    return minDamage;
                }

                return (maxDamage + minDamage) / 2;
            }
        }

        return 0F;
    }

    public static List<String[]> getConfigurations() {
        List<String[]> configurations = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(gunsDamageConfigPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length == 8) {

                    String[] output = new String[6];
                    for (byte i = 0; i < 6; i++) {
                        output[i] = parts[i];
                    }
                    configurations.add(output);

//                    if (!magazineIDsForGun.containsKey(parts[0])) {
                    magazineIDsForGun.put(parts[0], Arrays.asList(parts[6].split(",")));

                    if (debug) {
                        for (String s : magazineIDsForGun.keySet()) {
                            System.out.println("Gun ID is: " + s + " and magazines IDs is: ");
                            for (String mag : magazineIDsForGun.get(s)) {
                                System.out.println(mag);
                            }
                        }
                    }
//                    }

                    if (!roundsAmountForGun.containsKey(parts[0])) {
                        roundsAmountForGun.put(parts[0], Short.parseShort(parts[7]));
                        if (debug) { System.out.println("Gun ID is: " + parts[0] + " and its rounds amount is: " + parts[7]); }
                    }

                } else {
                    if (debug) { System.out.println("Error! Length of line " + line + " is: " + line.length() + ", but it must be 8!"); }
                }
            }
            if (debug) { configurations.forEach(config -> System.out.println("Configuration entry: " + Arrays.toString(config))); }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configurations;
    }
}
