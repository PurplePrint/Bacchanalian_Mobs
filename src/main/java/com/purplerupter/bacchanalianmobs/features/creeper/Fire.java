package com.purplerupter.bacchanalianmobs.features.creeper;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class Fire {
    private static final Field IS_FLAMING_FIELD = ReflectionHelper.findField(net.minecraft.world.Explosion.class, "isFlaming", "field_77286_a");

    @SubscribeEvent
    public void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getExplosivePlacedBy() instanceof EntityCreeper) {
            EntityLivingBase entity = event.getExplosion().getExplosivePlacedBy(); // TODO: Conditions check
            try {
                IS_FLAMING_FIELD.setBoolean(event.getExplosion(), true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
