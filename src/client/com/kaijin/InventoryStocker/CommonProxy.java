package com.kaijin.InventoryStocker;

import net.minecraft.src.*;

public class CommonProxy
{
    public static World PacketHandlerGetWorld(NetworkManager network)
    {
        //server side needs to grab the world entity
        return ModLoader.getMinecraftInstance().theWorld;
    }

    public static boolean isClient(World world)
    {
        return world instanceof WorldClient;
    }

    public static boolean isServer()
    {
        return false;
    }

    public static void sendPacketToPlayer(String playerName, Packet250CustomPayload packet)
    {
        // ModLoader.getMinecraftServerInstance().configManager.sendPacketToPlayer(playerName, packet);
    }

    public static void sendPacketToServer(Packet250CustomPayload packet)
    {
        ModLoader.sendPacket(packet);
    }
}
