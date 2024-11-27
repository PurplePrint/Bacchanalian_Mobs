package com.purplerupter.bacchanalianmobs.sight;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import static com.purplerupter.bacchanalianmobs.etc.utils.GetRenderDistance.getRenderDistanceDimension;
import static com.purplerupter.bacchanalianmobs.sight.XRayFromPlayer.*;

public class NearbyBeaconEntityAI extends EntityAIBase {

    // Объекты
    private EntityLiving mob;
    private World world;
    private short dimension;
    private EntityPlayer beacon;

    // Таймеры
    private int timer = 0;
    private int interval = 40;

    // Поиск маяка поблизости
    private int difX = 0;
    private int difZ = 0;
    private short radius = 0;

    // Назначаю задачу
    private boolean a = false;
    private boolean agro = false;
    private XRayExactEntityAI xRayExactEntityAI = null;

    private float defaultFollowRange;
    private float hugeFollowRange;

    public NearbyBeaconEntityAI(EntityLiving mob, World world, short dimension, float defaultFollowRange) {
        this.mob = mob;
        this.world = world;
        this.dimension = dimension;

        this.defaultFollowRange = defaultFollowRange;
    }

    @Override
    public boolean shouldExecute() { return true; }

    @Override
    public void updateTask() {
        this.timer++;
        if (this.timer >= this.interval) {
            this.timer = 0;
            this.dimension = (short) this.mob.dimension;

            // Моб уже имеет игрока-маяка как цель. Однако прошло время, и игрок мог убежать - нужна проверка.
            if (this.agro) {
                if (this.beacon.dimension != this.dimension) {
                    //System.out.println("The beacon player is in another dimension! Remove X-Ray task from mob");
                    finish();
                    return;
                }

                if (!isBeacon(this.dimension, this.beacon)) {
                    //System.out.println("This player is not beacon");
                    finish();
                    return;
                }

                this.difX = Math.abs(this.beacon.chunkCoordX - this.mob.chunkCoordX);
                this.difZ = Math.abs(this.beacon.chunkCoordZ - this.mob.chunkCoordZ);
                if (this.difX > this.radius || this.difZ > this.radius) {

                    //System.out.println("Player is too far! Remove X-Ray AI task from mob");
                    finish();
                    return;
                }
            }

            // Для начала - убедиться, находится ли игрок-маяк в том же измерении, что и моб, и находится ли он в пределах дальности прорисовки от моба
            if (hasBeaconsInDimension(this.dimension)) {
                for (String nickname : beaconPlayers(this.dimension).keySet()) {
                    EntityPlayer player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(nickname);
                    if (player != null) {

                        this.difX = Math.abs(player.chunkCoordX - this.mob.chunkCoordX);
                        if (this.difX <= getRenderDistanceDimension((short) this.mob.dimension)) {
                            this.difZ = Math.abs(player.chunkCoordZ - this.mob.chunkCoordZ);
                            if (this.difZ <= getRenderDistanceDimension((short) this.mob.dimension)) {

                                //System.out.println("A beacon player is in the render distance! " + nickname);
                                this.a = true;
                                this.beacon = player;
                                break;
                            }
                        }

                    }
                }

                // Игрок находится недалеко от моба, значит, нужно проверить, находится ли он достаточно близко для своего уровня эффекта "Маяк"
                if (this.a) {
                    this.radius = (short) (getBeaconRadiusForPlayer(this.dimension, this.beacon.getName()) + 1);
                    this.hugeFollowRange = (this.radius + 2) * 16;
                    if (this.difX <= this.radius
                            && this.difZ <= this.radius) {

                        //System.out.println("This mob is in the beacon player radius!");
                        this.agro = true;
                        //System.out.println("xRayExactEntityAI is: " + this.xRayExactEntityAI);
//                        xRayExactEntityAI = new xRayExactEntityAI((EntityLiving) event.getEntity(), mob.world, radius, beacon);
                        if (this.xRayExactEntityAI == null) {
                            this.xRayExactEntityAI = new XRayExactEntityAI(this.mob, this.world, this.radius, this.beacon);
                            this.mob.tasks.addTask(0, this.xRayExactEntityAI);
//                            mob.tasks.addTask(3, xRayExactEntityAI);
                        }
                        if (this.xRayExactEntityAI.stop) {
                            this.xRayExactEntityAI.stop = false;
                        }
                        //System.out.println("xRayExactEntityAI is: " + this.xRayExactEntityAI);
                        setHugeFollowRange();
                    }

                    // Игрок находится слишком далеко - игрок перестаёт быть целью для атаки
                    else {
                        finish();
                    }

                }
            }
        }
    }

    // Обнуляем данные, так как текущий игрок-маяк недоступен
    private void finish() {
        if (this.mob != null) {
//            this.mob.setAttackTarget(null);
            if (this.mob.getAttackTarget() instanceof EntityPlayer) {
                if (this.mob.getAttackTarget().getName().equals(this.beacon.getName())) {
                    if (!this.mob.canEntityBeSeen(this.beacon)
                            || this.mob.getDistanceSq(this.beacon) > (this.defaultFollowRange * this.defaultFollowRange) ) {
                        this.mob.setAttackTarget(null);
                    }
                }
            }
        }

//        this.xRayExactEntityAI = null;
        if (this.xRayExactEntityAI != null) {
            this.xRayExactEntityAI.stop = true;
        }
        this.agro = false;
        this.radius = Short.MIN_VALUE;
        this.difX = Short.MIN_VALUE;
        this.difZ = Short.MIN_VALUE;
        this.beacon = null;
//        mob = null;

        setNormalFollowRange();
    }

    private void setHugeFollowRange() {
        this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(this.hugeFollowRange);
    }

    private void setNormalFollowRange() {
        this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(this.defaultFollowRange);
    }
}
