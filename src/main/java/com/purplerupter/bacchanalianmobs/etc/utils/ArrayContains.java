package com.purplerupter.bacchanalianmobs.etc.utils;

import com.google.gson.JsonArray;

public class ArrayContains {
    public static boolean arrayContains(JsonArray array, String value) {
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).getAsString().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean arrayContains(JsonArray array, int value) {
        if (array != null) {
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).getAsInt() == value) {
                    return true;
                }
            }
        }
        return false;
    }
}
