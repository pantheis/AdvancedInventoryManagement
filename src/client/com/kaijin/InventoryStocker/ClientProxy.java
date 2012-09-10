/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import java.io.File;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.Configuration;

public class ClientProxy extends CommonProxy
{
    public static void load()
    {
        MinecraftForgeClient.preloadTexture("/com/kaijin/InventoryStocker/textures/terrain.png");
        LanguageRegistry.addName(mod_InventoryStocker.instance, "Inventory Stocker");
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
