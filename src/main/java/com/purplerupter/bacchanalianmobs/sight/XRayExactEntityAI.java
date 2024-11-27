// Отличается от XRayEntityAI тем, что не использует fake player, а использует реального игрока с его актуальными координатами как цель
package com.purplerupter.bacchanalianmobs.sight;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import static com.purplerupter.bacchanalianmobs.etc.utils.LawfulTarget.lawfulTarget;

public class XRayExactEntityAI extends EntityAIBase {

    // Объекты
    private EntityLiving mob;
    private World world;
    private short dimension;
    private EntityPlayer target;

    // Поиск
    private short distance = -1;
    private int difX = 0;
    private int difZ = 0;
    private boolean fail = false;
    private boolean failComplete = false;
    private boolean waitToSetTarget = false;

    // Таймеры
    private int timer = 0;
    private int interval = 20;
    private boolean sContinue = true;

    private int timer2 = 0;
    private int interval2 = 40 + (short)(this.interval * (short)10 / (short)100);

    public boolean stop = false;

    public XRayExactEntityAI(EntityLiving mob, World world, short distance, EntityPlayer target) {
        this.mob = mob;
        this.world = world;
        this.distance = distance;
        this.dimension = (short) mob.dimension;
        this.target = target;
    }

    @Override
    public boolean shouldExecute() {
        return lawfulTarget(this.target, this.dimension);
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (this.stop) {
            return false;
        }

        this.timer++;
        if (this.timer >= this.interval) {
            this.timer = 0;
            this.dimension = (short) this.mob.dimension;
            if (lawfulTarget(this.target, this.dimension)) {
                this.sContinue = true;
                return true;
            } else {
                this.sContinue = false;
                return false;
            }
        }

        return this.sContinue;
    }

    @Override
    public void updateTask() {

        // Если у моба уже есть другая цель для атаки - ждать, пока моб потеряет цель, и тогда назначить игрока-маяка целью
        if (this.waitToSetTarget) {
            if (this.mob.getAttackTarget() == null) {
                this.mob.setAttackTarget(this.target);
                this.waitToSetTarget = false;
            }
        }

        // Проводить проверку не каждый тик для экономии ресурсов. Излишняя точность в данном случае не нужна.
        this.timer2++;
        if (this.timer2 >= this.interval2) {
            this.timer2 = 0;

            // Если проверка расстояния выдала ложь - моб перестаёт атаковать игрока-маяка
            if (this.fail && !this.failComplete) {
                this.mob.setAttackTarget(null);
                // Дополнительный флаг, чтобы цель атаки (то есть - игрок-маяк) сбросился только один раз, а последующие цели не сбрасывались
                this.failComplete = true;
            }

            // Проверить - находится ли игрок-маяк достаточно близко к мобу, и доступен ли он
            if (lawfulTarget(this.target, (short) this.mob.dimension)) {
                this.difX = Math.abs(this.mob.chunkCoordX - this.target.chunkCoordX);
                if (this.difX <= this.distance) {
                    this.difZ = Math.abs(this.mob.chunkCoordZ - this.target.chunkCoordZ);
                    if (this.difZ <= this.distance) {

                        this.fail = false;
                        this.failComplete = false;
                        // Если у моба нет цели - задать игрока-маяка как цель
                        if (this.mob.getAttackTarget() == null) {
                            this.mob.setAttackTarget(this.target);
                        } else { // Иначе - ждать, пока моб не останется без цели
                            this.waitToSetTarget = true;
                        }

                    } else { this.fail = true; }
                } else { this.fail = true; }

            } else { this.fail = true; }
        }
    }

}
