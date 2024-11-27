/*

    Calculating firearm DPS is CRAZY hard.
    You have to factor in accuracy and ammo count, which is a lot of work.

 */

package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import techguns.items.guns.GenericGun;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.GunsDamageConfig.getAverageDamage;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.RangeAndWidth.getAverageFiringRange;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.RangeAndWidth.getAverageMobWidth;

public class ProcessTechgunsDPS {

    private static final float INCENDIARY_BONUS = 1.1F;
//    private static final float INCENDIARY_BONUS_CUSTOM = 1F;
    private static final float EXPLOSIVE_BONUS = 1.15F;
//    private static final float EXPLOSIVE_BONUS_CUSTOM = 1F;

    public static double getTechgunsDPS(ItemStack itemStack, EntityPlayerMP player) {
        String gunID = itemStack.getItem().getRegistryName().toString();
        if (debug) { System.out.println("The gun is: " + gunID); }
        GenericGun gun = (GenericGun) itemStack.getItem();
        float damage;

        // Average distance for recent 5 mobs attacked by player
        float averageDistance = getAverageFiringRange(player);
        if (debug) { System.out.println("Average attack distance (by recent 5 attacking entities) for player " + player.getName() + " is: " + averageDistance); }

        // Average damage for this gun by average distance
        float damageByDistance = getAverageDamage(gunID, averageDistance);
        if (debug) { System.out.println("Damage by distance for this gun is: " + damageByDistance); }

        // Average width for recent 5 mobs attacked by player
        float averageWidth = getAverageMobWidth(player);
        if (debug) { System.out.println("Average width of 5 recent attacked mobs for player " + player.getName() + " is: " + averageWidth); }

        // Accuracy multiplier
        // ПРЕДПОЛОЖИМ (мда...) что 100% точность обеспечена на расстоянии 10 блоков до моба шириной 1 блок
        float accuracy = averageDistance / averageWidth;
        damage = (damageByDistance / (accuracy / 2)) > 0 ? damageByDistance / (accuracy / 2) : damageByDistance; // делю на 2, чтобы низкая точность не так сильно снижала значение DPS
        if (debug) { System.out.println("Accuracy factor multiply damage to: " + damage); }

        // Ammo variant multiplier
        String var = gun.getCurrentAmmoVariantKey(itemStack);
        if (debug) { System.out.println("The gun's ammo variant is: " + var); }
        if (var.equals("incendiary")) {
            damage *= INCENDIARY_BONUS;
        }
        if (var.equals("explosive")) {
            damage *= EXPLOSIVE_BONUS;
        }
        if (debug) { System.out.println("Now damage is: " + damage); }

        return damage;
    }

}
