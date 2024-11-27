package com.purplerupter.bacchanalianmobs.dynamicdifficulty;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class MultiplayerDifficultyModeConfig {


    private static final String CONFIG_FILE_NAME = "multiplayer_difficulty_mode.cfg";
    private static File configPath;
    private static Configuration config;

    public static String equipmentDifficultyMode;
    public static String scalingPropsHealthDifficultyMode;
    public static String scalingPropsHealthTimeMode;
    public static String featuresScaleDifficultyMode;

    public MultiplayerDifficultyModeConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        loadConfig();
    }

    private static void loadConfig() {
        config = new Configuration(configPath);

        try {
            config.load();

            equipmentDifficultyMode = config.getString("Difficulty mode for equipment", Configuration.CATEGORY_GENERAL, "MAX",
                    "Difficulty selection mode for equipment when there are players with different difficulty points nearby. \"MIN\", \"MAX\", \"RANDOM\", \"AVERAGE\".");
            scalingPropsHealthDifficultyMode = config.getString("Difficulty mode for scaling props (health)", Configuration.CATEGORY_GENERAL, "MAX",
                    "Difficulty selection mode for scaling props (health) when there are players with different difficulty points nearby. ");
            scalingPropsHealthTimeMode = config.getString("Time mode for scaling props (health)", Configuration.CATEGORY_GENERAL, "MAX",
                    "Time selection mode for scaling props (health) where there are players with different amount of time nearby. \"MIN\", \"MAX\", \"RANDOM\", \"AVERAGE\".");
            featuresScaleDifficultyMode = config.getString("Difficulty mode for all the Features config", Configuration.CATEGORY_GENERAL, "MAX",
                    "Difficulty selection mode for all the Features scaling when there are players with different difficulty points nearby. \"MIN\", \"MAX\", \"RANDOM\", \"AVERAGE\".");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }
}
