package com.purplerupter.bacchanalianmobs.features.spider.web;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.FindBlockNearby.isBlockNearby;
import static com.purplerupter.bacchanalianmobs.features.spider.web.BlockBehind.getDirectionBehind;
import static com.purplerupter.bacchanalianmobs.features.spider.web.TempWebDatabase.addTempWeb;
import static com.purplerupter.bacchanalianmobs.features.spider.web.Utils.lawfulCollision;
import static com.purplerupter.bacchanalianmobs.features.spider.web.Utils.lawfulGravity;

public class WebPlacement extends EntityAIBase {

    private final EntitySpider spider;
    private final World world;

    private int timer1;
    private int timer2;
    private static final int wait = 20;
    private static final byte offset = 6;

    private BlockPos prevPos;
    private BlockPos currentPos;

    private byte webSearchRadius;
    private int lifeSpan;

    private byte shiftX;
    private byte shiftY;
    private byte shiftZ;

    public WebPlacement(EntitySpider spider, byte searchRadius, int lifeSpan) {
        if (debug) { System.out.println("EntitySpider called for entity: " + spider); }
        this.spider = spider;
        this.world = spider.getEntityWorld();

        this.webSearchRadius = searchRadius;
        this.lifeSpan = lifeSpan;

        this.timer1 = 0;
        this.timer2 = 0;

        this.shiftX = 0;
        this.shiftY = 0;
        this.shiftZ = 0;
    }

    @Override
    public boolean shouldExecute() {
        return true;
    }

    @Override
    public void startExecuting() {
        this.spider.getEntityData().setInteger("TempWebLifespan", this.lifeSpan);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.timer1 >= this.wait) {
            if (incorrectState()) {
                return false; }

            if (this.spider.getAttackTarget() == null) {
                return false; }

            this.timer1 = 0;
        }

        return true;
    }

    @Override
    public void updateTask() {
        this.timer1++;
        this.timer2++;

        if (this.timer2 >= (wait + offset)) {
            if (this.spider.getAttackTarget() != null) {

                int sX = (int) this.spider.posX;
                int sY = (int) this.spider.posY;
                int sZ = (int) this.spider.posZ;
//                System.out.println("Spider XYZ is: " + sX + ", " + sY + ", " + sZ);

                this.prevPos = this.currentPos != null ? this.currentPos : new BlockPos(sX, sY, sZ);
                this.currentPos = new BlockPos(sX, sY, sZ);
//                System.out.println("Prev pos is: " + prevPos);
//                System.out.println("Current pos is: " + currentPos);

                if (!isWebNearby(sX, sY, sZ)) {
                    // Place a web block
                    getDirection();
                    BlockPos pos = this.spider.getPosition().add(this.shiftX, this.shiftY, this.shiftZ);

                    if (lawfulCollision(spider.getEntityWorld(), pos)) {
                        if (lawfulGravity(spider.getEntityWorld(), pos)) {
//                            System.out.println("Shift by X: " + this.shiftX + " // Shift by Y: " + this.shiftY + " // Shift by Z: " + this.shiftZ);
                            this.world.setBlockState(pos, Blocks.WEB.getDefaultState());
//                            this.world.setBlockState(pos, TEMPORARY_WEB.getDefaultState());
                            long worldAge = this.world.getTotalWorldTime();
                            addTempWeb((short) this.spider.dimension, pos, lifeSpan, worldAge);
                        }
                    }

                    this.shiftX = 0; this.shiftY = 0; this.shiftZ = 0;
                }

                this.timer2 = 0;
            }
        }
    }

    private boolean incorrectState() {
        return this.spider.isInWater() || this.spider.isInLava()
                || this.spider.isAirBorne
                || this.spider.isOnLadder();
    }

    private boolean isWebNearby(int sX, int sY, int sZ) {
        boolean b = isBlockNearby( sX, sY, sZ, webSearchRadius, world, spider, new Block(new Material(MapColor.AIR)) );
        if (debug) { System.out.println("Is web nearby? " + b); }
        return b;
    }

//    private void getDirection(int sX, int sY, int sZ) {
    private void getDirection() {
        // Узнать направление, в котором движется паук, чтобы разместить блок паутины позади

        int difX = this.currentPos.getX() - this.prevPos.getX();
        int difY = this.currentPos.getY() - this.prevPos.getY();
        int difZ = this.currentPos.getZ() - this.prevPos.getZ();

        BlockPos result = getDirectionBehind(difX, difY, difZ);

        this.shiftX = (byte) result.getX();
        this.shiftY = (byte) result.getY();
        this.shiftZ = (byte) result.getZ();
    }
}
