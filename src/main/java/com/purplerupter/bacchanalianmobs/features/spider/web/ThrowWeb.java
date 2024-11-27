package com.purplerupter.bacchanalianmobs.features.spider.web;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.world.World;

import java.util.Random;

public class ThrowWeb extends EntityAIBase {
    private final EntitySpider spider;
    private final World world;
    private EntityLivingBase target;

    private final Random random;
    private final byte chance;

    private short timer;
    private final short interval;

    private final short maxDistanceToThrow;

    private float velocity;
    private float inaccuracy;

    public ThrowWeb(EntitySpider spider, World world, byte chance, short maxDistanceToThrow, float velocity, float inaccuracy) {
        this.spider = spider;
        this.world = world;
        this.random = new Random();
        this.chance = chance;

        this.timer = 0;
        this.interval = 30;

        this.maxDistanceToThrow = maxDistanceToThrow > 0 ? maxDistanceToThrow : Short.MAX_VALUE;

        this.velocity = velocity;
        this.inaccuracy = inaccuracy;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return true;
    }

    @Override
    public void updateTask() {
        this.timer++;
        if (this.timer >= this.interval) {
            this.timer = 0;

            if (this.spider.getAttackTarget() != null) {
                this.target = this.spider.getAttackTarget();
                if (lawful()) {
                    if (this.spider.canEntityBeSeen(this.target)
                            && this.spider.getDistanceSq(this.target) <= (this.maxDistanceToThrow * this.maxDistanceToThrow)) {

                        byte rnd = (byte) this.random.nextInt(100);
                        if (rnd <= this.chance) {
                            throwWeb();
                        }
                    }
                }
            }

        }
    }

    private boolean lawful() {
        return !this.spider.isInWater() && !this.spider.isInLava()
                && !this.spider.isBurning()
                && !this.spider.isAirBorne
                && !this.spider.isDead;
    }

    private void throwWeb() {
        EntityWebSnowball snowball = new EntityWebSnowball(this.world, this.spider);

        double dx = this.target.posX - this.spider.posX;
        double dy = this.target.posY + this.target.getEyeHeight() - snowball.posY;
        double dz = this.target.posZ - this.spider.posZ;

        snowball.shoot(dx, dy, dz, this.velocity, this.inaccuracy);
        this.world.spawnEntity(snowball);
    }
}
