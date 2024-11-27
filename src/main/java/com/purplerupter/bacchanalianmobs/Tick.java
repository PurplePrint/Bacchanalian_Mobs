package com.purplerupter.bacchanalianmobs;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.srp.EvolutionPhaseTracker.dimensionPhases;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.srp.EvolutionPhaseTracker.onPhaseChange;
import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.updatePlayerTime;
import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.updateTimeSpent;
import static com.purplerupter.bacchanalianmobs.features.spider.web.TempWebDatabase.*;

public class Tick {

    // Интервалы: вместо того чтобы выполнять ресурсоёмкое действие каждый тик, можно выполнять его раз в несколько тиков.
    // Time units is ticks
//    private short timer = 0;
    private short timerDimensions = 0;
    private short timerSRP = 0;
    private final short intervalEvolutionPhase = 40;
    private final short intervalDimensionTimeTracker = 40;
    private final byte shift = 20; // Задержка между выполнением разных действий. В теории, должно более равномерно распределить нагрузку вместо единовременного пика. Экономия на спичках.
//    private byte tasks = 0;
//    private final byte totalTasks = 2;

    public static final Map<EntityLiving, Integer> contusionTimer = new HashMap<>();

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote) { return; }

        timerDimensions++;
//        timerSRP++;
//        if ((timerDimensions / 2) >= shift || timerSRP > 0) {
        if (timerDimensions >= shift || timerSRP > 0) {
            timerSRP++;
        }

        // Time spent in dimensions
//        if ((timerDimensions / 2) >= intervalDimensionTimeTracker) {
        if (timerDimensions >= intervalDimensionTimeTracker) {
//            System.out.println((timerDimensions / 2));
//            System.out.println(timerDimensions);

            World world = event.world;
            for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
//                updatePlayerTime(player, (short)(timerDimensions / 2));
                updatePlayerTime(player, timerDimensions);
            }

            timerDimensions = 0;
        }

        // Evolution phase tracker (SRParasites)
//        if ((timerSRP / 2) >= intervalEvolutionPhase) {
        if (timerSRP >= intervalEvolutionPhase) {
            if (BacchanalianMobs.srParasitesIntegration) {

                World world = event.world;
                short dimensionId = (short) world.provider.getDimension();

                byte curPhase = SRPSaveData.get(world).getEvolutionPhase(dimensionId);

                if (dimensionPhases.containsKey(dimensionId)) {
                    byte previousPhase = dimensionPhases.get(dimensionId);
                    if (curPhase != previousPhase) {
                        onPhaseChange(world, dimensionId, previousPhase, curPhase);
                    }
                }

                dimensionPhases.put(dimensionId, curPhase);
            }

            timerSRP = 0;
        }

        long worldAge = event.world.getTotalWorldTime();

        // Features
        // TODO from config
        if (WEB_DATABASE_EXIST) {
            for (short dim : getTempWebDatabase().keySet()) {
                for (Map< Map<Integer, Long>, BlockPos > entry : getTempWebDatabase().get(dim)) {

                    for (Map<Integer, Long> map : entry.keySet()) {
                        int lifeSpan = map.keySet().stream().findFirst().orElse(null);
                        long timeStamp = map.values().stream().findFirst().orElse(null);

                        if (worldAge >= lifeSpan + timeStamp) {
//                            System.out.println("Try to destroy block at pos: " + entry.get(map) + " in world: " + event.world);
//                            event.world.destroyBlock(entry.get(map), false); // Sound played, it's unnecessary
                            event.world.setBlockToAir(entry.get(map));
//                            if (event.world.getBlockState(entry.get(map)).getBlock() instanceof BlockWeb) {

//                            removeTempWebEntry(dim, entry, getTempWebDatabase().get(dim).indexOf(entry));
                            removeTempWebEntry(dim, getTempWebDatabase().get(dim).indexOf(entry));
                        }
                    }
                }
            }
        }

        if (!contusionTimer.isEmpty()) {
            for (EntityLiving entity : contusionTimer.keySet()) {
                contusionTimer.replace(entity, contusionTimer.get(entity) - 1);
                if (contusionTimer.get(entity) <= 0) {

                    entity.setAttackTarget(null);
                    contusionTimer.remove(entity);
                }
            }
        }

    }
}
