package com.purplerupter.bacchanalianmobs.features.spider.web;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TempWebDatabase {
    private static final Map<Short, List< Map<Map<Integer, Long>, BlockPos > >> TEMP_WEB_DATABASE = new HashMap<>();
    public static boolean WEB_DATABASE_EXIST = false;

    public static void addTempWeb(short dimension, BlockPos pos, int lifespan, long worldAge) {
        WEB_DATABASE_EXIST = true;

        if (!TEMP_WEB_DATABASE.containsKey(dimension)) {
            TEMP_WEB_DATABASE.put(dimension, new ArrayList<>());
        }

        List< Map<Map<Integer, Long>, BlockPos > > webList = TEMP_WEB_DATABASE.computeIfAbsent(dimension, k -> new ArrayList<>());

        webList.add(new HashMap<Map<Integer, Long>, BlockPos >() {{
            put(new HashMap<Integer, Long>() {{
                put(lifespan, worldAge);
            }}, pos);
        }});
    }

    public static Map<Short, List< Map<Map<Integer, Long>, BlockPos > >> getTempWebDatabase() {
        return TEMP_WEB_DATABASE;
    }

//    public static void removeTempWebEntry(short dimension, Map< Map<Integer, Long>, BlockPos > entry, int index) {
    public static void removeTempWebEntry(short dimension, int index) {
//        System.out.println("Try to remove dead block...");
//        TEMP_WEB_DATABASE.get(dimension).remove(index);
        TEMP_WEB_DATABASE.get(dimension).get(index).clear();
    }
}
