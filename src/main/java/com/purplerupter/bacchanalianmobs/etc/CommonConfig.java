package com.purplerupter.bacchanalianmobs.etc;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;

public class CommonConfig {
    private static File configPath;
    private static final String CONFIG_FILE_NAME = "Common_Config.cfg";
    private static Configuration config;

    public static boolean allowDebugMessages;
    public static boolean performanceAnalyse;

    public static boolean enableDifficultyModule;
    public static boolean enableEntityConfigModule;
    public static boolean enableEquipmentModule;
    public static boolean enableBreakBlocksModule;
    public static boolean enablePillaringModule;
    public static boolean enableTransportModule;
    public static boolean enableFeaturesModule;
    public static boolean enableSightModule;

    public static boolean enableHealthBoostModule;

    public CommonConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
    }

    public void loadConfig() {
        if (!configPath.exists()) {
            createDefaultConfig();
        }

        config = new Configuration(configPath);
        try {
            config.load();
            allowDebugMessages = config.getBoolean("Allow debug messages", Configuration.CATEGORY_GENERAL, false,
                    "Messages in debug.log file. Don't enable it unless absolutely necessary, it can bloat your log to 100 megabytes or more.");
            System.out.println("Common Config successfully parsed, and the debug mode is: " + allowDebugMessages);
            performanceAnalyse = config.getBoolean("Allow performance analysis messages", Configuration.CATEGORY_GENERAL, false,
                    "Print to debug.log the difference in milliseconds between start and end of execution of significant code fragments. Shows how quickly the server executes code. Values above 50 indicate performance issues.");

            enableDifficultyModule = config.getBoolean("Enable the Dynamic Difficulty module", Configuration.CATEGORY_GENERAL, true,
                    "The Dynamic Difficulty module allows you to change the behavior of many other modules very flexibly. I do not recommend disabling it unless absolutely necessary.");
            enableEntityConfigModule = config.getBoolean("Enable the Entity Config module", Configuration.CATEGORY_GENERAL, true,
                    "The Entity Config module allows you to change the damage that entities take from different sources and from different attackers; " +
                            "it also allows you to apply potion effects and deal damage as a defense or offense.");
            enableEquipmentModule = config.getBoolean("Enable the Equipment module", Configuration.CATEGORY_GENERAL, true,
                    "The Equipment module allows mobs to spawn with equipment (armor, weapon, tools etc).");
            enableBreakBlocksModule = config.getBoolean("Enable the Break Blocks module", Configuration.CATEGORY_GENERAL, true,
                    "The Break Blocks module allows mobs to break blocks to reach their target");
            enablePillaringModule = config.getBoolean("Enable the Pillaring module", Configuration.CATEGORY_GENERAL, true,
                    "The Pillaring module allows mobs to pillar up to reach their target");
            enableTransportModule = config.getBoolean("Enable the Transport module", Configuration.CATEGORY_GENERAL, true,
                    "The Transport module allows mobs to call special entity as vehicle and riding it to reach the target");
            enableFeaturesModule = config.getBoolean("Enable the Features module", Configuration.CATEGORY_GENERAL, true,
                    "Various small and local complications, tweaks, AI tasks.");
            enableSightModule = config.getBoolean("Enable the Sight module", Configuration.CATEGORY_GENERAL, true,
                    "X-Ray vision mode for mobs, which can be used in different cases. Requires for the Raid module to work.");
            enableHealthBoostModule = config.getBoolean("Enable the Health Boost module", Configuration.CATEGORY_GENERAL, true,
                    "Items that can increase the maximum player HP.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }


        public void createDefaultConfig() {
        try {
            configPath.getParentFile().mkdirs();
            configPath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
