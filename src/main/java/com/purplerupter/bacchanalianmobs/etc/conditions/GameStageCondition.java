package com.purplerupter.bacchanalianmobs.etc.conditions;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import java.util.List;

import static com.purplerupter.bacchanalianmobs.etc.conditions.utils.GetRenderDistance.getRenderDistance;
import static com.purplerupter.bacchanalianmobs.etc.conditions.utils.IsWithinRenderDistance.isWithinRenderDistance;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class GameStageCondition {
    public static boolean isGameStageNearby(Entity entity, List<String> stages) {
        WorldServer world = null;
        try { world = (WorldServer) entity.getEntityWorld();
        } catch (Exception e) { if (debug) { System.out.println("The entity: " + entity + " world is client, can not cast it into WorldServer"); } }
        if (world != null) {
            int currentDimension = entity.dimension;
            int renderDistanceRadius = getRenderDistance(world.getMinecraftServer(), currentDimension) + 1; // '+ 1' это подстраховка

//            for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
//                if (player.dimension == currentDimension) {
//                    if (hasGameStages(player, stages) && isWithinRenderDistance(entity, player, renderDistanceRadius)) {
//                        return true;
//                    }
//                }
//            }
            for (EntityPlayer player1 : world.playerEntities) {
                EntityPlayerMP player = (EntityPlayerMP) player1;
                if (hasGameStages(player, stages) && isWithinRenderDistance(entity, player, renderDistanceRadius)) {
                    return true;
                }
            }
        } else { if (debug) { System.out.println("The method isGameStageNearby is not processed, because the server is null! Propably, 'entity.getEntityWorld' return a client-side World object."); } }
        return false;
    }

    public static boolean isGameStageNearby(Entity entity, String stage) {
        WorldServer world = (WorldServer) entity.world;
        int currentDimension = entity.dimension;
        int renderDistanceRadius = getRenderDistance(world.getMinecraftServer(), currentDimension) + 1;

//        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
//            if (player.dimension == currentDimension) {
//                if (GameStageHelper.hasStage(player, stage) && isWithinRenderDistance(entity, player, renderDistanceRadius)) {
//                    return true;
//                }
//            }
//        }
        for (EntityPlayer player1 : world.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) player1;
            if (isWithinRenderDistance(entity, player, renderDistanceRadius)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasGameStages(EntityPlayerMP player, List<String> stages) {
        for (String stage : stages) {
            if (!GameStageHelper.hasStage(player, stage)) {
                return false;
            }
        }
        return true;
    }

}
