package com.purplerupter.bacchanalianmobs.etc.events;

import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.items.guns.GenericGun;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.AddDistanceToNBT.addDistanceToNBT;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class HandleEntityAttack {
    public static final byte recentEntitiesCount = 5;
    private static float[] recentDistancesList = new float[recentEntitiesCount];
    private static float[] recentWidthList = new float[recentEntitiesCount];
    private static byte index = 0;

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {

        Entity source = event.getSource().getTrueSource();
        if (debug) { System.out.println(source); }
        if (debug) { System.out.println(event.getSource()); }
        Entity target = event.getEntity();
        if (debug) { System.out.println(target); }
        if (source == null || target == null) {
            if (debug) { System.out.println("Source is null or target is null. Cancel event handling."); }
            return;
        }

        // Techguns
        if (BacchanalianMobs.techgunsIntegration) {
//            System.out.println("1");

            if (source instanceof EntityPlayerMP) { // 'instanceof EntityPlayerMP' ???
                EntityPlayerMP player = (EntityPlayerMP) source;
//                System.out.println("2");
                if (player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof GenericGun
                        || player.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof GenericGun) { // Потенциальный баг: игрок атакует ближним оружием в левой руке, держа пистолет в правой руке
                    if (debug) { System.out.println("Player attack an entity with a gun."); }

                    float distance = source.getDistance(target); // how about getDistanceSq()???
                    if (debug) { System.out.println("The attack distance is: " + distance); }
                    recentDistancesList[index] = distance;

                    float width = target.width;
                    if (debug) { System.out.println("The attacked entity width is: " + width); }
                    recentWidthList[index] = width;

                    index++;
                    if (index >= recentEntitiesCount) {
                        index = 0;
                    }

                    addDistanceToNBT((EntityPlayerMP) source, recentDistancesList, recentWidthList);
                }
            } else {
                if (source instanceof EntityPlayerSP) {
                    if (debug) { System.out.println("EntityPlayerSP..."); }
                }
            }
        }

    }
}
