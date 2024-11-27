package com.purplerupter.bacchanalianmobs.features.general.riding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

import static com.purplerupter.bacchanalianmobs.features.general.riding.StartRiding.HORSE_SPEED;

public class EntityAIHorseControl extends EntityAIBase {
    private final EntityLiving horse;
    private final EntityLiving rider;

    public EntityAIHorseControl(EntityLiving horse, EntityLiving rider) {
        this.horse = horse;
        this.rider = rider;
        setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        return this.rider != null && this.rider.isRiding();
    }

    @Override
    public void updateTask() {
        // Синхронизируем движение лошади с движением наездника
        this.horse.getNavigator().tryMoveToEntityLiving(this.rider, HORSE_SPEED);
//            horse.rotationYaw = rider.rotationYaw; // Синхронизируем поворот
    }

    @Override
    public void resetTask() {
        this.horse.tasks.removeTask(this);
    }
}
