package com.purplerupter.bacchanalianmobs.etc.utils;

import com.oblivioussp.spartanweaponry.item.*;
import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

public class IsWeaponOrTool {
    public static boolean weaponOrTool(Item item) {
        if (BacchanalianMobs.spartanWeaponryIntegration) {
            // Too many Spartan Weaponry stuff
            // TODO add ranged shit-weapon
            return (item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemBattleaxe || item instanceof ItemDagger
                    || item instanceof ItemGreatsword || item instanceof ItemKatana || item instanceof ItemLongsword || item instanceof ItemRapier
                    || item instanceof ItemSaber || item instanceof ItemCaestus || item instanceof ItemClub || item instanceof ItemHammer
                    || item instanceof ItemMace || item instanceof ItemQuarterstaff || item instanceof ItemWarhammer || item instanceof ItemGlaive
                    || item instanceof ItemHalberd || item instanceof ItemLance || item instanceof ItemPike || item instanceof ItemSpear
                    || item instanceof ItemThrowingKnife || item instanceof ItemThrowingAxe || item instanceof ItemScythe || item instanceof ItemParryingDagger);
        } else {
            return (item instanceof ItemSword || item instanceof ItemTool);
        }
    }
}
