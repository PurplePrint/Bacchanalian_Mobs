package com.purplerupter.bacchanalianmobs.sight;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.etc.conditions.utils.GetRenderDistance.getRenderDistance;
import static com.purplerupter.bacchanalianmobs.etc.utils.LawfulTarget.lawfulTarget;

public class XRayEntityAI extends EntityAIBase {

    // Объекты
    private EntityLiving mob;
    private World world;
    private short dimension;
    private NBTTagCompound nbt;

    // Дальность поиска
    private boolean allSight;
    private short maxDistance = -1;
    private int maxDistanceSq = -1;

    // Поиск цели
    private boolean specificPlayer;
    private EntityPlayer targetPlayer = null;
    private Map<Integer, EntityPlayer> nearbyPlayers = new HashMap<>();
    private int pCX = Integer.MIN_VALUE;
    private int pCZ = Integer.MIN_VALUE;

    // Цель для атаки
    public String targetName = null;
    public static final String TARGET_NAME_TAG = "TargetName";
    private FakePlayer fake = null;

    // Обновление задачи каждый тик
    private int timer;
    private int interval = 20;
    private BlockPos prevBlock = null;
    private BlockPos currentBlock = null;
    private static final float maxDifference = 1.4F;

    private boolean setTarget = false;

    // Обновлять цель во избежание сбоев
    private int timer2 = 0;
    private int interval2 = 20;

    // Найти нового игрока в радиусе
    private int timer3 = 0;
    private int interval3 = 40;

    // Оповестить Dynamic Stealth, чтобы сменить фейк на настоящего игрока. (Dynamic Stealth сильно меняет ИИ мобов, нужен костыль)
    private static final String CAN_SEE_TARGET_TAG = "CanSeeTarget";

//    public XRayEntityAI(EntityLiving mob, World world, boolean allSight) {

    // Видеть определённого игрока на любом расстоянии (в радиусе прорисовки)
    public XRayEntityAI(EntityLiving mob, World world, EntityPlayer target) {
        this.mob = mob;
        this.world = world;
        this.dimension = (short) mob.dimension;

        this.allSight = true;
        this.maxDistance = (short) (getRenderDistance(mob.getEntityWorld().getMinecraftServer(), this.dimension) + 1);

        init();
        if (target != null) {
            this.specificPlayer = true;
            this.targetPlayer = target;
        } else {
            System.out.println("Error when call XRayEntityAI: the EntityPlayer target is null!");
        }
    }

    // Видеть всех игроков в радиусе, выбрать ближайшего
    public XRayEntityAI(EntityLiving mob, World world, short distance) {
        this.mob = mob;
        this.world = world;
        this.dimension = (short) mob.dimension;

        this.allSight = false;
        this.maxDistance = distance;

        init();
        this.specificPlayer = false;
    }

    // Видеть конкретного игрока в радиусе
    public XRayEntityAI(EntityLiving mob, World world, short distance, EntityPlayer target) {
        this.mob = mob;
        this.world = world;
        this.dimension = (short) mob.dimension;

        this.allSight = false;
        this.maxDistance = distance;

        init();
        if (target != null) {
            this.specificPlayer = true;
            this.targetPlayer = target;
        } else {
            System.out.println("Error when call XRayEntityAI: the EntityPlayer target is null!");
        }
    }

    private void init() {
        this.maxDistanceSq = (this.maxDistance * 16) * (this.maxDistance * 16);
        this.nbt = this.mob.getEntityData();
    }

    @Override
    public boolean shouldExecute() {
        if (this.maxDistance < 0) {
            System.out.println("Error! The MAX_DISTANCE is not set");
            return false;
        }

        // Моб выбирает любого игрока (ближайшего)
        if (!this.specificPlayer) {
            return findTargetNearby();
        }

        // Цель моба - это конкретный игрок, моб выбирает его
        else {
            // TODO а что если этот конкретный игрок недоступен???
            // Конкретный игрок определен при назначении ИИ задачи, дополнительные вычисления не нужны
            this.setTarget = true;
            return true;
        }
    }

