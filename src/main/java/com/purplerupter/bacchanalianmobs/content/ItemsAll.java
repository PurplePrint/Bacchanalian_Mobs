package com.purplerupter.bacchanalianmobs.content;

import com.purplerupter.bacchanalianmobs.content.items.*;
import com.purplerupter.bacchanalianmobs.etc.CommonConfig;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemsAll {
    public static final LivingShard LIVING_SHARD = new LivingShard();
    public static final LivingHeart LIVING_HEART = new LivingHeart();
    public static final RoughLivingHeart ROUGH_LIVING_HEART = new RoughLivingHeart();
    public static final CrystalTemplateRaw CRYSTAL_TEMPLATE_RAW = new CrystalTemplateRaw();
    public static final CrystalTemplateCharged CRYSTAL_TEMPLATE_CHARGED = new CrystalTemplateCharged();

    public static final List<Item> ALL_ITEMS = new ArrayList<>(Arrays.asList(
            CRYSTAL_TEMPLATE_RAW,
            CRYSTAL_TEMPLATE_CHARGED
    ));

    public static void addItemsToList() {
        if (CommonConfig.enableHealthBoostModule) {
            System.out.println("Health Boost module is enabled. New items will be registered.");
            ALL_ITEMS.addAll(HEALTH_BOOST_ITEMS);
        }
    }

    private static final List<Item> HEALTH_BOOST_ITEMS = new ArrayList<>(Arrays.asList(
            LIVING_SHARD,
            LIVING_HEART,
            ROUGH_LIVING_HEART
    ));
}
