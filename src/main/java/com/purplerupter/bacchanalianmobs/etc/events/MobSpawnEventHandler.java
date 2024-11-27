package com.purplerupter.bacchanalianmobs.etc.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import com.purplerupter.bacchanalianmobs.breakblocks.ai.Digging;
import com.purplerupter.bacchanalianmobs.entityconfig.ScalingPropsConfig;
import com.purplerupter.bacchanalianmobs.etc.CommonConfig;
import com.purplerupter.bacchanalianmobs.pillaring.ai.Pillaring;
import com.purplerupter.bacchanalianmobs.sight.NearbyBeaconEntityAI;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.breakblocks.config.BreakBlocksConfigHandler.getDiggingRuleForMob;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.GetNearbyDifficulty.getNearbyDifficulty;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.GetNearbyDifficulty.getNearbyTime;
import static com.purplerupter.bacchanalianmobs.entityconfig.PotionEffectsConfig.getEffectsRule;
import static com.purplerupter.bacchanalianmobs.entityconfig.ScalingPropsEventHandler.*;
import static com.purplerupter.bacchanalianmobs.equipment.EquipmentConfigHandler.*;
import static com.purplerupter.bacchanalianmobs.etc.MobsBlacklist.MOBS_BLACKLIST;
import static com.purplerupter.bacchanalianmobs.etc.MobsBlacklist.MODS_BLACKLIST;
import static com.purplerupter.bacchanalianmobs.etc.conditions.NearbyPlayers.getNearbyPlayer;
import static com.purplerupter.bacchanalianmobs.etc.utils.GetEvolutionPhase.getCurrentPhase;
import static com.purplerupter.bacchanalianmobs.features.FeaturesSpawnHandler.processFeatures;
import static com.purplerupter.bacchanalianmobs.pillaring.PillaringConfigHandler.getPillaringRule;

public class MobSpawnEventHandler {
    // Использование нескольких обработчиков одного и того же события с большой вероятностью приведет к несоблюдению очередности кода, необходимой для корректной работы всех функций
    // В первую очередь этот единый обработчик нужен для кода, меняющего ИИ мобов

    private static final String EQUIPMENT_SET_TAG = "EquipmentSet";

    private static final String MAIN_HAND = "MAIN_HAND";
    private static final String OFF_HAND = "OFF_HAND";
    private static final String HEAD = "HEAD";
    private static final String CHEST = "CHEST";
    private static final String LEGS = "LEGS";
    private static final String FEET = "FEET";

    private static boolean emptySlots = true;
    private static boolean mainHandProcessed = false;
    private static boolean offHandProcessed = false;
    private static boolean headProcessed = false;
    private static boolean chestProcessed = false;
    private static boolean legsProcessed = false;
    private static boolean feetProcessed = false;
    private static ArrayList<String> processedRulesEquip = new ArrayList<>();

    private static ArrayList<String> processedRulesEffects = new ArrayList<>();
    private static final String TAG_EFFECTS_PROCESSED = "EffectsProcessed";

    private static ArrayList<String> processedRulesScalingProps = new ArrayList<>();
    private ScalingPropsConfig config;


    private static final Random random = new Random();
    private static boolean transformToStray = false;

    private static final String TAG_PROCESSED = "Bacchanalia_ThisEntityAlreadyProcessed";

