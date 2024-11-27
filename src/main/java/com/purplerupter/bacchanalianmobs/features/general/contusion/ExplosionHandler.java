package com.purplerupter.bacchanalianmobs.features.general.contusion;

import com.purplerupter.bacchanalianmobs.features.DefaultSettings;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.Tick.contusionTimer;
import static com.purplerupter.bacchanalianmobs.features.general.contusion.SafeLocationFinder.findSafeLocation;

public class ExplosionHandler {

    private static final int MAX_DURATION = DefaultSettings.MAX_DURATION; // in ticks

    private static final byte MAX_DISTANCE = DefaultSettings.MAX_DISTANCE; // max distance for mirage
    private static byte playerHeight = 2; // может быть, некоторые моды могут изменить размеры игрока...
    private static byte playerWidth = 1;
    private static byte maxHeightDif = (byte)(playerHeight * 2);

    private static final List<String> contusionMobs = new ArrayList<>();
    private static final Map<String, Float> contusionMobsMultipliers = new HashMap<>();
    private static final Map<String, Integer> contusionMobsMaxDuration = new HashMap<>();
    private static final Map<String, Byte> contusionMobsMaxDistance = new HashMap<>();

    @SubscribeEvent
    public void explosionHurt(LivingHurtEvent event) {
        if (event.getSource().damageType.equals("explosion.player") || event.getSource().damageType.equals("explosion")) {
//            String mobID = EntityList.getKey(event.getEntity()).toString(); // TODO
//            int duration = (int)(event.getAmount() / 3 * contusionMobsMultipliers.get(mobID)) * 20; // in ticks // TODO
//            int duration = (int)(event.getAmount() / 3 * 1) * 20; // in ticks
            int duration = (int)(event.getAmount()) * 20; // in ticks

            if (event.getEntity() instanceof EntityPlayer) {
                ((EntityPlayer) event.getEntity()).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("minecraft:nausea"), duration));
            } else {
//                if (contusionMobs.get(mobID)) { // TODO
                String entityID = EntityList.getKey(event.getEntity()).toString();
                if (contusionMobs.contains(entityID)) {

                    if ( ((EntityLiving) event.getEntity()).getAttackTarget() != null ) {

                        FakePlayer mirage = FakePlayerFactory.getMinecraft((WorldServer) event.getEntity().world);
                        mirage.setCustomNameTag("ContusionMirage");
                        BlockPos miragePos = findSafeLocation(((EntityCreature) event.getEntity()), contusionMobsMaxDistance.get(entityID), maxHeightDif);
                        if (miragePos == null) {
                            System.out.println("Cannot find lawful BlockPos nearby! Set mirage pos on the player pos.");
                            miragePos = new BlockPos(((EntityLiving) event.getEntity()).getAttackTarget());
                        }
                        mirage.setPosition(miragePos.getX(), miragePos.getY(), miragePos.getZ());
                        ((EntityLiving) event.getEntityLiving()).setAttackTarget(mirage);
                        contusionTimer.put( ((EntityLiving) event.getEntity()), contusionMobsMaxDuration.get(entityID) );
                    }
                }

            }

        }
        
    }

    public static void addMobToContusion(String entityID, float durationMultiplier, int maxDuration, byte maxDistance) {
        if (contusionMobs.isEmpty() || !contusionMobs.contains(entityID)) {
            contusionMobs.add(entityID);
            contusionMobsMultipliers.put(entityID, durationMultiplier);
            contusionMobsMaxDuration.put(entityID, maxDuration);
            contusionMobsMaxDistance.put(entityID, maxDistance);
        }
    }
}
