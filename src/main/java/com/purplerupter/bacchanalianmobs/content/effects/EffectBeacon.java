package com.purplerupter.bacchanalianmobs.content.effects;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;

import static com.purplerupter.bacchanalianmobs.sight.XRayFromPlayer.addPlayerToBeacons;
import static com.purplerupter.bacchanalianmobs.sight.XRayFromPlayer.removePlayerFromBeacons;

public class EffectBeacon extends Potion {

    public static final String name = "Beacon";
    public static final String beaconID = "effect_beacon";

    public EffectBeacon() {
        super(true, 0x5A6C81); // TODO цвет
    }

    @Override
    public Potion setPotionName(String p_76390_1_) {
        return super.setPotionName(name);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap attributeMap, int amplifier) {
        super.applyAttributesModifiersToEntity(entity, attributeMap, amplifier);

        if (entity instanceof EntityPlayer) {
            addPlayerToBeacons((EntityPlayer)entity, (short)(amplifier*2));
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap attributeMap, int amplifier) {
        super.removeAttributesModifiersFromEntity(entity, attributeMap, amplifier);
        if (entity instanceof EntityPlayer) {
            removePlayerFromBeacons((EntityPlayer)entity);
        }
    }
}
