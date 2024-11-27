package com.purplerupter.bacchanalianmobs.sight;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class XRayFromPlayer {
    private static final Map< Short, Map<String, Short> > beaconPlayersDatabase = new HashMap<>();
//    private static final Map<String, Integer> beaconPlayersTimeBase = new HashMap<>();

    public static void addPlayerToBeacons(EntityPlayer player, short radius) {
        // If not exist, create new entry for dimension
        if (!beaconPlayersDatabase.containsKey((short)player.dimension)) {
            beaconPlayersDatabase.put((short)player.dimension, new HashMap<>());
        }
        // If not exist, create new entry for player
        if (!beaconPlayersDatabase.get((short)player.dimension)
                .containsKey(player.getName())) {
            beaconPlayersDatabase.get((short)player.dimension)
                    .put(player.getName(), radius);
        }
    }

    public static void removePlayerFromBeacons(EntityPlayer player) {
        try {
//            System.out.println(beaconPlayersDatabase);
            beaconPlayersDatabase.get((short) player.dimension).remove(player.getName());
//            System.out.println(beaconPlayersDatabase);
        } catch (Exception e) {
//            System.out.println("Error! Unable to remove player " + player.getName() + " from the Beacon-sight database!");
        }
    }

    public static void updatePlayerDimension(EntityPlayer player) {
        short prevDim = Short.MIN_VALUE;
        for (short dim : beaconPlayersDatabase.keySet()) {
            if (beaconPlayersDatabase.get(dim).containsKey(player.getName())) {
                prevDim = dim;
                break;
            }
        }

        if (prevDim != player.dimension && prevDim != Short.MIN_VALUE) {
            beaconPlayersDatabase.put((short)player.dimension, Collections.singletonMap(player.getName(),
                    beaconPlayersDatabase.get(prevDim).get(player.getName()) )
            );
            beaconPlayersDatabase.get(prevDim).remove(player.getName());
        }
    }

    public static boolean hasBeaconsInDimension(short dimID) {
        if (beaconPlayersDatabase.containsKey(dimID)) {
            return !beaconPlayersDatabase.get(dimID).isEmpty();
        } else {
            return false;
        }
    }

    public static Map<String, Short> beaconPlayers(short dimID) {
        return beaconPlayersDatabase.get(dimID);
    }

    public static short getBeaconRadiusForPlayer(short dimension, String name) {
        return beaconPlayersDatabase.get(dimension).get(name);
    }

    public static boolean isBeacon(short dimension, EntityPlayer player) {
        if (!hasBeaconsInDimension(dimension)) {
            return false;
        }

        String playerName = player.getName();
        for (String name : beaconPlayers(dimension).keySet()) {
            if (name.equals(playerName)) {
                return true;
            }
        }

        return false;
    }
}
