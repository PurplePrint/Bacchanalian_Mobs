package com.purplerupter.bacchanalianmobs.features.general.riding;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class Taxi extends EntityAIBase {

    private EntityLiving taxiMob;
    private World world;

    private int distanceToSearch;
    private int interval;

    private byte distanceToMount;
    private byte chanceToMount;
    private byte chancePerMob;

    private List<EntityLiving> entities;

    private int timer;
    private int cooldown;
    private int cooldownMax;
    private boolean setCooldown;
    private boolean riding;

    private static final Random random = new Random();

    public Taxi(EntityLiving taxiMob, World world, int distanceToSeatch, int interval,
                byte distanceToMount, byte chanceToMount, byte chancePerMob, int cooldownMax) {
        this.taxiMob = taxiMob;
        this.world = world;
        this.distanceToSearch = distanceToSeatch;
        this.interval = interval;

        this.distanceToMount = distanceToMount;
        this.chanceToMount = chanceToMount;
        this.chancePerMob = chancePerMob;

        this.entities = null;
        this.timer = 0;

        this.cooldown = 0;
        this.cooldownMax = cooldownMax;
        this.setCooldown = false;
        this.riding = false;
    }

    public boolean shouldExecute() {
        if (this.taxiMob.isDead) {
            return false;
        }

        return true;
    }

    public void updateTask() {
        if (this.setCooldown) {
            this.cooldown++;
            if (this.cooldown >= this.cooldownMax) {
                this.setCooldown = false;
            }
        } else {

            this.timer++;
            if (this.timer >= this.interval && this.interval > 0) {
//                System.out.println("Let's check!");

                if (!this.taxiMob.getPassengers().isEmpty() || this.taxiMob.isRiding()) {
//                    System.out.println("Failed");
                    return;
                }

                if (random.nextInt() <= this.chanceToMount) {

                    this.entities = world.getEntitiesWithinAABB(EntityLiving.class, this.taxiMob.getEntityBoundingBox().grow(this.distanceToSearch));
                    for (EntityLiving potentialPassenger : this.entities) {
                        if (potentialPassenger == this.taxiMob) { continue; }
//                        System.out.println("Entity list is: " + this.entities);

                        if (!potentialPassenger.isDead
                                && !potentialPassenger.isRiding() && !potentialPassenger.isBeingRidden()) {
//                            System.out.println("Next...");
                            if (random.nextInt() <= this.chancePerMob) {
//                                System.out.println("Next...");

                                potentialPassenger.startRiding(this.taxiMob);
                                this.riding = true;
                            }
                        }
                    }
                    if (!this.riding) {
//                        System.out.println("Cooldown");
                        this.setCooldown = true;
                    }

                } else {
                    this.setCooldown = true;
                }


                this.timer = 0;
            }

        }
    }
}
