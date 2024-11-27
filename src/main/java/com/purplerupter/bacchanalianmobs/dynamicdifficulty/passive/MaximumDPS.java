package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive;

import com.google.common.collect.Multimap;
import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import techguns.items.GenericItemShared;
import techguns.items.guns.GenericGun;
import techguns.items.guns.GenericGunCharge;
import techguns.items.guns.GuidedMissileLauncher;
import techguns.items.guns.SonicShotgun;

import java.util.*;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.GunsDamageConfig.*;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns.ProcessTechgunsDPS.getTechgunsDPS;
import static com.purplerupter.bacchanalianmobs.etc.utils.IsWeaponOrTool.weaponOrTool;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class MaximumDPS {

    private static final ArrayList<Double> dpsListOfAllItems = new ArrayList<>();

    private static List<String> allMagazines = new ArrayList<>();
    private static Map<String, Integer> roundsInInventory = new HashMap<>();

    public static double getMaximumDPSValue(EntityPlayer player) {
        if (BacchanalianMobs.techgunsIntegration) {
            // Ammo first
            getAmmoCount(player);
        }
        getItemWithMaximumDPS(player);

        if (dpsListOfAllItems.isEmpty()) {
            return 0.0;
        }

        double maximumDPS = Collections.max(dpsListOfAllItems);
        dpsListOfAllItems.clear();
        return maximumDPS;
    }

    private static void getItemWithMaximumDPS(EntityPlayer player) {
        for (ItemStack itemStack : player.inventory.mainInventory) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof Item) {

                double amount = getDPS(itemStack, player);
                dpsListOfAllItems.add(amount);
            }
        }
    }

    private static void getAmmoCount(EntityPlayer player) {
        for (ItemStack itemStack : player.inventory.mainInventory) {
            if (!itemStack.isEmpty()) {

                if (itemStack.getItem() instanceof GenericItemShared) {
                    getConfigurations(); // init the magazineIDsForGun
                    GenericItemShared tgItem = (GenericItemShared) itemStack.getItem();
                    String slot = tgItem.getSlot(itemStack).toString();
                    if (debug) { System.out.println("The slot is: " + slot); }
                    if (slot.equals("Ammo")) {
                        if (debug) { System.out.println("This is ammo slot"); }

                        String itemID = itemStack.getItem().getRegistryName().toString() + ":" + itemStack.getMetadata();
                        if (debug) { System.out.println("Item ID with meta is: " + itemID); }
                        for (String key : magazineIDsForGun.keySet()) {
                            short magsCount = 0;
                            for (String mag : magazineIDsForGun.get(key)) {
                                if (debug) { System.out.println(mag); }
                                if (mag.equals(itemID)) {
                                    if (debug) { System.out.println("Match! This item is magazine for that gun."); }
                                    magsCount = (short) itemStack.getCount();
                                }
                            }
                            int rounds = (magsCount * roundsAmountForGun.get(key));
                            if (debug) { System.out.println("Rounds count: " + rounds); }
                            roundsInInventory.put(key, rounds);
                        }

                    }
                }

            }
        }
    }

    private static double getDPS(ItemStack itemStack, EntityPlayer player) {

        // Techguns
        if (BacchanalianMobs.techgunsIntegration) {
            if (itemStack.getItem() instanceof GenericGun
                    || itemStack.getItem() instanceof GenericGunCharge
                    || itemStack.getItem() instanceof GuidedMissileLauncher
                    || itemStack.getItem() instanceof SonicShotgun) {

                if (debug) { System.out.println("Gun item detected: " + itemStack + " // " + itemStack.getItem()); }
                String gunID = itemStack.getItem().getRegistryName().toString();

                // DPS multiplier by ammo count
                GenericGun gun = (GenericGun) itemStack.getItem();
                short ammoLeft = (short) gun.getAmmoLeft(itemStack);
                System.out.println("Ammo left in the gun is: " + ammoLeft);
                if (debug) { System.out.println(roundsInInventory); }
                int totalAmmo = roundsInInventory.get(gunID) + ammoLeft;
                if (debug) { System.out.println("Rounds total count for this gun: " + totalAmmo); }

//                return getTechgunsDPS(itemStack, (EntityPlayerMP) player);
                double DPS = getTechgunsDPS(itemStack, (EntityPlayerMP) player);

                // Decrease DPS if inventory ammo is too low
                short roundsInMag = roundsAmountForGun.get(gunID);
                if (debug) { System.out.println("This gun can have maximum " + roundsInMag + " in its magazine"); }

                float difference = (float)totalAmmo / (float)roundsInMag;
                if (difference < 3) {
                    boolean passed = false;
                    if (difference <= 0.09) {
                        DPS *= 0.1;
                        passed = true;
                    }
                    if (difference <= 0.5 && !passed) {
                        DPS *= 0.2;
                    }
                    if (difference <= 2 && !passed) {
                        DPS *= 0.5;
                    }
                }

                if (debug) { System.out.println("The DPS for gun " + gun + " is: " + DPS); }
                return DPS;
            }
        }

        Multimap<String, AttributeModifier> map = itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);

        double attackSpeed = 4.0;
        double damage = 1.0;
        if (map.containsKey("generic.attackSpeed")) {
            attackSpeed += sumAllAndReturn(map.get("generic.attackSpeed"));
        }
        if (map.containsKey("generic.attackDamage")) {
            damage += sumAllAndReturn(map.get("generic.attackDamage"));
        }
        double actualSpeedInSeconds = Math.ceil((20 / attackSpeed)) / 20.0;

        if (isWeaponOrTool(itemStack.getItem())) {
            return damage / actualSpeedInSeconds;
        } else {
            return 1.0 / actualSpeedInSeconds;
        }
    }

    private static boolean isWeaponOrTool(Item item) {
        return weaponOrTool(item);
    }

    private static double sumAllAndReturn(Collection<AttributeModifier> col) {
        return col.stream().map(AttributeModifier::getAmount).reduce(0.0D, Double::sum);
    }
}
