package com.purplerupter.bacchanalianmobs.features.spider.web;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Utils {
    public static boolean lawfulCollision(World world, BlockPos pos) {
        System.out.println("The world is: " + world);
        if (!world.isAirBlock(pos)) {
            System.out.println(world.getBlockState(pos));
            System.out.println("It is an air block!");
            return false; }

        return true;
    }

    public static boolean lawfulGravity(World world, BlockPos pos) {
        System.out.println("The world is: " + world);
        if (!world.isAirBlock(pos.down())) {
            return true; }
        if (!world.isAirBlock(pos.up())) {
            return true; }
        if (!world.isAirBlock(pos.north())) {
            return true; }
        if (!world.isAirBlock(pos.south())) {
            return true; }
        if (!world.isAirBlock(pos.east())) {
            return true; }
        if (!world.isAirBlock(pos.west())) {
            return true; }

        return false;
    }
}
