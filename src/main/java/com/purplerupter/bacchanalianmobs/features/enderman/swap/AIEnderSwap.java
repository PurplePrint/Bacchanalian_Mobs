package com.purplerupter.bacchanalianmobs.features.enderman.swap;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class AIEnderSwap extends EntityAIBase {

    private final EntityEnderman enderman;

    private int timer = 0;
    private final int startInterval = 80;
    private final int cooldown;
    private int cooldownCurrent;

    private final float minDistanceSQ;
    private final float maxDistanceSQ;

    private final boolean playSound;
    private final List<PotionEffect> debuffs;

    private int counter = 0;

    public AIEnderSwap(EntityEnderman enderman, short minDistance, short maxDistance, int cooldown, boolean playSound, List<PotionEffect> debuffs) {
//        System.out.println("AIEnderSwap called: " + enderman + " // " + minDistance + " // " + maxDistance + " // " + cooldown + " // " + playSound + " // " + debuffs);
        this.enderman = enderman;

        this.minDistanceSQ = minDistance * minDistance;
        this.maxDistanceSQ = maxDistance * maxDistance;

        this.cooldown = cooldown;
        this.cooldownCurrent = cooldown;

        this.playSound = playSound;
        this.debuffs = debuffs;
    }

    public boolean shouldExecute() { return true; }

    public void updateTask() {
        this.timer++;
        if (this.timer >= this.startInterval
                && this.cooldownCurrent >= this.cooldown) {
            if (this.enderman.getAttackTarget() != null && this.enderman.getAttackTarget() instanceof EntityPlayer) {

                EntityPlayer player = (EntityPlayer) this.enderman.getAttackTarget();
                if (this.enderman.getDistanceSq(player) >= this.minDistanceSQ && this.enderman.getDistanceSq(player) <= this.maxDistanceSQ) {

                    if (player.isInWater() || player.isInLava() || player.isAirBorne) {
//                        System.out.println("player is unavailable");
                        return;
                    } else {
                        BlockPos playerPos = new BlockPos(player);
                        BlockPos enderPos = new BlockPos(this.enderman);

                        this.cooldownCurrent = 0;
                        this.enderman.attemptTeleport(playerPos.getX(), playerPos.getY(), playerPos.getZ());
                        player.attemptTeleport(enderPos.getX(), enderPos.getY(), enderPos.getZ());

                        this.counter++;
//                        player.sendMessage(new TextComponentString("Swap N" + this.counter + "!"));

                        if (this.playSound) {
                            playSwapSound(player);
                        }

                        if (this.debuffs != null && !this.debuffs.isEmpty()) {
                            for (PotionEffect effect : this.debuffs) {
                                player.addPotionEffect(effect);
                            }
                        }
//                        else { System.out.println("Debuffs list is empty"); }
                    }

                }

            }
            this.timer = 0;
        }

        this.cooldownCurrent++;
    }

    private void playSwapSound(EntityPlayer player) {
        if (player.world.isRemote) return;

        player.world.playSound(
                null, // Указываем, что никто не является конкретным инициатором
                player.getPosition().getX() + 0.5F, // Центр игрока
                player.getPosition().getY() + 1.0F, // Чуть выше центра
                player.getPosition().getZ() + 0.5F,
                SoundEvents.ENTITY_ENDERMEN_TELEPORT, // TODO другой звук
                SoundCategory.PLAYERS,
                1.0F, // Громкость
                1.0F  // Высота
        );
    }
}
