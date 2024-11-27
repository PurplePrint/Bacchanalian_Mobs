package com.purplerupter.bacchanalianmobs.breakblocks.config;

import net.minecraft.item.Item;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.DefaultConfigs.createDefaultConfig;

public class TurretBlocksConfig {
    private static final String CONFIG_FILE_NAME = "list_of_turret_blocks.cfg";
    private static File configPath;

    public static ArrayList<String> turretBlocks = new ArrayList<>();

    public TurretBlocksConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(CONFIG_FILE_NAME, configPath);
        }
        loadConfig();
    }

    private static void loadConfig() {
        try {
            List<String> lines = Files.readAllLines(configPath.toPath());

            for (String line : lines) {
                if (line.isEmpty() || line.startsWith("#")) { continue; }
                try {
                    Item.getByNameOrId(line);
                    turretBlocks.add(line);
                } catch (Exception e) { if (debug) { System.out.println("The: " + line + " is not a valid item (block) ID!"); } }
            }

        } catch (IOException e) { if (debug) { System.out.println("Error when try to load the TurretBlocks Config."); } e.printStackTrace(); }
    }
}
