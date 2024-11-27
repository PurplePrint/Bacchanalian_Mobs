package com.purplerupter.bacchanalianmobs.etc.utils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class TagNBT {

    public static NBTTagCompound getOrCreatePersistentData(EntityPlayerMP player) {
        NBTTagCompound playerData = player.getEntityData();
        if (!playerData.hasKey(EntityPlayerMP.PERSISTED_NBT_TAG)) {
            NBTTagCompound persistentData = new NBTTagCompound();
            playerData.setTag(EntityPlayerMP.PERSISTED_NBT_TAG, persistentData);
            return persistentData;
        } else {
            return playerData.getCompoundTag(EntityPlayerMP.PERSISTED_NBT_TAG);
        }
    }
}
