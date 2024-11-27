package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.etc.proxy.CommonProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.GetEvolutionPhase.getCurrentPhase;

public class DamageEventHandler {

    private static List<String> processedRulesSource = new ArrayList<>();
    private static List<String> processedRulesEntity = new ArrayList<>();

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        Entity attacker = event.getSource().getTrueSource();
        String mobID;
        if (entity instanceof EntityPlayerMP) {
            mobID = "minecraft:player";
        } else {
            mobID = EntityList.getKey(entity).toString();
        }
        String damageSource = event.getSource().damageType;
        if (debug) { System.out.println("Mob: " + mobID + " get damage from: " + damageSource + " source"); }

        // Получаем правило для DamageSource
        float multiplier = 1F;
        boolean endLoopSource = false;
        while (!endLoopSource) {

            JsonObject damageSourceRule = CommonProxy.damageConfig.getDamageSourceRule(processedRulesSource, mobID, entity, damageSource, (byte) 0);
            if (damageSourceRule != null) {
                if (debug) { System.out.println("damageSourceRule is not null"); }

                if (damageSourceRule.has("Last rule EMPTY")) {
                    if (debug) { System.out.println("This is the last rule, and it's not match"); }
                    break;
                }
                if (damageSourceRule.has("Last rule")) {
                    if (debug) { System.out.println("This is the last rule"); }
                    endLoopSource = true;
                }

                if (damageSource.equals("mob") || damageSource.equals("player")) {
                    if (debug) { System.out.println("damageSource is a 'mob' or a 'player': Change damage by source is cancel!"); }

                } else {
                    multiplier *= damageSourceRule.getAsJsonObject("DamageChanges").get(damageSource).getAsFloat();
                    if (debug) { System.out.println("Multiplier updated - now:" + multiplier); }
                }

                processedRulesSource.add(damageSourceRule.get("Rule name").getAsString());

            } else { if (debug) { System.out.println("damageSourceRule is null! MobID is: " + mobID); break; } }
        }
        processedRulesSource.clear();

        event.setAmount(event.getAmount() * multiplier);
        if (debug) { System.out.println("Event damage has changed: " + event.getAmount()); }

        if (attacker != null) {
            if (debug) { System.out.println("The attacker is not null"); }

            String attackerID;
            if (attacker instanceof EntityPlayerMP) {
                if (debug) { System.out.println("attackerID is 'minecraft:player'"); }
                attackerID = "minecraft:player";
            } else {
                if (debug) { System.out.println("attackerID is entity: "); }
                attackerID = EntityList.getKey(attacker).toString();
                if (debug) { System.out.println(attackerID); }
            }

            float mult = 1F;
            boolean endLoopEntity = false;
            while (!endLoopEntity) {

                JsonObject entityDamageRule = CommonProxy.damageConfig.getEntityDamageRule(processedRulesEntity, attackerID, mobID, entity, getCurrentPhase(entity));
                if (entityDamageRule != null) {
                    if (debug) { System.out.println("entityDamageRule is not null"); }

                    if (entityDamageRule.has("Last rule EMPTY")) {
                        if (debug) { System.out.println("This is the last rule, and it's not match"); }
                        break;
                    }
                    if (entityDamageRule.has("Last rule")) {
                        if (debug) { System.out.println("This is the last rule"); }
                        endLoopEntity = true;
                    }

                    mult *= entityDamageRule.get("multiplier").getAsFloat();
                    if (debug) { System.out.println("The multiplier is: " + mult); }

                    processedRulesEntity.add(entityDamageRule.get("Rule name").getAsString());
                } else { if (debug) { System.out.println("entityDamageRule is null"); } break; }
            }
            processedRulesEntity.clear();

        }
    }
}
