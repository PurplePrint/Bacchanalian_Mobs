package com.purplerupter.bacchanalianmobs.etc.conditions.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class GetRenderDistance {

    public static int getRenderDistance(MinecraftServer server, int dimensionId) {
        if (server.isSinglePlayer()) {
            int result = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
            if (debug) { System.out.println("This world is a singleplayer, and the render distance is: " + result); }
            return result;
        } else {
//            int result = server.getWorld(dimensionId).getMinecraftServer().getPlayerList().getViewDistance();
            int result;
            try {
                result = server.getWorld(dimensionId).getMinecraftServer().getPlayerList().getViewDistance();
            } catch (NullPointerException e) {
                if (debug) { System.out.println("NullPointerException in 'getPlayerList()' when trying to get the render distance!"); }
                result = 7;
            }
            if (debug) { System.out.println("This world is multiplayer, and the render distance is: " + result); }
            return result;
        }
    }

}
