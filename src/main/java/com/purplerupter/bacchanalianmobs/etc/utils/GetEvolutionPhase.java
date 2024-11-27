package com.purplerupter.bacchanalianmobs.etc.utils;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class GetEvolutionPhase {


    public static byte getCurrentPhase(Entity entity) {
        if (BacchanalianMobs.srParasitesIntegration) { return getCurrentPhase(entity, true); } else {
            if (debug) { System.out.println("SRParasites integration disabled, return the default value (0)"); }
            return 0;
        }
    }

    public static byte getCurrentPhase(EntityLiving entity) {
        return getCurrentPhase((Entity) entity);
    }

    public static byte getCurrentPhase(Entity entity, boolean srpIntegration) {
        World world = entity.getEntityWorld();
        MinecraftServer server = entity.getEntityWorld().getMinecraftServer();
        if (server != null && !world.isRemote) {
            byte phase = (SRPSaveData.get(world).getEvolutionPhase(world.provider.getDimension()));
            if (debug) { System.out.println("Current SRP phase in the " + world + " world is: " + phase); }
            return (phase); // the minimum phase is -2
        } else {
            if (debug) { System.out.println("Error! The entity world is client! Can't get SRP phase for this entity!"); }
            return 0;
        }
    }
}
