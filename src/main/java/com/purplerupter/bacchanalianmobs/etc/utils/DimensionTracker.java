package com.purplerupter.bacchanalianmobs.etc.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import static com.purplerupter.bacchanalianmobs.sight.XRayFromPlayer.updatePlayerDimension;

public class DimensionTracker {
    @SubscribeEvent
    public void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        updatePlayerDimension(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        updatePlayerDimension(event.player);
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            updatePlayerDimension((EntityPlayer) event.getEntity());
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        updatePlayerDimension(event.player);
    }
}
