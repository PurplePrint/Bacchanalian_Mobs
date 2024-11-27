// it's not really a death event handler: it's hurt event handler... But in real use - mobs will break turrets when a turret entity is dead
package com.purplerupter.bacchanalianmobs.breakblocks;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import techguns.entities.npcs.NPCTurret;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.breakblocks.config.BreakBlocksConfigHandler.getDiggingRuleForMob;
import static com.purplerupter.bacchanalianmobs.breakblocks.config.TurretBlocksConfig.turretBlocks;
import static com.purplerupter.bacchanalianmobs.etc.utils.GetAttackReach.getAttackReach;

public class TurretDeathEventHandler {
    public TurretDeathEventHandler() {
        if (debug) { System.out.println("TurretDeathEventHandler registered"); }
    }

    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof NPCTurret && event.getSource().getTrueSource() != null) {
            if ((event.getSource().getTrueSource() instanceof EntityLiving) &&
                    !(event.getSource().getTrueSource() instanceof EntityPlayerMP) && !(event.getSource().getTrueSource() instanceof EntityArmorStand)) {
                EntityCreature turret = (EntityCreature) event.getEntity();
                EntityLiving destroyer = (EntityLiving) event.getSource().getTrueSource();
                if (debug) { System.out.println("Entity " + destroyer + " probably may destroy a turret: " + turret); }

                JsonObject turretDestroyConfig = getDiggingRuleForMob(destroyer, (byte) 1);
                if (turretDestroyConfig == null) {
                    if (debug) { System.out.println("The turretDestroyConfig is null (may be - this mob is forbidden to destroy a turrets)"); } return; }
                if (!turretDestroyConfig.get("Turret destroying").getAsBoolean()) {
                    if (debug) { System.out.println("Turret destroying for entity: " + destroyer + " is not allowed"); } return; }

                float destroyerReach = getAttackReach(destroyer); // Protection from ranged mobs
                float destroyerReachMod = destroyerReach * 1.1F; // Add a small margin
                if (debug) { System.out.println("The entity " + destroyer + " hit entity turret from " + destroyer.getDistance(turret) + " distance.");
                System.out.println("And the lawful attack reach, calculated as (half of the mob width + 3), is: " + destroyerReach + ". " +
                        "This value increased by 1.1 up to" + destroyerReachMod + "and it will be used."); }
                DestroyTurrets destroyTurrets = new DestroyTurrets(turretDestroyConfig, turretBlocks, turret, destroyer, destroyerReach);
                destroyTurrets.findAndDestroyTurret();
            }
        }
    }
}
