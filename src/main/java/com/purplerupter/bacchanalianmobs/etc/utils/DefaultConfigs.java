package com.purplerupter.bacchanalianmobs.etc.utils;

import com.purplerupter.bacchanalianmobs.BacchanalianMobs;

import java.io.*;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class DefaultConfigs {
    private static final String RESOURCES_PATH = "assets/" + BacchanalianMobs.MODID + "/default_configs/";


    public static void createDefaultConfig(String configFile, File configPath) {
        try (InputStream inputStream = DefaultConfigs.class.getClassLoader().getResourceAsStream(RESOURCES_PATH + DEFAULT_CONFIG_PREFIX + configFile)) {
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder defaultConfig = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    defaultConfig.append(line).append("\n");
                }

                try (FileWriter writer = new FileWriter(configPath)) {
                    writer.write(defaultConfig.toString());
                    if (debug) { System.out.println("Default config was created: " + configPath); }
                }
            } else {
                if (debug) { System.out.println("Cannot find " + configFile + " in the 'resources' folder."); }
                if (debug) { System.out.println(RESOURCES_PATH); }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
