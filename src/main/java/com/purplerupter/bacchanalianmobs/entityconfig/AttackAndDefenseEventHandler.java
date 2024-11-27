package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.etc.utils.GetEvolutionPhase.getCurrentPhase;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;

public class AttackAndDefenseEventHandler {

    private AttackAndDefenseConfig config;

    private static List<String> processedRulesDefense = new ArrayList<>();
    private static List<String> processedRulesAttack = new ArrayList<>();

    public AttackAndDefenseEventHandler(AttackAndDefenseConfig config) {
        this.config = config;
        if (debug) { System.out.println("AttackAndDefenseConfig loaded into EntityEventHandler"); }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) // https://github.com/pWn3d1337/Techguns2/blob/master/src/main/java/techguns/events/TGEventHandler.java#L344
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().getEntityWorld().isRemote) {
            if (debug) { System.out.println("onLivingAttack: the world is remote (client)"); }
            return;
        }

        if (debug) { System.out.println("onLivingAttack event."); }
        Entity target = event.getEntity();
        Entity source = event.getSource().getTrueSource();
        if (debug) { System.out.println("The target is: " + target + " // The source is: " + source); }

        if (target == null) {
            if (debug) { System.out.println("onLivingAttack event failed - the target is null!"); }
            return; }
        if (source == null) {
            if (debug) { System.out.println("onLivingAttack event failed - the source is null!"); }
            return; }

        if (source instanceof EntityPlayer) {
            if (debug) { System.out.println("handleDefense " + source + " // " + target); }
            handleDefense((EntityPlayer) source, target);
        } else if (target instanceof EntityPlayer) {
            if (debug) { System.out.println("handleAttack " + source + " // " + target); }
            handleAttack((EntityPlayer) target, source);
        }
    }

    private void handleDefense(EntityPlayer player, Entity target) {
        if (debug) { System.out.println("handleDefense started!"); }
        byte index = (byte)(getCurrentPhase(target) + 2);
        String mobId = EntityList.getKey(target).toString();
        if (debug) { System.out.println("Index (phase) is: " + index); }
        if (debug) { System.out.println("Mob name is: " + mobId); }

        boolean endLoopDefense = false;
        while (!endLoopDefense) {

            JsonObject defenseRule = config.getMobDefenseRule(processedRulesDefense, mobId, target, index);
            if (defenseRule != null) {
                if (debug) { System.out.println("defenseRule is not null"); }

                if (defenseRule.has("Last rule EMPTY")) {
                    if (debug) { System.out.println("This is the last rule, and it's not match"); }
                    break;
                }
                if (defenseRule.has("Last rule")) {
                    if (debug) { System.out.println("This is the last defense rule"); }
                    endLoopDefense = true;
                }

                System.out.println(defenseRule);
                applyEffectsAndDamage(defenseRule.getAsJsonObject("Damage"), defenseRule.getAsJsonObject("Effects"), player, target);

                processedRulesDefense.add(defenseRule.get("Rule name").getAsString());

            } else { if (debug) { System.out.println("Defense rule is null!!!"); } }
        }
        processedRulesDefense.clear();
    }

    private void handleAttack(EntityPlayer player, Entity source) {
        if (debug) { System.out.println("handleAttack started!"); }
        byte index = (byte)(getCurrentPhase(source) + 2);
        String mobId = EntityList.getKey(source).toString();
        if (debug) { System.out.println("Index (phase) is: " + index); }
        if (debug) { System.out.println("Mob name is: " + mobId); }

        boolean endLoopAttack = false;
        while (!endLoopAttack) {

            JsonObject offenseRule = config.getMobOffenseRule(processedRulesAttack, mobId, source, index);
            if (offenseRule != null) {
                if (debug) { System.out.println("attackRule is not null"); }

                if (offenseRule.has("Last rule EMPTY")) {
                    if (debug) { System.out.println("This is the last rule, and it's not match"); }
                    break;
                }
                if (offenseRule.has("Last rule")) {
                    if (debug) { System.out.println("This is the last rule"); }
                    endLoopAttack = true;
                }

                applyEffectsAndDamage(offenseRule.getAsJsonObject("Damage"), offenseRule.getAsJsonObject("Effects"), player, source);

                processedRulesAttack.add(offenseRule.get("Rule name").getAsString());

            } else { if (debug) { System.out.println("Offense rule is null!!!"); } }
        }
        processedRulesAttack.clear();
    }

    private void applyEffectsAndDamage(JsonObject damage, JsonObject effects, EntityPlayer player, Entity entity) {
        if (debug) { System.out.println("applyEffectsAndDamage started!"); }
        if (damage != null) {
            double amount = damage.get("amount").getAsDouble();
            String type = damage.get("type").getAsString();
            DamageSource source = new DamageSource(damage.get("source").getAsString());
            if (debug) { System.out.println("Damage amount is: " + amount + " // Damage type is: " + type + " // Damage source is: " + source); }

            if (type.equals("hp")) {
                player.attackEntityFrom(source, (float) amount);
            }
            if (type.equals("percent")) {
                float damageAmount = player.getMaxHealth() / 100 * (float) amount;
                player.attackEntityFrom(source, damageAmount);
            } else {
                if (debug) { System.out.println("Error! The damage type is not 'hp' and not 'percent'! It's: " + type); }
            }
        }

        if (effects != null) {
            String effectID = effects.get("effectID").getAsString();
            int duration = effects.get("duration").getAsInt();
            int level = effects.get("level").getAsInt();
            if (debug) { System.out.println(effectID + " // " + level + " // " + duration); }

            if (effectID != null) {
                player.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(effectID), duration, level));
            } else {
                if (debug) { System.out.println("Error! The effectID is null!"); }
            }
        }
    }

}