    public MobSpawnEventHandler(ScalingPropsConfig config) {
        System.out.println("The MobSpawnEventHandler class registered");
        this.config = config;
        System.out.println("Scaling Props config is: " + config);
    }

//    @SubscribeEvent
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() != null && !event.getWorld().isRemote) {

            Entity entity = event.getEntity();

//            // black listed mobs
//            if (entity instanceof EntityGuardianBoat) { return; }
//            if (entity instanceof EntityTransportParrot) { return; }

            if (entity instanceof EntityLiving && !(entity instanceof EntityPlayerMP) && !(entity instanceof EntityArmorStand)) {
                if (debug) { System.out.println("A valid EntityJoinWorldEvent for entity: " + entity); }
                String entityID = EntityList.getKey(entity).toString();

                // black listed mobs - by mod
                if (MODS_BLACKLIST.contains( entityID.split(":")[0]) ) {
                    if (debug) { System.out.println("This mob, " + entityID + ", is black listed by whole mod: " + entityID.split(":")[0]);
                        System.out.println("+++ +++ +++"); }
                    return;
                }

                // black listed mobs - by ID
                if (MOBS_BLACKLIST.contains(entityID)) {
                    if (debug) { System.out.println("This mob, " + entityID + ", is black listed");
                        System.out.println("+++ +++ +++"); }
                    return;
                }

                // Не выполнять код каждый раз заново для старых сущностей во время загрузки мира / сервера. Эти сущности уже были обработаны раньше.
                if (event.getEntity().getEntityData().hasKey(TAG_PROCESSED)) {
                    if (debug) { System.out.println("This mob was processed before. Skip."); }
                    return;
                }

                long stamp1 = 0;
                if (BacchanalianMobs.performanceDebug) {
                    stamp1 = System.currentTimeMillis();
                    System.out.println("EntityJoinWorldEvent started. Entity: " + entity);
                }

                EntityLiving livingEntity = (EntityLiving) entity;
                World world = event.getWorld();
                entity = null;


                if (CommonConfig.enableSightModule) {
                    // TODO список мобов для икс рея из конфига!
                    livingEntity.tasks.addTask(0, new NearbyBeaconEntityAI(livingEntity, world, (short) livingEntity.dimension,
                            (float) livingEntity.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getBaseValue()
                    ));
                }


                if (CommonConfig.enableFeaturesModule) {
                    processFeatures(livingEntity, entityID);
                }


                if (CommonConfig.enableEquipmentModule) {
                    if (isRuleEquipmentForMob(EntityList.getKey(livingEntity).toString())) {

                        NBTTagCompound nbt = livingEntity.getEntityData();
                        while (!nbt.getBoolean(EQUIPMENT_SET_TAG)) {
                            boolean endLoop = false;

                            JsonObject ruleForMob;
                            try {
                                ruleForMob = getEquipmentDataForMob(livingEntity, processedRulesEquip);
                            } catch (Exception e) {
                                if (debug) { System.out.println("Cannot get equipment data for mob: " + livingEntity); }
                                ruleForMob = null;
                                e.printStackTrace();
                            }
                            if (ruleForMob != null) {

                                if (ruleForMob.has("Last rule")) {
                                    if (debug) { System.out.println("This is the last rule"); }
                                    endLoop = true;
                                }

                                for (Map.Entry<String, JsonElement> entry : ruleForMob.getAsJsonObject("Appointments").entrySet()) {
                                    if ((entry.getKey().equals(MAIN_HAND) || entry.getKey().equals(OFF_HAND))
                                            || entry.getKey().equals(HEAD) || entry.getKey().equals(CHEST)
                                            || entry.getKey().equals(LEGS) || entry.getKey().equals(FEET)) {

                                        String itemGroupName = getItemGroupFromAppointment(entry.getValue().getAsString(), entry.getKey(), livingEntity);
                                        if (debug) { System.out.println("Item group name is: " + itemGroupName); }
                                        ItemStack itemStack = getItemStackFromItemGroup(itemGroupName, livingEntity);
                                        if (debug) { System.out.println("Item stack is: " + itemStack); }

                                        if (itemStack == null) { if (debug) { System.out.println("Error! The item stack is null!"); } continue; }

                                        if (debug) { System.out.println("The item stack is: " + itemStack); }
                                        switch (entry.getKey()) {
                                            case "MAIN_HAND":
                                                livingEntity.setHeldItem(EnumHand.MAIN_HAND, itemStack);
                                                mainHandProcessed = true; break;
                                            case "OFF_HAND":
                                                livingEntity.setHeldItem(EnumHand.OFF_HAND, itemStack);
                                                offHandProcessed = true; break;
                                            case "HEAD":
                                                livingEntity.setItemStackToSlot(EntityEquipmentSlot.HEAD, itemStack);
                                                headProcessed = true; break;
                                            case "CHEST":
                                                livingEntity.setItemStackToSlot(EntityEquipmentSlot.CHEST, itemStack);
                                                chestProcessed = true; break;
                                            case "LEGS":
                                                livingEntity.setItemStackToSlot(EntityEquipmentSlot.LEGS, itemStack);
                                                legsProcessed = true; break;
                                            case "FEET":
                                                livingEntity.setItemStackToSlot(EntityEquipmentSlot.FEET, itemStack);
                                                feetProcessed = true; break;
                                            default:
                                                if (debug) { System.out.println("Error! Appoinments in that rule for mob is invalid because it's not any of entity's equipment slot."); } break;
                                        }

                                    } else { if (debug) { System.out.println("Error! The name of a slot is " + entry.getKey() + " and it's not of any entity equipment slot!"); } }
                                }

                                processedRulesEquip.add(ruleForMob.get("Rule name").getAsString());

                            } else { if (debug) { System.out.println("A config rule for mob " + livingEntity + " is null!"); } }

                            if (!(mainHandProcessed && offHandProcessed && headProcessed && chestProcessed && legsProcessed && feetProcessed) && !endLoop) {
                                if (debug) { System.out.println("One of equipment rules was successfully processed. " +
                                        "However, one or more equipment slots of this mob still empty. " +
                                        "Trying to get a new rule from the config!"); }
                                continue;

                            }

                            if ((mainHandProcessed && offHandProcessed && headProcessed && chestProcessed && legsProcessed && feetProcessed) || endLoop) {
                                if (debug) { System.out.println("This is the final equipment for the mob"); }
                                nbt.setBoolean(EQUIPMENT_SET_TAG, true);
                                processedRulesEquip.clear();
                                mainHandProcessed = false; offHandProcessed = false; headProcessed = false; chestProcessed = false; legsProcessed = false; feetProcessed = false;
                            }

                        }
                    }
                }


                if (CommonConfig.enableBreakBlocksModule) {
                    JsonObject ruleObject;
                    try {
                        ruleObject = getDiggingRuleForMob(livingEntity);
                    } catch (Exception e) {
                        if (debug) { System.out.println("Cannot get diging rule for mob: " + livingEntity); }
                        ruleObject = null;
                        e.printStackTrace();
                    }
                    if (ruleObject != null) {

                        // Pointless trick?
                        JsonObject configObject = new JsonObject();
                        boolean breakSoftBlocks = ruleObject.get("Break soft blocks").getAsBoolean();
                        configObject.addProperty("Break soft blocks", breakSoftBlocks);
                        if (breakSoftBlocks) {
                            configObject.addProperty("Tool for soft", ruleObject.get("Required tool for soft blocks").getAsBoolean());
                            configObject.addProperty("Soft multiplier", ruleObject.get("Soft blocks break speed multiplier").getAsFloat());
                            configObject.addProperty("Soft multiplier (no tool)", ruleObject.get("Soft blocks break speed multiplier (without tool)").getAsFloat());
                            boolean breakStoneBlocks = ruleObject.get("Break stone blocks").getAsBoolean();
                            configObject.addProperty("Break stone blocks", breakStoneBlocks);
                            if (breakStoneBlocks) {
                                configObject.addProperty("Tool for stone", ruleObject.get("Required tool for stone blocks").getAsBoolean());
                                configObject.addProperty("Stone multiplier", ruleObject.get("Stone blocks break speed multiplier").getAsFloat());
                                configObject.addProperty("Stone multiplier (no tool)", ruleObject.get("Stone blocks break speed multiplier (without tool)").getAsFloat());
                            }
                        }

                        livingEntity.tasks.addTask(3, new Digging(livingEntity, configObject));

                    } else { if (debug) { System.out.println("Error, the ruleObject is null!"); } }
                }


                if (CommonConfig.enablePillaringModule) {
                    JsonObject ruleObject;
                    try {
                        ruleObject = getPillaringRule(livingEntity);
                    } catch (Exception e) {
                        if (debug) { System.out.println("Cannot get pillaring rule for mob: " + livingEntity); }
                        ruleObject = null;
                        e.printStackTrace();
                    }
                    if (ruleObject != null) {
                        livingEntity.tasks.addTask(4, new Pillaring(livingEntity, ruleObject.get("Block").getAsString()));
                    }
                }

//                livingEntity.tasks.addTask(1, new ParrotRidingTrigger(livingEntity));

                // TODO: NBT тег чтобы не назначать эффекты тому же мобу при перезапуске сервера
                if (CommonConfig.enableEntityConfigModule) {
                    boolean endLoopEffect = false;
                    while (!endLoopEffect) {

                        JsonObject effectsRule = getEffectsRule(processedRulesEffects, livingEntity);
                        if (effectsRule != null) {

                            if (effectsRule.has("Last rule EMPTY")) {
                                if (debug) { System.out.println("This is the last rule, and it's not match"); }
                                break;
                            }
                            if (effectsRule.has("Last rule")) {
                                if (debug) { System.out.println("This is the last effects rule"); }
                                endLoopEffect = true;
                            }

                            System.out.println("Effects rule is: " + effectsRule);
                            for (Map.Entry<String, JsonElement> entry : effectsRule.entrySet()) {
                                if (debug) { System.out.println("Entry is: " + entry); }

                                if (entry.getKey().equals("Rule name")) {
                                    continue;
                                }

                                byte amplifier = 0;
                                boolean infinite = false;
                                int duration = 60;
                                if (entry.getValue().isJsonObject()) {

                                    if (entry.getValue().getAsJsonObject().has("Level")) {
                                        amplifier = entry.getValue().getAsJsonObject().get("Level").getAsByte();
                                    }

                                    if (entry.getValue().getAsJsonObject().has("Infinite")) {
                                        infinite = entry.getValue().getAsJsonObject().get("Infinite").getAsBoolean();
                                    }
                                    if (infinite) {
                                        duration = 1_000_000; // Not infinite but enough.
                                    } else { if (entry.getValue().getAsJsonObject().has("Duration")) {
                                        duration = entry.getValue().getAsJsonObject().get("Duration").getAsInt();
                                    } }
                                }

                                if (!livingEntity.getEntityData().hasKey(TAG_EFFECTS_PROCESSED)
                                        || !livingEntity.getEntityData().getBoolean(TAG_EFFECTS_PROCESSED)
                                        || duration == 1_000_000) {

                                    PotionEffect apply = new PotionEffect(Potion.getPotionFromResourceLocation(entry.getKey()), duration * 20, amplifier);
                                    if (debug) { System.out.println("Applying this PotionEffect: " + apply.toString()); }
                                    livingEntity.addPotionEffect(apply);
                                }

                            }
                            livingEntity.getEntityData().setBoolean(TAG_EFFECTS_PROCESSED, true);

                            processedRulesEffects.add(effectsRule.get("Rule name").getAsString());

                        } else if (debug) { System.out.println("Effects rule is null!!!"); }

                        if (debug) {
                            System.out.println("List of potion effects on this entity: ");
                            for (PotionEffect potionEffect : livingEntity.getActivePotionEffects()) {
                                System.out.println(potionEffect.toString());
                            }
                        }
                    }
                    processedRulesEffects.clear();
                }


                // Scaling Props - health
                if (CommonConfig.enableEntityConfigModule && this.config != null) {
                    System.out.println("Scaling Props started");
                    byte phase = getCurrentPhase(livingEntity);
                    byte phaseIndex = (byte)(phase + 2); // +2 because SRP phases is from -2 to 10, but multipliers for phases is from 0 to 12
                    if (debug) { System.out.println("The phase is: " + phase); }

                    boolean endLoopScaleHealth = false;
                    while (!endLoopScaleHealth) {

                        JsonObject rule = config.getRuleForMob(entityID, livingEntity, phaseIndex, processedRulesScalingProps);
                        if (debug) { System.out.println("The config rule is: " + rule); }

                        if (rule != null) {
                            if (debug) { System.out.println("The rule is not null"); }

                            if (rule.has("Last rule EMPTY")) {
                                if (debug) { System.out.println("This is the last rule, and it's not match"); }
                                break;
                            }
                            if (rule.has("Last rule")) {
                                if (debug) { System.out.println("This is the last rule"); }
                                endLoopScaleHealth = true;
                            }

                            double scalingFactor = 1;

                            if (rule.has("Simple scale")) {
                                // По какой-то причине, множители 'Simple scale' повторно применяются при перезаходе в мир.
                                // Это не влияет на множители по времени и сложности. Как показала отладка - множитель сложности находит '0' несмотря на наличие игрока со сложностью выше нуля поблизости.
                                if (getNearbyPlayer(livingEntity) != null) {
                                    float simpleScale = rule.get("Simple scale").getAsFloat();
                                    if (debug) { System.out.println("This rule has simple scale. It is: " + simpleScale); }
                                    scalingFactor *= simpleScale;
                                }
                            }

                            if (rule.has("ChangeByDifficulty")) {
                                if (debug) { System.out.println("ChangeByDifficulty..."); }
                                JsonObject changeByDifficulty = rule.getAsJsonObject("ChangeByDifficulty");
                                if (debug) { System.out.println("That rule is: "); System.out.println(changeByDifficulty); }

                                double minDiff = changeByDifficulty.get("DifficultyMin").getAsDouble();
                                double maxDiff = changeByDifficulty.get("DifficultyMax").getAsDouble();
                                double targetDiff = changeByDifficulty.get("TargetDifficulty").getAsDouble();
                                float targetMultiplier = getTargetMultiplier(changeByDifficulty, phaseIndex);
                                if (debug) { System.out.println("Parsed: ");
                                    System.out.println(minDiff + " // " + maxDiff + " // " + targetDiff + " // " + targetMultiplier); }

                                double nearbyDifficulty = getNearbyDifficulty(livingEntity, (byte)2);
                                if (debug) { System.out.println("The nearby difficulty is: " + nearbyDifficulty); }

                                if (nearbyDifficulty >= minDiff) {
                                    if (debug) { System.out.println("The nearby difficulty is more than 'minDiff' (" + minDiff + ")"); }

                                    if (nearbyDifficulty > maxDiff) {
                                        if (debug) { System.out.println("The nearby difficulty is more than 'maxDiff' (" + maxDiff + ")"); }
                                        nearbyDifficulty = maxDiff;
                                    }

                                    if (debug) { System.out.println("Changing scalingFactor..."); }
                                    scalingFactor = (nearbyDifficulty / targetDiff) * targetMultiplier;
                                    if (debug) { System.out.println("scalingFactor is: " + scalingFactor); }

                                } else { if (debug) { System.out.println("The nearby difficulty is less than 'minDiff' (" + minDiff + ")"); } }

                            } else { if (debug) { System.out.println("This rule has no ChangeByDifficulty"); } }

                            if (rule.has("ChangeByTime")) {
                                JsonObject changeByTime = rule.getAsJsonObject("ChangeByTime");
                                if (debug) { System.out.println("That rule is: "); System.out.println(changeByTime); }

                                long minTime = changeByTime.get("TimeMin").getAsLong();
                                long maxTime = getTimeMax(changeByTime);
                                long targetTime = changeByTime.get("TargetTime").getAsLong();
                                float targetMultiplier = getTargetMultiplier(changeByTime, phaseIndex);
                                if (debug) { System.out.println("Parsed: ");
                                    System.out.println(minTime + " // " + maxTime + " // " + targetTime + " // " + targetMultiplier); }

                                long nearbyTime = getNearbyTime(livingEntity);

                                if (nearbyTime >= minTime) {
                                    if (debug) { System.out.println("The time is more than 'minTime' (" + minTime + ")"); }

                                    if (nearbyTime > maxTime) {
                                        if (debug) { System.out.println("The time is more than 'maxTime' (" + maxTime + ")"); }
                                        nearbyTime = maxTime;
                                    }

                                    float scale = ((float)(nearbyTime / targetTime) * targetMultiplier);
                                    if (debug) { System.out.println("That ChangeByTime rule scale the scalingFactor of entity's health by: " + scale); }
                                    scalingFactor *= scale;

                                } else { if (debug) { System.out.println("The time is less than 'minTime' (" + minTime + ")"); } }

                            } else { if (debug) { System.out.println("This rule has no ChangeByTime"); } }

                            if (debug) { System.out.println("Calling scaleHealth method..."); System.out.println("The scaling factor is: " + scalingFactor); }
                            scaleHealth(livingEntity, scalingFactor);

                            processedRulesScalingProps.add(rule.get("Rule name").getAsString());
                            if (debug) { System.out.println("The list of all processed rules is: " + processedRulesScalingProps); }

                        } else { if (debug) { System.out.println("The rule is null or conditions not passed!"); } }

                    }
                    processedRulesScalingProps.clear();
                    if (debug) { System.out.println("Processed rules list has been cleared. Now it is: " + processedRulesScalingProps); }

                } else {
                    if (debug) { System.out.println("Skipping Scaling Props."); }
                }

//                if (livingEntity instanceof EntityZombie) {
//                    livingEntity.tasks.addTask(1, new EntityAICheckTargetAndRun(livingEntity, 80));
//                }

//                if (livingEntity instanceof EntitySpider) {
//                    livingEntity.tasks.addTask(3, new WebPlacement((EntitySpider) livingEntity));
//                }
//
//                if (livingEntity instanceof EntityZombie) {
//                    livingEntity.tasks.addTask(3, new LeapAI(livingEntity, livingEntity.getEntityWorld()));
//                }
//
//                if (livingEntity instanceof EntityCreeper) {
//                    livingEntity.tasks.addTask(1, new Breach(livingEntity));
//                }

                livingEntity.getEntityData().setBoolean(TAG_PROCESSED, true);

                if (BacchanalianMobs.performanceDebug) {
                    long stamp2 = System.currentTimeMillis();
                    int result = (int) (stamp2 - stamp1);
                    System.out.println("EntityJoinWorldEvent was finished. It took: " + result + " ms. Entity: " + livingEntity);
                }
            }


        } else {
            if (event.getEntity() == null) { if (debug) { System.out.println("The entity from event is null"); } }
            if (event.getWorld().isRemote) { if (debug) { System.out.println("The world from event is remote"); } }
        }
    }
}