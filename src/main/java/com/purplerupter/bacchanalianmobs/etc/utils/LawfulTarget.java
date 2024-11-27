package com.purplerupter.bacchanalianmobs.etc.utils;

import net.minecraft.entity.player.EntityPlayer;

public class LawfulTarget {
    public static boolean lawfulTarget(EntityPlayer player, short dimension) {
        if (player == null) {
            return false;
        }

        if (player.isDead) {
            return false; }
        if (player.dimension != dimension) {
            return false; }
        if (player.isCreative() || player.isSpectator()) {
            return false; }

        return true;
    }

    public static boolean lawfulTarget(EntityPlayer player) {
        if (player == null) {
            return false;
        }

        if (player.isDead) {
            return false; }
        if (player.isCreative() || player.isSpectator()) {
            return false; }

        return true;
    }
}
