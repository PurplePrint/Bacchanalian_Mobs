package com.purplerupter.bacchanalianmobs.breakblocks;

import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.breakblocks.ai.AITaskBreakBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.FindBlockNearby.isBlockNearby;

public class DestroyTurrets {
    private ArrayList<String> turretBlocks;
    private static final byte MAX_SEARCH_RANGE = 10; // Максимальное расстояние для поиска блока турели
    private EntityCreature turret;
    private EntityLiving destroyer;
    private float reach;
    private World world;
    private int breakThreshold; // Порог разрушения (чем больше, тем дольше будет разрушаться блок)
//    private boolean toolRequires;
//    private short diggingSpeed;
//    private boolean sourceHardness;
    private final JsonObject turretDestroyConfig;

    public DestroyTurrets(final JsonObject turretDestroyConfig, final ArrayList<String> turretBlocks, final EntityCreature entityTurret, final EntityLiving destroyer, final float destroyerReach) {
        if (debug) { System.out.println("DestroyTurrets called"); }

        this.turret = entityTurret;
        this.world = turret.world;
        this.turretBlocks = turretBlocks;
        this.destroyer = destroyer;
        this.reach = destroyerReach;

        this.turretDestroyConfig = turretDestroyConfig;
    }

    public void findAndDestroyTurret() {

        isBlockNearby((int) turret.posX, (int) turret.posY, (int) turret.posZ,
                MAX_SEARCH_RANGE, reach, world, destroyer, turret, turretBlocks, turretDestroyConfig);

    }
}
