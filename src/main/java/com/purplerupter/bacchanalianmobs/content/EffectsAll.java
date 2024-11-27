package com.purplerupter.bacchanalianmobs.content;

import com.purplerupter.bacchanalianmobs.content.effects.EffectBeacon;
import com.purplerupter.bacchanalianmobs.etc.CommonConfig;
import net.minecraft.potion.Potion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.content.effects.EffectBeacon.beaconID;

public class EffectsAll {
    public static Potion effectBeacon = null;

    public static final List<Potion> ALL_POTIONS = new ArrayList<>(Arrays.asList(

    ));

    public static void addPotionsToList() {
        if (CommonConfig.enableSightModule) {
            System.out.println("Sight module is enabled. New potions will be registered.");

            effectBeacon = new EffectBeacon().setRegistryName(beaconID);
            ALL_POTIONS.add(effectBeacon);
        }
    }

//    private static final List<Potion> SIGHT_MODULE_POTIONS = new ArrayList<>(Arrays.asList(
//            effectBeacon
//    ));
}
