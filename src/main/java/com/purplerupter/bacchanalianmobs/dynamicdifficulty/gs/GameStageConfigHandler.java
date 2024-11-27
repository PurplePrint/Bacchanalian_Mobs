package com.purplerupter.bacchanalianmobs.dynamicdifficulty.gs;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class GameStageConfigHandler {

    private static final String CONFIG_FILE_NAME = "points_per_stages.cfg";
    private static File configPath;

    public GameStageConfigHandler(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
    }

    public List<String[]> getConfigurations() {
        List<String[]> configurations = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    configurations.add(parts);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return configurations;
    }
}
