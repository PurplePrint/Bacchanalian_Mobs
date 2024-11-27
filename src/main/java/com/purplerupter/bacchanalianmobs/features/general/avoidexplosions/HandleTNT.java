package com.purplerupter.bacchanalianmobs.features.general.avoidexplosions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.features.general.avoidexplosions.MobsInArea.callMobsInArea;

public class HandleTNT {

    public static final double AVOID_RADIUS = 10.0D; // Радиус, в котором мобы убегают
    private static int fuse = 0;

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) { return; }

        if (event.getEntity() instanceof EntityTNTPrimed) {
//            System.out.println("Entity TNT primed!");
            Entity explosive = event.getEntity();
            World world = explosive.world;

            fuse = ((EntityTNTPrimed) explosive).getFuse(); // Устанавливаем задержку для TNT (в тиках)
//            System.out.println("Fuse: " + fuse);

            // Создание области поиска вокруг взрыва
            callMobsInArea(explosive, world, fuse);
        }
    }
}
