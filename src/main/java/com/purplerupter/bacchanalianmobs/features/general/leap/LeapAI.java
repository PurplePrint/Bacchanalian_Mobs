package com.purplerupter.bacchanalianmobs.features.general.leap;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LeapAI extends EntityAIBase {

    // Objects
    private EntityLiving entity;
    private EntityLivingBase target;

    // Timers
    private int timer;
    private static final int interval = 40;
    public boolean performLeap;
    private int leapTimer;

    // Data for conditions to start / end / perform leap
    private BlockPos startPos;
    private double startY;
    private double targetY;
    private BlockPos targetPos;
    private double targetYAtStart;
    private Vec3d mobSight;

    private short maxFarDistance;
    private static final short maxFarDistanceDEFAULT = 7;
//    private float distanceToTarget;

//    private float speedPerSecond = 3.0F; // too far per leap
    private float speedPerSecond = 2F;
    private float speedPerTick = speedPerSecond / 20;
    private boolean stopApplyingYBonus = false;

    // Conditions to start the leap
    private boolean focusRequires;
    private short timeToFocus;
    private short focusTimer;
    private short maxSightAngleToFocus;
    private short maxMoveAngleToFocus;
    private boolean isFocusComplete;
    private boolean checkFocus;

    public LeapAI(EntityLiving entity, short maxFarDistance, short timeToFocus, short maxSightAngleToFocus, short maxMoveAngleToFocus) {
//        System.out.println("LeapAI called for entity: " + entity);
        this.entity = entity;
        this.target = null;

        this.timer = 0;
        this.performLeap = false;
        this.leapTimer = 0;

        this.startPos = null;
        this.startY = 0;
        this.targetY = 0;
        this.targetYAtStart = 0;
        this.targetPos = null;
        this.mobSight = null;
        this.maxFarDistance = maxFarDistance;
//        this.distanceToTarget = 0;

//        this.additionalBoostY = 2.0F; // TODO
//        this.ticksForYBoost = 7;

        this.focusRequires = true;
        this.timeToFocus = timeToFocus; // Must be more than 1
        this.maxSightAngleToFocus = maxSightAngleToFocus;
        this.maxMoveAngleToFocus = maxMoveAngleToFocus;
        this.focusTimer = 0;
        this.isFocusComplete = false;
        this.checkFocus = false;
    }

    @Override
    public boolean shouldExecute() { return true; }

    @Override
    public void updateTask() {

        // Проверить, смотрит ли моб на игрока достаточно долго, как бы фокусируясь перед прыжком
        if (this.focusRequires && !this.performLeap
                && this.target != null) {

            if (this.isFocusComplete) {
//                player.sendMessage(new TextComponentString("Focus complete!"));

                this.performLeap = true;

                this.focusTimer = 0;
                this.checkFocus = false;
                this.isFocusComplete = false;
            }

            if ((this.focusTimer == 0 || this.focusTimer >= this.timeToFocus)
                    && !this.performLeap) {

                // Вектор взгляда моба
                Vec3d mobLookVec = entity.getLook(1.0F).normalize();
                // Вектор направления движения моба, рассчитанный на основе поворота туловища (rotationYaw)
                Vec3d mobMoveVec = new Vec3d(-Math.sin(Math.toRadians(entity.rotationYaw)), 0, Math.cos(Math.toRadians(entity.rotationYaw))).normalize();

                // Вектор от моба к игроку
                Vec3d directionToPlayer = this.target.getPositionVector().subtract(entity.getPositionVector()).normalize();

                // Рассчитываем угол между направлением взгляда моба и направлением к игроку
                double lookAngle = Math.toDegrees(Math.acos(mobLookVec.dotProduct(directionToPlayer)));
                // Рассчитываем угол между направлением туловища моба и направлением к игроку
                double moveAngle = Math.toDegrees(Math.acos(mobMoveVec.dotProduct(directionToPlayer)));

                // Проверяем, находится ли угол взгляда и направления туловища в пределах допустимого отклонения
                boolean isLookingAtPlayer = lookAngle <= maxSightAngleToFocus;
                boolean isMovingTowardsPlayer = moveAngle <= maxMoveAngleToFocus;

                this.focusTimer++;

                if (isLookingAtPlayer && isMovingTowardsPlayer) {
                    if (this.checkFocus) {
                        this.isFocusComplete = true;
                        this.checkFocus = false;
//                        player.sendMessage(new TextComponentString("Check n2 - done!"));
                    } else {
//                        player.sendMessage(new TextComponentString("Check n1 - done!"));
                        this.checkFocus = true;
                    }
                }

            } else if (!this.performLeap) {
                this.focusTimer++;
            }

        }

        // Выполнить рывок
        if (this.performLeap) {
            if (this.leapTimer == 0) { // Начало - определяются значения переменных, меняются параметры рывка
//                System.out.println("Perform Leap started!");
                this.leapTimer++;

                this.startPos = new BlockPos(this.entity);
                this.targetPos = new BlockPos(this.target);
                this.startY = this.entity.posY;
//                this.targetY = this.target.posY - this.startY + (this.target.height / 2); // разница в высоте пола + середина туловища цели
                this.targetY = this.target.posY - this.startY;
                float a = 0.65F; // TODO Не идеально, но похоже на правду
                if (this.targetY < a) {
                    this.targetY = a;
                }
                this.mobSight = this.entity.getLookVec(); // Направление, куда смотрит моб. Определяет движение по осям XZ
                this.maxFarDistance = (short) ( this.startPos.getDistance( // Максимальная дальность, на которую моб может прыгнуть
                        this.targetPos.getX(), this.startPos.getY(), this.targetPos.getZ() // не уверен, откуда брать Y - из startPos или targetPos
                ) + 2 ); // 2 блока за спиной цели

//                System.out.println("Start pos BlockPos: " + this.startPos);
//                System.out.println("Start Y: " + this.startY);
//                System.out.println("Target Y: " + this.targetY);
//                System.out.println("Mob sight: " + this.mobSight);
//                System.out.println("Max far distance: " + this.maxFarDistance);

            } else {
                // Процесс пошёл: тики, во время которых моб будет двигаться в процессе рывка
//                System.out.println("Perform Leap continue!");

                double distanceToTarget = this.startPos.getDistance(
                        this.targetPos.getX(),
                        this.targetPos.getY(),
                        this.targetPos.getZ()
                );

                BlockPos currentPos = new BlockPos(this.entity);
                double distanceTo = this.startPos.getDistance(
                        currentPos.getX(),
                        this.startPos.getY(),
//                        currentPos.getY(),
                        currentPos.getZ()
                );
//                System.out.println("Distance from startPos to currentPos is: " + distanceTo);

                // Динамически меняю скорость движения по XZ в зависимости от пройденного расстояния
                float multiplierForX = 1;
                float multiplierForY = 1;
                float multiplierForZ = 1;
                if (distanceTo > maxFarDistanceDEFAULT) {
                    multiplierForX = (float) (distanceTo / maxFarDistanceDEFAULT);
//                    multiplierForY = (float) (distanceTo / maxFarDistanceDEFAULT); // TODO ???
                    multiplierForZ = (float) (distanceTo / maxFarDistanceDEFAULT);
                }

                // Множитель подъёма наверх, по Y, если цель находится далеко от моба
                if (distanceToTarget > maxFarDistanceDEFAULT) {
                    multiplierForY = (float) (distanceToTarget / maxFarDistanceDEFAULT);
                }

                if (this.targetYAtStart == 0) {
                    this.targetYAtStart = this.targetY;
                    this.targetY *= multiplierForY > 1 ? multiplierForY / 3 : 1; // не пропорционально
                }

                this.entity.addVelocity(
                        this.mobSight.x * this.speedPerTick * multiplierForX,
                        !this.stopApplyingYBonus ? this.speedPerTick * multiplierForY : 0,
                        this.mobSight.z * this.speedPerTick * multiplierForZ
                );

                // TODO слишком высоко по Y, как следствие - моб летит слишком далеко при рывке
                if (!this.stopApplyingYBonus) {
                    if (this.entity.posY >= this.startY + this.targetY) {
//                        System.out.println("Stop applying the Y bonus!");
                        this.stopApplyingYBonus = true;
                    }
                }

//                System.out.println("this.entity is: " + this.entity);
//                System.out.println("currentPos is: " + currentPos);
//                System.out.println("startPos is: " + this.startPos);
                if (distanceTo >= this.maxFarDistance) {

//                    System.out.println("Mob leap far enough! Stop leaping!");
                    this.performLeap = false;
                    this.timer = 0;
                    this.leapTimer = 0;

                    this.target = null;
                    this.startPos = null;
                    this.startY = 0;
                    this.targetY = 0;
                    this.targetPos = null;
                    this.targetYAtStart = 0;
                    this.stopApplyingYBonus = false;
                    this.mobSight = null;
                    this.maxFarDistance = 10;

                    this.isFocusComplete = false;
                    this.focusTimer = 0;
                    this.checkFocus = false;
                } else {

                    this.leapTimer++;
                }

            }

        }

        // Проверка условий, чтобы выполнить рывок
        else {
            this.timer++;
            if (this.timer >= interval) { // Проводить проверку не каждый тик, а раз в несколько, для экономии ресурсов

                this.target = this.entity.getAttackTarget();
                if (this.target != null && this.target instanceof EntityPlayer) {
                    if (!incorrectState()) {

                        BlockPos mobPos = new BlockPos(this.entity);
                        BlockPos playerPos = new BlockPos(this.target);
                        if ( mobPos.getDistance( playerPos.getX(), playerPos.getY(), playerPos.getZ() ) <= maxFarDistanceDEFAULT ) {

                            if (this.focusRequires) {
                                if (!this.checkFocus) {
                                    this.checkFocus = true;
                                } else {

                                    if (this.isFocusComplete) {

                                        // Проверки завершены, выполнение рывка разрешено
                                        this.performLeap = true;
                                        this.timer = 0;
                                        this.leapTimer = 0;

                                        this.focusTimer = 0;
                                        this.isFocusComplete = false;
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

    }

    private boolean incorrectState() {
        return this.entity.isInWater() || this.entity.isInLava()
                || this.entity.isAirBorne
                || this.entity.isRiding();
    }

    @Override
    public void resetTask() {

    }

}
