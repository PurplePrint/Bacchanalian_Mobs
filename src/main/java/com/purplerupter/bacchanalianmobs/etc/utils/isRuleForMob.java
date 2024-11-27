package com.purplerupter.bacchanalianmobs.etc.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

import static com.purplerupter.bacchanalianmobs.etc.utils.ArrayContains.arrayContains;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class isRuleForMob {
    public static boolean isRuleForMob(JsonObject configData, String mobID) {
        for (Map.Entry<String, JsonElement> entry : configData.getAsJsonObject("Rules").entrySet()) {
            System.out.println(entry);
            System.out.println(entry.getValue().getAsJsonObject());
            System.out.println(entry.getValue().getAsJsonObject().get("Mob List"));
            System.out.println(entry.getValue().getAsJsonObject().get("Mob List").getAsJsonArray());
//            if (arrayContains(entry.getValue().getAsJsonObject().getAsJsonArray("Mob list"), mobID)) {
            if (arrayContains(entry.getValue().getAsJsonObject().get("Mob List").getAsJsonArray(), mobID)) {
                if (debug) { System.out.println("Rule for mob is true"); }
                return true;
            }
        }

        if (debug) { System.out.println("Rule for mob is false"); }
        return false;
    }
}
