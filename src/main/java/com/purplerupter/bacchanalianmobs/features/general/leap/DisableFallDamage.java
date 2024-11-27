package com.purplerupter.bacchanalianmobs.features.general.leap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class DisableFallDamage {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityHurt(LivingHurtEvent event) {
        if (event.getEntity().world.isRemote) { return; }
        if (!(event.getEntity() instanceof EntityLiving)) { return; }
        if ( !(event.getSource().equals(DamageSource.FALL)) ) {
//            System.out.println("This is not fall damage! This is: " + event.getSource()); return; }
            return; }

        // TODO проверка соответствия моба конфигу рывка, чтобы не проверять лишних мобов

        EntityLiving mob = (EntityLiving) event.getEntity();

        for (EntityAITasks.EntityAITaskEntry entry : mob.tasks.taskEntries) {
            if (entry.action instanceof LeapAI) {
                LeapAI leapTask = (LeapAI) entry.action;
                if (leapTask.performLeap) {
                    event.setCanceled(true);
                    if (debug) { System.out.println("Cancel fall damage for a leaping mob"); }
                }
            }
        }
    }
}
