package com.purplerupter.bacchanalianmobs.features.creeper;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class ChargedCreeper {

    private Random random = new Random();
    private byte chance = 20;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMobSpawn(LivingSpawnEvent event) {
        if (event.getWorld().isRemote) { return; }
        if (!(event.getEntity() instanceof EntityCreeper)) { return; }

        if (random.nextInt(100) <= chance) {
            EntityCreeper creeper = (EntityCreeper) event.getEntity();
            NBTTagCompound nbt = new NBTTagCompound();
            creeper.writeEntityToNBT(nbt);
            nbt.setBoolean("powered", true);
            creeper.readEntityFromNBT(nbt);
        }
    }
}
