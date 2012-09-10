/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import java.io.File;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.src.*;
import net.minecraftforge.common.Configuration;

public class CommonProxy implements IGuiHandler
{
    public static void load()
    {

    }

    public static Configuration getConfiguration()
    {
        return new Configuration(new File("config/InventoryStocker.cfg"));
    }

//    public static World PacketHandlerGetWorld(NetworkManager network)
//    {
//        //server side needs to grab the world entity
//        return ((NetServerHandler)network.getNetHandler()).getPlayerEntity().worldObj;
//    }

    public static boolean isClient(World world)
    {
        return false;
    }

    public static boolean isServer()
    {
        return true;
    }

//    public static void sendPacketToPlayer(String playerName, Packet250CustomPayload packet)
//    {
//        ModLoader.getMinecraftServerInstance().configManager.sendPacketToPlayer(playerName, packet);
//    }

    public static void sendPacketToServer(Packet250CustomPayload packet)
    {
        // ModLoader.sendPacket(packet);
    }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (!world.blockExists(x, y, z))
        {
            return null;
        }

        TileEntity tile = world.getBlockTileEntity(x, y, z);

        if (!(tile instanceof TileEntityInventoryStocker))
        {
            return null;
        }

        return new ContainerInventoryStocker(player.inventory, (TileEntityInventoryStocker)tile, player);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		if (!world.blockExists(x, y, z))
        {
            return null;
        }

        TileEntity tile = world.getBlockTileEntity(x, y, z);

        if (!(tile instanceof TileEntityInventoryStocker))
        {
            return null;
        }

        return new GuiInventoryStocker(player.inventory, (TileEntityInventoryStocker)tile, player);
	}
}
