package com.purplerupter.bacchanalianmobs.content.items.utils;

import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class HealthBoostUtils {
    public static void healthBooster(World world, EntityPlayer player, ItemStack stack, byte healthBoost) {
        world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                0.5f, 1.0f + 0.1f * (float) BacchanalianMobs.random.nextGaussian());

        stack.shrink(1);

        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (attribute != null) {
            float oldHealth = player.getMaxHealth();
            attribute.setBaseValue(oldHealth + healthBoost);
        } else {
//            System.out.println("Error! Player MAX_HEALTH attribute is null!!!");
        }
    }

    public static void healPlayer(EntityPlayer player, float currentHP, float maxHP) {
        float threshold = maxHP / 4 * 3;
        if (currentHP < (threshold)) {
            player.heal(threshold);
        }

        player.addPotionEffect(new PotionEffect(
//                Potion.getPotionFromResourceLocation("minecraft:regeneration"), 1200, 3, false, true
                MobEffects.REGENERATION, 1200, 3, false, true
        ));
    }
}
