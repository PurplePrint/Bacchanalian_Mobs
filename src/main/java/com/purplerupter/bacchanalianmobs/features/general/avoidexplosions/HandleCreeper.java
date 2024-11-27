package com.purplerupter.bacchanalianmobs.features.general.avoidexplosions;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.purplerupter.bacchanalianmobs.features.general.avoidexplosions.MobsInArea.callMobsInArea;

public class HandleCreeper {
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote) { return; }

        if (event.getEntity() instanceof EntityCreeper) {
            EntityCreeper creeper = (EntityCreeper) event.getEntity();

            if (creeper.getCreeperState() == 1) {
                creeper.getEntityData().setBoolean("kamikazeCreeper", true);
                callMobsInArea(creeper, creeper.getEntityWorld(), 40);
            }

            if (creeper.getCreeperState() == 0) {
                creeper.getEntityData().removeTag("kamikazeCreeper");
            }
        }
    }
}
