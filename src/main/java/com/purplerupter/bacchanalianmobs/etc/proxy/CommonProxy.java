package com.purplerupter.bacchanalianmobs.etc.proxy;

import com.purplerupter.bacchanalianmobs.content.etc.events.RestorePlayerMaxHealth;
import com.purplerupter.bacchanalianmobs.etc.CommonConfig;
import com.purplerupter.bacchanalianmobs.Tick;
import com.purplerupter.bacchanalianmobs.entityconfig.*;
import com.purplerupter.bacchanalianmobs.etc.events.HandleEntityAttack;
import com.purplerupter.bacchanalianmobs.etc.events.MobSpawnEventHandler;
import com.purplerupter.bacchanalianmobs.etc.utils.DimensionTracker;
import com.purplerupter.bacchanalianmobs.etc.utils.GetRenderDistance;
import com.purplerupter.bacchanalianmobs.features.FeaturesConfig;
//import com.purplerupter.bacchanalianmobs.features.creeper.ChargedCreeper;
import com.purplerupter.bacchanalianmobs.features.creeper.Fire;
import com.purplerupter.bacchanalianmobs.features.general.avoidexplosions.HandleCreeper;
import com.purplerupter.bacchanalianmobs.features.general.avoidexplosions.HandleTNT;
import com.purplerupter.bacchanalianmobs.features.general.contusion.ExplosionHandler;
import com.purplerupter.bacchanalianmobs.features.general.leap.DisableFallDamage;
import com.purplerupter.bacchanalianmobs.features.general.riding.StartRiding;
import com.purplerupter.bacchanalianmobs.features.spider.web.WebOnAttack;
//import com.purplerupter.bacchanalianmobs.sight.FindBeaconNearby;
import com.purplerupter.bacchanalianmobs.sight.XRayFromPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.configDir;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class CommonProxy {

    public static DamageConfig damageConfig;

    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("preInit from CommonProxy");

        ScalingPropsConfig config1 = null;

        if (CommonConfig.enableEntityConfigModule) {
            if (debug) { System.out.println("[EntityConfig] Pre-Initialization started."); }

            AttackAndDefenseConfig config = new AttackAndDefenseConfig(configDir);
            MinecraftForge.EVENT_BUS.register(new AttackAndDefenseEventHandler(config));

            damageConfig = new DamageConfig(configDir);
            MinecraftForge.EVENT_BUS.register(new DamageEventHandler());

            config1 = new ScalingPropsConfig(configDir);
//            MinecraftForge.EVENT_BUS.register(new ScalingPropsEventHandler(config1));

            MinecraftForge.EVENT_BUS.register(new PotionEffectsConfig(configDir));

            if (debug) { System.out.println("[EntityConfig] Pre-Initialization completed."); }

        } else {
            System.out.println("The Entity Config module is disabled");
        }

        System.out.println("Init event handlers on server side (CommonProxy)");
        MinecraftForge.EVENT_BUS.register(new MobSpawnEventHandler(config1));
        MinecraftForge.EVENT_BUS.register(new HandleEntityAttack());
        MinecraftForge.EVENT_BUS.register(new Tick());

        if (CommonConfig.enableFeaturesModule) {
            System.out.println("Init 'Features' module classes on server side (CommonProxy)");

            MinecraftForge.EVENT_BUS.register(new FeaturesConfig(configDir));

            MinecraftForge.EVENT_BUS.register(new HandleCreeper());
            MinecraftForge.EVENT_BUS.register(new HandleTNT());
            MinecraftForge.EVENT_BUS.register(new DisableFallDamage());
//            MinecraftForge.EVENT_BUS.register(new ChargedCreeper());
            MinecraftForge.EVENT_BUS.register(new Fire());
            MinecraftForge.EVENT_BUS.register(new StartRiding());
            MinecraftForge.EVENT_BUS.register(new WebOnAttack());
            MinecraftForge.EVENT_BUS.register(new ExplosionHandler());
        } else {
            System.out.println("The Features module is disabled");
        }

//        if (CommonConfig.enableSightModule) {
//            System.out.println("Init 'Sight' module classes on server side (CommonProxy)");
//            MinecraftForge.EVENT_BUS.register(new XRayFromPlayer());
//        } else {
//            System.out.println("The 'Sight' module is disabled");
//        }

        if (CommonConfig.enableHealthBoostModule) {
            System.out.println("Init 'Health Boost' module classes on server side (CommonProxy)");

            MinecraftForge.EVENT_BUS.register(new RestorePlayerMaxHealth());
        }

        // Utils
        MinecraftForge.EVENT_BUS.register(new DimensionTracker());
        MinecraftForge.EVENT_BUS.register(new GetRenderDistance());
    }
}
