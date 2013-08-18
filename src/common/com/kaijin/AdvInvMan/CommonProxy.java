/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvInvMan;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler
{
	public void load() {}

	public boolean isClient()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
	}

	public boolean isServer()
	{
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

	public static void sendPacketToPlayer(Packet250CustomPayload packet, EntityPlayerMP player)
	{
		PacketDispatcher.sendPacketToPlayer(packet, (Player)player);
	}

	public static void sendPacketToServer(Packet250CustomPayload packet)
	{
		PacketDispatcher.sendPacketToServer(packet);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) 
	{
		if (!world.blockExists(x, y, z))
		{
			return null;
		}

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (!(tile instanceof TileEntityStocker))
		{
			return null;
		}

		return new ContainerStocker(player.inventory, (TileEntityStocker)tile);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) 
	{
		if (!world.blockExists(x, y, z))
		{
			return null;
		}

		TileEntity tile = world.getBlockTileEntity(x, y, z);

		if (!(tile instanceof TileEntityStocker))
		{
			return null;
		}
		return new GuiStocker(player.inventory, (TileEntityStocker)tile);
	}
}
