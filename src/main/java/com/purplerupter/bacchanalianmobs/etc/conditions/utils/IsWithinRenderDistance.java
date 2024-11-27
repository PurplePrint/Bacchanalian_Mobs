package com.purplerupter.bacchanalianmobs.etc.conditions.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class IsWithinRenderDistance {


    public static boolean isWithinRenderDistance(Entity entity, EntityPlayerMP player, int renderDistance) {
        int entityX = (int) entity.posX;
        int entityZ = (int) entity.posZ;
        int chunkXEntity = entityX >> 4;
        if (debug) { System.out.println("Entity coordinates X: " + chunkXEntity); }
        int chunkZEntity = entityZ >> 4;
        if (debug) { System.out.println("Entity coordinates Z: " + chunkZEntity); }

        int chunkXPlayer = player.chunkCoordX;
        if (debug) { System.out.println("Player coordinate X: " + chunkXPlayer); }
        int chunkZPlayer = player.chunkCoordZ;
        if (debug) { System.out.println("Player coordinate Z: " + chunkZPlayer); }

        boolean result = Math.abs(chunkXEntity - chunkXPlayer) <= renderDistance && Math.abs(chunkZEntity - chunkZPlayer) <= renderDistance;
        if (debug) { System.out.println("The result is: " + result); }
        return result;
    }
}
