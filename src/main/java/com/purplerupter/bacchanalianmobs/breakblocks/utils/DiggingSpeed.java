package com.purplerupter.bacchanalianmobs.breakblocks.utils;

import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class DiggingSpeed {
    public static float getDiggingSpeed(final EntityLivingBase entity, final World world, final BlockPos pos) {
        final ItemStack heldItem = entity.getHeldItem(EnumHand.MAIN_HAND);
        float f = heldItem.isEmpty() ? 1.0f : heldItem.getDestroySpeed(world.getBlockState(pos));

        if (f > 1.0f) {
            final short i = (short) EnchantmentHelper.getEfficiencyModifier(entity);
            if (i > 0 && !heldItem.isEmpty()) { f += i * i + 1; }
        }

        if (entity.isPotionActive(MobEffects.HASTE)) {
            f *= 1.0f + (entity.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2f;
        }

        if (entity.isPotionActive(MobEffects.MINING_FATIGUE)) {
            float f2;
            switch (entity.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0: { f2 = 0.3f; break; }
                case 1: { f2 = 0.09f; break; }
                case 2: { f2 = 0.0027f; break; }
                default: { f2 = 0.00002f; break; }
            }
            f *= f2;
        }

        if (entity.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(entity)) { f /= 5.0f; }
        if (!entity.onGround) { f /= 5.0f; }
        if (debug) { System.out.println("The digging speed is: " + f); }
        return (f < 0.0f) ? 0.0f : f;
    }
}
