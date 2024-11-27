package com.purplerupter.bacchanalianmobs.transport.guardian;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class EntityAIRideBoat extends EntityAIBase {

    EntityLiving living;
    int wait = 0;

    int delay = 0;
    boolean yes = false;
    boolean check1 = false;
    boolean check2 = false;

    public EntityAIRideBoat(EntityLiving living) {
        if (debug) { System.out.println("EntityAIRideBoat called"); }
        this.living = living;
    }

    @Override
    public boolean shouldExecute() {
        if (!this.yes) {
            this.delay++;
            if (this.delay >= 40) {

                this.delay = 0;
                this.yes = true;
            }
        }

        if (this.yes) {

            if (this.check1) {

                if (this.check2) {
                    this.wait = 0;
                    this.delay = 0;
                    this.yes = false;
                    this.check1 = false;
                    this.check2 = false;
                    return true;
                }

                this.wait++;
                if (this.wait >= 40) {
                    this.wait = 0;

                    if (check()) {
                        this.check2 = true;
                    } else {
                        this.check1 = false;
                        this.yes = false;
                    }
                }
            } else {

                if (check()) {
                    this.check1 = true;
                } else {
                    this.yes = false;
                }
            }
        }

        return false;
    }

    private boolean check() {
        return this.living.isInWater() && !this.living.isRiding() && this.living.getAttackTarget() != null;
    }

    @Override
    public void startExecuting() {
        if (debug) { System.out.println("startExecuting..."); }

        EntityGuardianBoat boat = new EntityGuardianBoat(this.living.world);
        if (debug) { System.out.println("The boat entity is: " + boat); }
        boat.setLocationAndAngles(this.living.posX, this.living.posY, this.living.posZ, this.living.rotationYaw, this.living.rotationPitch);
        if (debug) { System.out.println("setLocationAndAngles processed."); }

        this.living.world.spawnEntity(boat);
        if (debug) { System.out.println("spawnEntity (boat) processed"); }
        this.living.startRiding(boat);
        if (debug) { System.out.println("startRiding (boat) processed"); }

        if (debug) { System.out.println("this.wait is: " + this.wait); }
        this.wait = 0;
        if (debug) { System.out.println("this.wait now is 0: " + this.wait); }
    }
}
