// TODO комментарии в конфигах

package com.purplerupter.bacchanalianmobs.etc.utils;

import com.google.gson.JsonObject;

public class CleanJson {
    public static JsonObject cleanJsonRule(JsonObject rule) {
        if (rule.has("Mob list")) {
            rule.remove("Mob list");
        }
        if (rule.has("Conditions")) {
            rule.remove("Conditions");
        }

        return rule;
    }
}
