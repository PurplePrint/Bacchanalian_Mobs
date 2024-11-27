package com.purplerupter.bacchanalianmobs.breakblocks.utils;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class MultipliedBlockHardness {
    public static float getMultipliedBlockHardness(float origHardness, JsonObject configObject, IBlockState state, EntityLivingBase entity) {
        if (!configObject.get("Break soft blocks").getAsBoolean()) {
            return origHardness;
        }

        Block block = state.getBlock();
        String requiredTool = block.getHarvestTool(state);
        int requiredHarvestLevel = block.getHarvestLevel(state);
        if (debug) { System.out.println("The block is: " + block + " // The requiredTool is: " + requiredTool + " // The requiredHarvestLevel is: " + requiredHarvestLevel); }

        boolean isSoftBlock = requiredTool == null;
        if (debug && isSoftBlock) { System.out.println("This is a soft block!"); }

        // Определяем, держит ли моб подходящий инструмент
        boolean hasEffectiveTool = isEffectiveToolForBlock(entity.getHeldItemMainhand(), block, state);

        // Логика для мягких блоков
        if (isSoftBlock) { // Если блок не требует инструмента (мягкий блок)
            float softMultiplier;
            if (hasEffectiveTool) {
                softMultiplier = configObject.get("Soft multiplier").getAsFloat();
            } else {
                softMultiplier = configObject.get("Soft multiplier (no tool)").getAsFloat();
            }
            float multipliedHardness = origHardness * (1 / softMultiplier);
            if (debug) { System.out.println("The multiplied hardness is: " + multipliedHardness); }
            return multipliedHardness;
        }

        // Проверяем, можно ли ломать каменные блоки
        if (!configObject.get("Break stone blocks").getAsBoolean()) {
            return origHardness; // Если ломать каменные блоки запрещено, возвращаем оригинальную скорость
        }

        // Логика для блоков, требующих инструмента (например, каменные блоки)
        float stoneMultiplier;
        if (hasEffectiveTool) {
            stoneMultiplier = configObject.get("Stone multiplier").getAsFloat();
        } else {
            stoneMultiplier = configObject.get("Stone multiplier (no tool)").getAsFloat();
        }

        float multipliedHardness = origHardness * (1 / stoneMultiplier);
        if (debug) { System.out.println("The multiplied hardness is: " + multipliedHardness); }
        return multipliedHardness;
    }

    /**
     * Метод для проверки, является ли инструмент в руке эффективным для добычи данного блока.
     */
    public static boolean isEffectiveToolForBlock(ItemStack heldItem, Block block, IBlockState state) {
        if (heldItem.isEmpty()) {
            return false; // Если предмета в руке нет, инструмент не эффективен
        }

        // Проверяем, является ли предмет киркой
        if (heldItem.getItem() instanceof ItemPickaxe) {
            ItemPickaxe pickaxe = (ItemPickaxe) heldItem.getItem();
            int toolLevel = pickaxe.getHarvestLevel(heldItem, "pickaxe", null, state);
            int blockHarvestLevel = block.getHarvestLevel(state);

            return toolLevel >= blockHarvestLevel;
        }

        return false;
    }

}
