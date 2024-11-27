package com.purplerupter.bacchanalianmobs.content.items;

import com.purplerupter.bacchanalianmobs.content.items.constructor.HealthItem;
import net.minecraft.creativetab.CreativeTabs;

public class LivingHeart extends HealthItem {
    public LivingHeart() {
        super(false);

        this.setRegistryName("living_heart");
        this.setUnlocalizedName("living_heart");
        this.setCreativeTab(CreativeTabs.MISC); // TODO
    }
}
