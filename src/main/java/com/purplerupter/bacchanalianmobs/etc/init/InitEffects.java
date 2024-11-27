package com.purplerupter.bacchanalianmobs.etc.init;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.purplerupter.bacchanalianmobs.content.EffectsAll.ALL_POTIONS;
import static com.purplerupter.bacchanalianmobs.content.EffectsAll.addPotionsToList;

public class InitEffects {
    @SubscribeEvent
    public void registerEffects(RegistryEvent.Register<Potion> event) {

        addPotionsToList();

        for (Potion potion : ALL_POTIONS) {
            event.getRegistry().register(potion);
        }
    }
}
