package com.purplerupter.bacchanalianmobs.etc.init;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.purplerupter.bacchanalianmobs.content.ItemsAll.ALL_ITEMS;
import static com.purplerupter.bacchanalianmobs.content.ItemsAll.addItemsToList;

public class InitItems {
    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {

        addItemsToList();

        for (Item item : ALL_ITEMS) {
            event.getRegistry().register(item);
        }
    }
}
