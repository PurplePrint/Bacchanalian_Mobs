package com.purplerupter.bacchanalianmobs.etc.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.etc.utils.DimensionTimeTracker.TIME_SPENT_TAG;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.debug;
import static com.purplerupter.bacchanalianmobs.etc.utils.TagNBT.getOrCreatePersistentData;

public class CommandShowDimensionTime extends CommandBase {

    @Override
    public String getName() {
        return "showdimensiontime";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/showdimensiontime - Shows the time spent in each dimension.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            NBTTagCompound persistentData = getOrCreatePersistentData(player);

//            DimensionTimeTracker.updateTimeSpent(player);

            Map<Short, Long> timeSpent = getTimeSpentMap(persistentData);

            player.sendMessage(new TextComponentString("Time spent in each dimension:"));

            for (Map.Entry<Short, Long> entry : timeSpent.entrySet()) {
                short dimensionId = entry.getKey();
                long timeInSeconds = entry.getValue();
                String timeFormatted = formatTime(timeInSeconds);

                player.sendMessage(new TextComponentString("Dimension " + dimensionId + ": " + timeFormatted));
            }
        } else {
            sender.sendMessage(new TextComponentString("This command can only be used by a player."));
        }
    }

    private Map<Short, Long> getTimeSpentMap(NBTTagCompound persistentData) {
        Map<Short, Long> timeSpent = new HashMap<>();
        if (persistentData.hasKey("TimeSpentInDimensions")) {
            NBTTagCompound timeSpentTag = persistentData.getCompoundTag(TIME_SPENT_TAG);
            if (debug) { System.out.println("Time spent tag is: " + timeSpentTag); }
            for (String key : timeSpentTag.getKeySet()) {
                short dimensionId = Short.parseShort(key);
                long time = timeSpentTag.getInteger(key);
                timeSpent.put(dimensionId, time);
            }
        }
        return timeSpent;
    }

    private String formatTime(long timeInSeconds) {
        int hours = (int)(timeInSeconds / 72000);
        int minutes = (int) ((timeInSeconds % 72000) / 1200);
        int seconds = (int) ((timeInSeconds % 1200)) / 20;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void register(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandShowDimensionTime());
    }
}