    private boolean findTargetNearby() {
        // Нет смысла искать ближайших игроков, если моб может получить X-Ray только для одного конкретного игрока
        if (this.specificPlayer) { return false; }

//        this.world.getClosestPlayerToEntity(this.mob, this.maxDistance);

        this.dimension = (short) this.mob.dimension;
        try { // Null если на сервере нет игроков?
            for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
                if (player.dimension == this.dimension) {

                    int pCX = player.chunkCoordX;
                    int mCX = this.mob.chunkCoordX;
                    if (Math.abs(pCX - mCX) <= this.maxDistance) {

                        int pCZ = player.chunkCoordZ;
                        int mCZ = this.mob.chunkCoordZ;
                        if (Math.abs(pCZ - mCZ) <= this.maxDistance) {

//                                if (player.isCreative() || player.isSpectator()) {
////                                System.out.println("Error! Player " + player.getName() + " is in creative or spectrator mode.");
//                                    continue;
//                                }
                            if (!lawfulTarget(player)) {
                                continue;
                            }

                            System.out.println("Player " + player.getName() + " is close enough to the mob.");
                            this.pCX = pCX;
                            this.pCZ = pCZ;
                            this.nearbyPlayers.put((int) this.mob.getDistanceSq(player), player);

//                        } else { return false; }
                        } else { continue; }
//                    } else { return false; }
                    } else { continue; }
                }
            }

            // А ведь можно просто использовать world.getClosestPlayerToEntity, но я этого не сделал
            int minimum = Integer.MIN_VALUE;
            if (!this.nearbyPlayers.isEmpty()) {
                minimum = Collections.min( this.nearbyPlayers.keySet() );

            } else { System.out.println("Error! The nearby players map is empty."); }

            if (minimum != Integer.MIN_VALUE) {
                this.targetPlayer = this.nearbyPlayers.get(minimum);
                this.setTarget = true;
                return true;

            } else { System.out.println("Error! The minimum value not set."); }

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void startExecuting() {

        // TODO обратить внимание: понизить до дефолтного, когда моб перестанет владеть иксреем
        IAttributeInstance followRange = this.mob.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
//        if (followRange != null) {
        followRange.setBaseValue(this.maxDistance * 16);

//        if (this.targetPlayer != null && this.pCX != Integer.MIN_VALUE && this.pCZ != Integer.MIN_VALUE) {
//        if (this.targetPlayer != null) {
        if (this.specificPlayer) {

            if (this.targetPlayer != null) {
                fakeTarget();
                this.targetName = this.targetPlayer.getName();
                this.nbt.setString(TARGET_NAME_TAG, this.targetName);
                this.mob.setAttackTarget(this.fake);

//                this.prevBlock = new BlockPos(this.mob);
            } else { System.out.println("Error! targetPlayer is invalid."); }
//            } else { System.out.println("Error! targetPlayer or pCX or pCZ is invalid."); }
        }

        this.prevBlock = new BlockPos(this.mob);
    }

    @Override
    public boolean shouldContinueExecuting() { return true; }

    @Override
    public void updateTask() {
        // На случай, если игрок перестал быть целью атаки моба - снова сделать его целью атаки
        if (this.setTarget && this.mob.getAttackTarget() == null && this.targetPlayer != null) {
            fakeTarget();
            this.mob.setAttackTarget(this.fake);
            this.nbt.setBoolean(CAN_SEE_TARGET_TAG, false); // TODO а надо ли?
        }

        this.timer2++;
        if (this.timer2 >= this.interval2) {
            this.timer2 = 0;
            if (this.targetPlayer != null) {
                // Периодически (но не каждый тик) проверять, является ли этот игрок доступной целью, или он недоступен для атаки
                if (lawfulTarget(this.targetPlayer, (short) this.mob.dimension)) {
                    if (Math.abs(this.targetPlayer.chunkCoordX - this.mob.chunkCoordX) <= this.maxDistance
                            && Math.abs(this.targetPlayer.chunkCoordZ - this.mob.chunkCoordZ) <= this.maxDistance) {

                        this.setTarget = true;

                    } else { this.setTarget = false; return; }
                } else { this.setTarget = false; return; }

            } else { this.setTarget = false; return; }
        }

        this.timer3++;
        if (this.timer3 >= this.interval3) {
            this.timer3 = 0;
            // Попытаться найти других игроков поблизости, если текущий игрок недоступен и если не нужно атаковать конкретного игрока
            if (!this.specificPlayer
                    && (!this.setTarget || this.targetPlayer == null)) {
                findTargetNearby();
            }
        }

        if (this.setTarget) { // Нет смысла выполнять проверку ниже, если цель для атаки недоступна
            // Проверка: сократить количество вызовов canEntityBeSeen(), вызывая его только если моб преодолел достаточное расстояние.
            // Нет смысла вызывать canEntityBeSeen(), если моб остался на том же месте, и те же ближайшие блоки преграждают ему видимость цели
            if (this.prevBlock.getDistance(
                    (int) this.mob.posX, (int) this.mob.posY, (int) this.mob.posZ
            ) >= maxDifference) {

                this.prevBlock = new BlockPos(this.mob);
//            System.out.println("Mob moved significantly to new block: " + this.prevBlock);

                // Если моб видит игрока - его цель атаки должна смениться с фейка на игрока
                if (this.mob.canEntityBeSeen(this.targetPlayer)) {
                    this.mob.setAttackTarget(this.targetPlayer);
//                System.out.println("Got caught!");
                    this.nbt.setBoolean(CAN_SEE_TARGET_TAG, true); // TODO а надо ли?
                    this.setTarget = true;
                    return;
                }

                // Если моб видит фейка, он понимает что не нашёл игрока и продолжает поиск. Новый фейк на текущих координатах игрока.
                if (this.mob.canEntityBeSeen(this.fake)) {
                    fakeTarget();
//                System.out.println("Updated fake target position.");
                    this.mob.setAttackTarget(this.fake);
                    this.nbt.setBoolean(CAN_SEE_TARGET_TAG, false); // TODO а надо ли?
                    this.setTarget = true;
                }

            } else {
                // Если моб не может сблизиться с целью из-за препятствия и стоит на месте - изредка обновлять координаты фейка,
                // с расчётом на то что игрок может поменять местоположение и моб сможет добраться до него

//            this.timer++;
//            if (this.timer >= 40) {
//                this.timer = 0;
//
//                if (this.mob.canEntityBeSeen(this.targetPlayer)) {
//                    this.mob.setAttackTarget(this.targetPlayer);
////                    System.out.println("Got caught!");
//                    this.nbt.setBoolean(CAN_SEE_TARGET_TAG, true); // TODO
//                }
//
//            }
                this.timer++;
                if (this.timer >= 300) {
                    this.timer = 0;

                    fakeTarget();
                    this.mob.setAttackTarget(this.fake);
                    this.nbt.setBoolean(CAN_SEE_TARGET_TAG, false); // TODO а надо ли?
                    this.setTarget = true;
                }
            }
        }
    }

    // Создать ложного игрока на последних координатах реального игрока.
    // Так у игрока будет возможность подготовиться к приближающемуся противнику или отступить.
    // С таким подходом X-Ray мобов будет интереснее.
    private void fakeTarget() {
        this.fake = FakePlayerFactory.getMinecraft((WorldServer) this.world);
        this.fake.setPosition(this.targetPlayer.posX, this.targetPlayer.posY, this.targetPlayer.posZ);
    }

//    private boolean lawfulTarget(EntityPlayer player, short dimension) {
//        if (player.isDead) {
//            return false; }
//        if (player.dimension != dimension) {
//            return false; }
//        if (player.isCreative() || player.isSpectator()) {
//            return false; }
//
//        return true;
//    }

//    @Override
//    public void resetTask() {
//        System.out.println("Reset!");
//        this.mob.setAttackTarget(null);
//    }

}
