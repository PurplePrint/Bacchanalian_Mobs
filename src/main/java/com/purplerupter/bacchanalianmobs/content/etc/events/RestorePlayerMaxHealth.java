package com.purplerupter.bacchanalianmobs.content.etc.events;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import static com.purplerupter.bacchanalianmobs.content.items.constructor.HealthItem.TAG_ADDITIONAL_HEALTH_AMOUNT;

public class RestorePlayerMaxHealth {

    @SubscribeEvent
    public void onPlayerClone(net.minecraftforge.event.entity.player.PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            EntityPlayer old = event.getOriginal();
            EntityPlayer player = event.getEntityPlayer();

            NBTTagCompound oldNBT = old.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (oldNBT != null && oldNBT.hasKey(TAG_ADDITIONAL_HEALTH_AMOUNT)) {

                float additionalHealth = oldNBT.getFloat(TAG_ADDITIONAL_HEALTH_AMOUNT);

                NBTTagCompound newNBT = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
//                if (newNBT == null) {
//                    newNBT = new NBTTagCompound();
//                    player.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG, newNBT);
//                }

                newNBT.setFloat(TAG_ADDITIONAL_HEALTH_AMOUNT, additionalHealth);
                updatePlayerMaxHealth(player, additionalHealth);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        NBTTagCompound persistedData = player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        if (persistedData != null
                && persistedData.hasKey(TAG_ADDITIONAL_HEALTH_AMOUNT)) {

            updatePlayerMaxHealth(player, persistedData.getFloat(TAG_ADDITIONAL_HEALTH_AMOUNT));
        }
    }

    private void updatePlayerMaxHealth(EntityPlayer player, float health) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.setBaseValue(health);
        } else {
//            System.out.println("Error! Player MAX_HEALTH attribute is null!!!");
        }
    }
}
