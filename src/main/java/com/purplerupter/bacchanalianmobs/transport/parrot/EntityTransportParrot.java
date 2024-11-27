package com.purplerupter.bacchanalianmobs.transport.parrot;

//import com.purplerupter.bacchanalianmobs.transport.parrot.LaunchBoostAI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.passive.EntityParrot;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.GetAttackReach.getAttackReach;

// Класс сущности транспортного попугая
public class EntityTransportParrot extends EntityParrot {

    private EntityLiving passenger;

    private int idleTime;
    private float speed = 10F;
//    private boolean boostCompleted = false; // Флаг для проверки, завершился ли взлёт
//    private byte boostDifference; // Разница между изначальной высотой моба и высотой после буста. Нужна чтобы стремиться не к "ногам" игрока, а к такой же высоте относительно игрока

    public EntityTransportParrot(World worldIn) {
        super(worldIn);
        if (debug) { System.out.println("EntityTransportParrot called"); }

        this.idleTime = 0;
        this.experienceValue = 0;
        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();

        // Добавление базовых задач
        this.tasks.addTask(9, new EntityAILookIdle(this));

        // Задача LaunchBoostAI будет добавлена позже, когда пассажир будет найден
    }

//    public void setBoostDifference(byte amount) {
//        this.boostDifference = amount;
//        if (debug) { System.out.println("The boost difference is: " + this.boostDifference); }
//    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        System.out.println("applyEntityAttributes...");
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(speed);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
    }

    @Override
    public void onLivingUpdate() {
//        super.onLivingUpdate();

        // Проверяем наличие пассажиров и обновляем переменную passenger
        List<Entity> passengers = this.getPassengers();
        if (!passengers.isEmpty() && passengers.get(0) instanceof EntityLiving) {
            this.passenger = (EntityLiving) passengers.get(0);
        }

        // Если взлёт ещё не завершён и пассажир есть, запускаем LaunchBoostAI
//        if (!boostCompleted && this.passenger != null) {
//            this.tasks.addTask(1, new LaunchBoostAI(this, this.passenger, this.speed));
//            boostCompleted = true; // Отмечаем, что задача по взлёту запущена
//        }

        // Логика для завершения взлёта и последующего поведения
//        if (boostCompleted) {
        if (this.passenger == null || this.passenger.getAttackTarget() == null) {
            if (debug) { System.out.println("This transport parrot is idle. Waiting..."); }
            this.idleTime++;

            if (this.idleTime > 500) {
                if (debug) { System.out.println("Kill this parrot"); }
                this.dismountEntity(this.passenger);
                this.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
            }
        } else {
            this.idleTime = 0;
        }

        if (this.passenger != null) {
            if (debug) { System.out.println("A passenger is not null"); }
            EntityLivingBase target = this.passenger.getAttackTarget();
            if (debug) { System.out.println("The target is: " + target); }

            if (target != null) {
                if (debug) { System.out.println("The target is not null"); }

                if (timeToDismount(this.passenger)) {
                    if (debug) { System.out.println("Close enough! Time to dismount!"); }
                    this.passenger.dismountRidingEntity();
                    this.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);

                    super.onLivingUpdate();
                    return;
                }

                // Навигация к цели
//                    this.getNavigator().tryMoveToXYZ(target.posX, target.posY, target.posZ, 1);
                if (debug) { System.out.println("tryMoveToXYZ: " + target.posX + ", " + target.posY + ", " + target.posZ); }
                this.getNavigator().tryMoveToXYZ(target.posX, target.posY, target.posZ, 3);
            }
        }
//        }

        super.onLivingUpdate();
    }

    private boolean timeToDismount(EntityLiving mob) {

        EntityLivingBase target = mob.getAttackTarget();
        if (target != null
                && mob.getDistanceSq(target.posX, target.posY, target.posZ) <= (getAttackReach(mob) * getAttackReach(mob))) {
            return this.world.getBlockState(this.getPosition().down()).getMaterial().isSolid(); }

        return false;
    }
}
