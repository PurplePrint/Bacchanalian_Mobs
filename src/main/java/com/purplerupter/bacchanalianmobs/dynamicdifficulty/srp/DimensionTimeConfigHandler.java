package com.purplerupter.bacchanalianmobs.dynamicdifficulty.srp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.srp.EvolutionPhaseTracker.setMinimumTime;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class DimensionTimeConfigHandler {


    private static final String CONFIG_FILE_NAME = "dimensions_minimum_time.cfg";
    private static File configPath;
    public DimensionTimeConfigHandler(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
        loadConfig();
    }

    public void loadConfig() {
        try {
            List<String> lines = Files.readAllLines(configPath.toPath());
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length != 2) {
                    continue;
                }

                short dimensionId = parts[0].equalsIgnoreCase("ALL") ? Short.MIN_VALUE : Short.parseShort(parts[0]);
                int timeAmount = Integer.parseInt(parts[1]);

                if (debug) { System.out.println("The key is: " + dimensionId + " // The amount is: " + timeAmount); }
                setMinimumTime(dimensionId, timeAmount);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
