package com.purplerupter.bacchanalianmobs.features.spider.web;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityWebSnowball extends EntitySnowball {

    public EntityWebSnowball(World worldIn, EntityLivingBase throwerIn) {
        super(worldIn, throwerIn);
//        System.out.println("Web Snowball!");
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (result.entityHit instanceof EntityLivingBase) {
            BlockPos pos = new BlockPos(result.entityHit.posX, result.entityHit.posY, result.entityHit.posZ);
            World world = result.entityHit.getEntityWorld();

            if (world.isAirBlock(pos)) {
                world.setBlockState(pos, Blocks.WEB.getDefaultState());
            }
        }

        this.setDead();
    }
}
