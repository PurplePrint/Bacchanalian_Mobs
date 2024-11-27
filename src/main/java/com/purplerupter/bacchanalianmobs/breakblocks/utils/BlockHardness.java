package com.purplerupter.bacchanalianmobs.breakblocks.utils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.breakblocks.utils.DiggingSpeed.getDiggingSpeed;

public class BlockHardness {
    public static float getBlockHardness(final EntityLivingBase entity, final World world, final BlockPos pos) {
        final float hardness = world.getBlockState(pos).getBlockHardness(world, pos);

        if (hardness <= 0.0f) { return 0.0f; }

        return getDiggingSpeed(entity, world, pos) / hardness;
    }
}
