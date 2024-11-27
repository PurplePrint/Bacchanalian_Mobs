package com.purplerupter.bacchanalianmobs.etc.utils;

import net.minecraft.entity.EntityLiving;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class GetAttackReach {
    public static float getAttackReach(EntityLiving entityLiving) {
        float width = entityLiving.width / 2;
        if (debug) { System.out.println("The half width of this EntityLiving (" + entityLiving + ") is: " + width + ". Add this to the basic 3-blocks wide attack reach."); }
        return (3F + width);
    }
}
