package com.purplerupter.bacchanalianmobs;

import com.purplerupter.bacchanalianmobs.breakblocks.TurretDeathEventHandler;
import com.purplerupter.bacchanalianmobs.breakblocks.config.BreakBlocksConfigHandler;
import com.purplerupter.bacchanalianmobs.breakblocks.config.TurretBlocksConfig;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import com.purplerupter.bacchanalianmobs.equipment.EquipmentConfigHandler;
import com.purplerupter.bacchanalianmobs.etc.CommonConfig;
import com.purplerupter.bacchanalianmobs.etc.init.InitEffects;
import com.purplerupter.bacchanalianmobs.etc.init.InitEntities;
import com.purplerupter.bacchanalianmobs.etc.init.InitItems;
import com.purplerupter.bacchanalianmobs.etc.proxy.CommonProxy;
import com.purplerupter.bacchanalianmobs.etc.utils.CustomTextManager;
import com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker;
import com.purplerupter.bacchanalianmobs.pillaring.PillaringConfigHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;
import java.util.Random;

@Mod(modid = BacchanalianMobs.MODID, name = BacchanalianMobs.NAME, version = BacchanalianMobs.VERSION)
public class BacchanalianMobs {
    public static final String MODID = "bacchanalianmobs";
    public static final String NAME = "Bacchanalian Mobs";
    public static final String VERSION = "0.4 INDEV";


    public static File configDir;
    public static final String CONFIG_DIR_NAME = "Bacchanalian Mobs";
    public static final String DEFAULT_CONFIG_PREFIX = "default_";

    public static boolean debug;
    public static boolean performanceDebug;

    public static boolean gameStagesIntegration;
    public static boolean spartanWeaponryIntegration;
    public static boolean srParasitesIntegration;
    public static boolean techgunsIntegration;

    private DynamicDifficulty dynamicDifficulty = new DynamicDifficulty();

    public static final Random random = new Random();

//    public static long lastPassiveCalculationTimestamp = 0L;
//    public static long currentTimestamp = 0L;

    @Mod.Instance
    public static BacchanalianMobs instance = new BacchanalianMobs();

    @SidedProxy(clientSide = "com.purplerupter.bacchanalianmobs.etc.proxy.ClientProxy", serverSide = "com.purplerupter.bacchanalianmobs.etc.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        gameStagesIntegration = Loader.isModLoaded("gamestages");
        spartanWeaponryIntegration = Loader.isModLoaded("spartanweaponry");
        srParasitesIntegration = Loader.isModLoaded("srparasites");
        techgunsIntegration = Loader.isModLoaded("techguns");

        if (gameStagesIntegration) { System.out.println("Game Stages integration active"); }
        if (spartanWeaponryIntegration) { System.out.println("Spartan Weaponry integration active"); }
        if (srParasitesIntegration) { System.out.println("SRParasites integration active"); }
        if (techgunsIntegration) { System.out.println("Techguns integration active"); }

        configDir = new File(event.getModConfigurationDirectory(), CONFIG_DIR_NAME);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        // Main config and debug flags
        MinecraftForge.EVENT_BUS.register(new CommonConfig(configDir));
        debug = CommonConfig.allowDebugMessages;
        System.out.println("The debug mode is: " + debug);
        performanceDebug = CommonConfig.allowDebugMessages;
        System.out.println("The performance debug mode is: " + performanceDebug);

        // Utils
        CustomTextManager.init(configDir); // my skill issues with I18N and sides...
        MinecraftForge.EVENT_BUS.register(new DimensionTimeTracker());

        // Content
        MinecraftForge.EVENT_BUS.register(new InitItems());
        InitEntities.initEntities();
        MinecraftForge.EVENT_BUS.register(new InitEffects());

        // Modules

        if (CommonConfig.enableDifficultyModule) {
            dynamicDifficulty.preInit(event);
            System.out.println("The Dynamic Difficulty module loaded (preInit)");
        } else {
            System.out.println("The Dynamic Difficulty module is disabled");
        }

        if (CommonConfig.enableEquipmentModule) {
            MinecraftForge.EVENT_BUS.register(new EquipmentConfigHandler(configDir));
            System.out.println("The Equipment module loaded (preInit)");
        } else {
            System.out.println("The Equipment module is disabled");
        }

        if (CommonConfig.enableBreakBlocksModule) {
            MinecraftForge.EVENT_BUS.register(new BreakBlocksConfigHandler(configDir));
            if (techgunsIntegration) {
                System.out.println("Techguns integration active, load the 'Turret Destroying' content");
                MinecraftForge.EVENT_BUS.register(new TurretBlocksConfig(configDir));
                MinecraftForge.EVENT_BUS.register(new TurretDeathEventHandler());
            }
            System.out.println("The Break Blocks module loaded (pre init)");
        } else {
            System.out.println("The Break Blocks module is disabled");
        }

        if (CommonConfig.enablePillaringModule) {
            MinecraftForge.EVENT_BUS.register(new PillaringConfigHandler(configDir));
            System.out.println("The Pillaring module loaded (pre init)");
        } else {
            System.out.println("The Pillaring module is disabled");
        }

        // Proxies
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (CommonConfig.enableDifficultyModule) {
            dynamicDifficulty.init(event);
            System.out.println("The Dynamic Difficulty module loaded (init)");
        }
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {

        // Commands

        if (CommonConfig.enableDifficultyModule) {
            dynamicDifficulty.serverStarting(event);
            System.out.println("The Dynamic Difficulty module loaded (serverStarting)");
        }
    }

}
