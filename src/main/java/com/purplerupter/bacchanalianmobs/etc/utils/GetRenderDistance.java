package com.purplerupter.bacchanalianmobs.etc.utils;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.etc.conditions.utils.GetRenderDistance.getRenderDistance;

public class GetRenderDistance {
    private static final Map<Short, Short> dimensionalRenderDistance = new HashMap<>();

    public static void setRenderDistance(short dimension, short distance) {
        dimensionalRenderDistance.put(dimension, distance);
    }

    public static short getRenderDistanceDimension(short dimension) {
        return dimensionalRenderDistance.get(dimension);
    }

    @SubscribeEvent
    public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        setRenderDistance((short) event.player.dimension,
                (short) getRenderDistance(event.player.world.getMinecraftServer(), event.player.dimension)
        );
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        setRenderDistance((short) event.player.dimension,
                (short) getRenderDistance(event.player.world.getMinecraftServer(), event.player.dimension)
        );
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        setRenderDistance((short) event.player.dimension,
                (short) getRenderDistance(event.player.world.getMinecraftServer(), event.player.dimension)
        );
    }
}
