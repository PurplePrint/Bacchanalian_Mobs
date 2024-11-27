package com.purplerupter.bacchanalianmobs.features.spider.web;

import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.purplerupter.bacchanalianmobs.features.DefaultSettings.TMP_WEB_LIFESPAN;
import static com.purplerupter.bacchanalianmobs.features.spider.web.BlockBehind.getDirectionBehind;
import static com.purplerupter.bacchanalianmobs.features.spider.web.TempWebDatabase.addTempWeb;
import static com.purplerupter.bacchanalianmobs.features.spider.web.Utils.lawfulCollision;
import static com.purplerupter.bacchanalianmobs.features.spider.web.Utils.lawfulGravity;

public class WebOnAttack {
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().world.isRemote) { return; }
        if (event.getEntity() instanceof EntitySpider) {
            System.out.println("WebOnAttack!");
            EntitySpider spider = (EntitySpider) event.getEntity();
            Vec3d sight = spider.getLookVec();
            System.out.println("Spider: " + spider);
            System.out.println("Sight: " + sight);

            placeWebBehind(spider,
                    (int)sight.x - (int)spider.posX,
                    (int)sight.y - (int)spider.posY,
                    (int)sight.z - (int)spider.posZ);
            return;
        }

        if (event.getSource().getTrueSource() instanceof EntitySpider) {
            System.out.println("WebOnAttack!");
            EntitySpider spider = (EntitySpider) event.getSource().getTrueSource();
            Vec3d sight = spider.getLookVec();
            System.out.println("Spider: " + spider);
            System.out.println("Sight: " + sight);

            placeWebBehind(spider,
                    (int)sight.x - (int)spider.posX,
                    (int)sight.y - (int)spider.posY,
                    (int)sight.z - (int)spider.posZ);
            return;
        }
    }

    private static void placeWebBehind(EntitySpider spider, int difX, int difY, int difZ) {
        System.out.println("Place web behind: " + spider + " // " + difX + ", " + difY + ", " + difZ);
        BlockPos behindRelative = getDirectionBehind(difX, difY, difZ);
        System.out.println("Relative behind is: " + behindRelative);
        BlockPos actualBehind = spider.getPosition().add
                (behindRelative.getX(), behindRelative.getY(), behindRelative.getZ());
        System.out.println("Actual behind BlockPos is: " + actualBehind);

        if (lawfulCollision(spider.getEntityWorld(), actualBehind)) {
            System.out.println("Lawful collision!");
            if (lawfulGravity(spider.getEntityWorld(), actualBehind)) {
                System.out.println("Lawful gravity!");
//                            System.out.println("Shift by X: " + this.shiftX + " // Shift by Y: " + this.shiftY + " // Shift by Z: " + this.shiftZ);
                spider.world.setBlockState(actualBehind, Blocks.WEB.getDefaultState());
                long worldAge = spider.world.getTotalWorldTime();
                int lifeSpan = TMP_WEB_LIFESPAN;
                if (spider.getEntityData().hasKey("TempWebLifespan")) {
                    lifeSpan = spider.getEntityData().getInteger("TempWebLifespan"); }
                addTempWeb((short) spider.dimension, actualBehind, lifeSpan, worldAge);
            }
        }
    }
}
