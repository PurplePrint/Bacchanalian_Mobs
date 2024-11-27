package com.purplerupter.bacchanalianmobs.etc.init;

import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import com.purplerupter.bacchanalianmobs.transport.guardian.EntityGuardianBoat;
import com.purplerupter.bacchanalianmobs.transport.parrot.EntityTransportParrot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class InitEntities {
    public static void initEntities() {
        int entityID = 0;

        EntityRegistry.registerModEntity(new ResourceLocation(BacchanalianMobs.MODID, "guardianBoat"), EntityGuardianBoat.class, "mobBoat",
                ++entityID, BacchanalianMobs.instance, 64, 3, true);

        EntityRegistry.registerModEntity(new ResourceLocation(BacchanalianMobs.MODID, "transportParrot"), EntityTransportParrot.class, "transportParrot",
                ++entityID, BacchanalianMobs.instance, 64, 3, true);
    }
}
