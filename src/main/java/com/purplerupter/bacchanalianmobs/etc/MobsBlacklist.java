package com.purplerupter.bacchanalianmobs.etc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class MobsBlacklist {

    public static final List<String> MOBS_BLACKLIST = new ArrayList<>();
    public static final List<String> MODS_BLACKLIST = new ArrayList<>() ;

    private static final String BLACKLIST_CONFIG_FILENAME = "Mobs_Blacklist.cfg";
    private static File configPath;

    public MobsBlacklist(File configDir) {
        configPath = new File(configDir, BLACKLIST_CONFIG_FILENAME);
        loadConfig();
    }

    private static void loadConfig() {
        if (!configPath.exists()) {
            createDefaultConfig(BLACKLIST_CONFIG_FILENAME, configPath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Rule for specific mob
                if (line.split(":").length == 2) { // TODO  '== 3' ???
                    MOBS_BLACKLIST.add(line);
                }

                // Rule for all mobs in whole mod
                if (line.endsWith("*")) {
                    MODS_BLACKLIST.add(line.substring(0, line.length() - 1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
