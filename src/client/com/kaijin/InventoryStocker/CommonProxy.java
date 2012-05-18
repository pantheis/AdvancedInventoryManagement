package com.kaijin.InventoryStocker;

import java.io.File;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import net.minecraft.client.Minecraft;

public class CommonProxy
{
    public static void load()
    {
        MinecraftForgeClient.preloadTexture("/com/kaijin/InventoryStocker/textures/terrain.png");
        ModLoader.addName(mod_InventoryStocker.InventoryStocker, "Inventory Stocker");
    }

    public static Configuration getConfiguration()
    {
        return new Configuration(new File(Minecraft.getMinecraftDir(), "config/InventoryStocker.cfg"));
    }

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
