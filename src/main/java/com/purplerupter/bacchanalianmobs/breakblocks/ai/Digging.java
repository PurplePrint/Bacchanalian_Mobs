package com.purplerupter.bacchanalianmobs.breakblocks.ai;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import static com.purplerupter.bacchanalianmobs.breakblocks.utils.BlockHardness.getBlockHardness;
import static com.purplerupter.bacchanalianmobs.breakblocks.utils.MultipliedBlockHardness.getMultipliedBlockHardness;
import static com.purplerupter.bacchanalianmobs.breakblocks.utils.MultipliedBlockHardness.isEffectiveToolForBlock;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class Digging extends EntityAIBase
{
    private EntityLivingBase target;
    private EntityLiving digger;

    private BlockPos curBlock;
    private int scanTick;
    private int digTick;
    private BlockPos obsPos;
    private int obsTick;

    private JsonObject configObject;

    public Digging(final EntityLiving digger, JsonObject configObject) {
        this.scanTick = 0;
        this.digTick = 0;
        this.obsPos = null;
        this.obsTick = 0;
        this.digger = digger;
        this.configObject = configObject;
    }

    public boolean shouldExecute() {
        this.target = this.digger.getAttackTarget();
        System.out.println("The target is: " + this.target);
        if (this.target == null || !this.target.isEntityAlive() || !this.digger.getNavigator().noPath()) {
            boolean a = this.target == null;
            boolean c = this.digger.getNavigator().noPath();
            System.out.println("Achtung! Null? " + a + " No path? " + c);
            if (this.target != null) {
                boolean b = this.target.isEntityAlive();
                System.out.println(" isEntityAlive? " + b);
            }
            return false;
        }
        final double dist = this.digger.getDistanceSq((Entity)this.target);
        final double navDist = this.digger.getNavigator().getPathSearchRange();
        if (dist < 1.0 || dist > navDist * navDist) {
            return false;
        }
        if (this.obsPos == null) {
            this.obsPos = this.digger.getPosition();
        }
        if (!this.obsPos.equals((Object)this.digger.getPosition())) {
            this.obsTick = 0;
            this.obsPos = null;
            return false;
        }
        if (++this.obsTick < 20) {
            return false;
        }
        this.curBlock = ((this.curBlock != null && this.digger.getDistanceSq(this.curBlock) <= 16.0 && this.canHarvest(this.digger, this.curBlock)) ? this.curBlock : this.getNextBlock(this.digger, this.target, 2.0));
        return this.curBlock != null;
    }

    public void startExecuting() {
        super.startExecuting();
        this.digger.getNavigator().clearPath();
        this.obsTick = 0;
        this.obsPos = null;
    }

    public void resetTask() {
        this.curBlock = null;
        this.digTick = 0;
        this.obsTick = 0;
        this.obsPos = null;
    }

    public boolean shouldContinueExecuting() {
        return this.target != null && this.curBlock != null && this.digger.getDistanceSq(this.curBlock) <= 16.0 && this.canHarvest(this.digger, this.curBlock);
    }

    public void updateTask() {
        this.digger.getLookHelper().setLookPosition(this.target.posX, this.target.posY + this.target.getEyeHeight(), this.target.posZ, (float)this.digger.getHorizontalFaceSpeed(), (float)this.digger.getVerticalFaceSpeed());
        this.digger.getNavigator().clearPath();
        ++this.digTick;
        final float origHardness = getBlockHardness((EntityLivingBase)this.digger, this.digger.world, this.curBlock) * (this.digTick + 1.0f);
        final IBlockState state = this.digger.world.getBlockState(this.curBlock);
        final float hardness = getMultipliedBlockHardness(origHardness, configObject, state, (EntityLivingBase)this.digger);
        final ItemStack heldItem = this.digger.getHeldItem(EnumHand.MAIN_HAND);
        if (this.digger.world.isAirBlock(this.curBlock)) {
            this.resetTask();
        }
        else if (hardness >= 1.0f) {
            final boolean canHarvest = state.getMaterial().isToolNotRequired() || canHarvest(this.digger, this.curBlock);
            this.digger.world.destroyBlock(this.curBlock, false);
            if (canHarvest && this.digger.world instanceof WorldServer) {
                final FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer)this.digger.world);
                player.setHeldItem(EnumHand.MAIN_HAND, heldItem);
                player.setHeldItem(EnumHand.OFF_HAND, this.digger.getHeldItem(EnumHand.OFF_HAND));
                player.setPosition((double)this.digger.getPosition().getX(), (double)this.digger.getPosition().getY(), (double)this.digger.getPosition().getZ());
                final TileEntity tile = this.digger.world.getTileEntity(this.curBlock);
                state.getBlock().harvestBlock(this.digger.world, (EntityPlayer)player, this.curBlock, state, tile, heldItem);
            }
            this.digger.getNavigator().setPath(this.digger.getNavigator().getPathToEntityLiving((Entity)this.target), this.digger.getMoveHelper().getSpeed());
            this.resetTask();
        }
        else if (this.digTick % 5 == 0) {
            this.digger.world.playSound((EntityPlayer)null, this.curBlock, state.getBlock().getSoundType(state, this.digger.world, this.curBlock, (Entity)this.digger).getHitSound(), SoundCategory.BLOCKS, 1.0f, 1.0f);
            this.digger.swingArm(EnumHand.MAIN_HAND);
            this.digger.world.sendBlockBreakProgress(this.digger.getEntityId(), this.curBlock, (int)(hardness * 10.0f));
        }
    }

    private BlockPos getNextBlock(final EntityLiving entityLiving, final EntityLivingBase target, final double dist) {
        final int digWidth = MathHelper.ceil(entityLiving.width);
        final int digHeight = MathHelper.ceil(entityLiving.height);
        final int passMax = digWidth * digWidth * digHeight;
        if (passMax <= 0) {
            return null;
        }
        final int y = this.scanTick % digHeight;
        final int x = this.scanTick % (digWidth * digHeight) / digHeight;
        final int z = this.scanTick / (digWidth * digHeight);
        final double rayX = x + Math.floor(entityLiving.posX) + 0.5 - digWidth / 2.0;
        final double rayY = y + Math.floor(entityLiving.posY) + 0.5;
        final double rayZ = z + Math.floor(entityLiving.posZ) + 0.5 - digWidth / 2.0;
        final Vec3d rayOrigin = new Vec3d(rayX, rayY, rayZ);
        Vec3d rayOffset = new Vec3d(Math.floor(target.posX) + 0.5, Math.floor(target.posY) + 0.5, Math.floor(target.posZ) + 0.5);
        rayOffset.add(new Vec3d(x - digWidth / 2.0, (double)y, z - digWidth / 2.0));
        Vec3d norm = rayOffset.subtract(rayOrigin).normalize();
        if (Math.abs(norm.x) == Math.abs(norm.z) && norm.x != 0.0) {
            norm = new Vec3d(norm.x, norm.y, 0.0).normalize();
        }
        rayOffset = rayOrigin.add(norm.scale(dist));
        final BlockPos p1 = entityLiving.getPosition();
        final BlockPos p2 = target.getPosition();
        if (p1.getDistance(p2.getX(), p1.getY(), p2.getZ()) < 4.0) {
            if (p2.getY() - p1.getY() > 2.0) {
                rayOffset = rayOrigin.add(new Vec3d(0.0, dist, 0.0));
            }
            else if (p2.getY() - p1.getY() < -2.0) {
                rayOffset = rayOrigin.add(new Vec3d(0.0, -dist, 0.0));
            }
        }
        final RayTraceResult ray = entityLiving.world.rayTraceBlocks(rayOrigin, rayOffset, false, true, false);
        this.scanTick = (this.scanTick + 1) % passMax;
        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            final BlockPos pos = ray.getBlockPos();
            final IBlockState state = entityLiving.world.getBlockState(pos);
            if (this.canHarvest(entityLiving, pos)) {
                return pos;
            }
        }
        return null;
    }

    private boolean canHarvest(final EntityLiving entity, final BlockPos pos) {
        if (debug) { System.out.println("====  ====  ===="); }
        if (debug) { System.out.println("The canHarvest method called. The config object is: " + configObject); }
        final IBlockState state = entity.world.getBlockState(pos);

        if (!state.getMaterial().isSolid() || state.getBlockHardness(entity.world, pos) < 0.0f) {
            if (debug) { System.out.println("The material is not solid or block hardness is less than 0. Return false!"); }
            return false;
        }

        final Block block = state.getBlock();

        // Получаем уровень добычи для блока
        int harvestLevel = block.getHarvestLevel(state);
        String requiredTool = block.getHarvestTool(state);
        boolean isSoftBlock = requiredTool == null;
        boolean hasEffectiveTool = isEffectiveToolForBlock(entity.getHeldItemMainhand(), block, state);

        if (debug) { System.out.println("The requiredTool is " + requiredTool + " // The harvestLevel is: " + harvestLevel +
                " // The isSoftBlock is: " + isSoftBlock + " // The hasEffectiveTool is: " + hasEffectiveTool); }

        // Логика для мягких блоков
        if (isSoftBlock) {
            if (debug) { System.out.println("This is a soft block"); }
            if (!configObject.get("Break soft blocks").getAsBoolean()) {
                if (debug) { System.out.println("The 'Break soft blocks' from config is false, so return false"); }
                return false; // Запрещено ломать мягкие блоки
            }

            // Если для мягких блоков требуется инструмент, проверяем наличие инструмента у моба
            if (configObject.get("Tool for soft").getAsBoolean() && !hasEffectiveTool) {
                if (debug) { System.out.println("The 'Tool for soft' from config is false, so return false"); }
                return false; // Инструмент обязателен, но его нет
            }

            if (debug) { System.out.println("Return true"); }
            return true; // Мягкий блок можно ломать
        }

        if (debug) { System.out.println("This is not a soft block (i.e. - stone block)"); }

        // Логика для каменных блоков
        if (!configObject.has("Break stone blocks") // Если в конфиге не указаны каменные блоки - значит их запрещено копать
                || !configObject.get("Break stone blocks").getAsBoolean()) {
            if (debug) { System.out.println("The 'Break stone blocks' from config is false, so return false"); }
            return false; // Запрещено ломать каменные блоки
        }

        // Если для каменных блоков требуется инструмент, проверяем наличие подходящего инструмента
        if (configObject.get("Tool for stone").getAsBoolean() && !hasEffectiveTool) {
            if (debug) { System.out.println("The 'Tool for stone' from config is false, so return false"); }
            return false; // Инструмент обязателен, но его нет
        }

        if (debug) { System.out.println("Return true"); }
        return true; // Каменный блок можно ломать
    }
}
