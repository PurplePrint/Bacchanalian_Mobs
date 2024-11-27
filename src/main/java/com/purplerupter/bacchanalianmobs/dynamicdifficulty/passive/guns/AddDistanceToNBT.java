package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.guns;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import static com.purplerupter.bacchanalianmobs.etc.utils.TagNBT.getOrCreatePersistentData;

public class AddDistanceToNBT {
    public static final String DISTANCES_TAG = "RecentDistancesList";
    public static final String WIDTH_TAG = "RecentWidthList";

    public static void addDistanceToNBT(EntityPlayerMP player, float[] recentDistancesList, float[] recentWidthList) {
        NBTTagCompound persistentData = getOrCreatePersistentData(player);

        persistentData.setTag(DISTANCES_TAG, tagFromArray(recentDistancesList));
        persistentData.setTag(WIDTH_TAG, tagFromArray(recentWidthList));
    }

    private static NBTTagCompound tagFromArray(float[] array) {
        NBTTagCompound tag = new NBTTagCompound();
        for (byte i = 0; i < array.length; i++) {
            tag.setFloat(String.valueOf((byte)(i + 1)), array[i]);
        }
        return tag;
    }
}
