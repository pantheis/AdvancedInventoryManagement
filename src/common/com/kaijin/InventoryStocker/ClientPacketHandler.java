/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientPacketHandler implements IPacketHandler
{
	/*
	 * Packet format:
	 *   byte 0: Packet Type
	 *   int 1: x location of TileEntity
	 *   int 2: y location of TileEntity
	 *   int 3: z location of TileEntity
	 * Currently available packet types
	 * Server:
	 *   0=
	 *      int 4: "metadata", sync client TE rotation and lights with server
	 */

	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));

		//Read the first int to determine packet type
		int packetType = -1;
		try
		{
			packetType = stream.readInt();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}

		int x = 0;
		int y = 0;
		int z = 0;
		World world;
		TileEntity tile;
		try
		{
			x = stream.readInt();
			y = stream.readInt();
			z = stream.readInt();
			world = FMLClientHandler.instance().getClient().theWorld;
			tile = world.getBlockTileEntity(x, y, z);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}

		//check if the tile we're looking at is an Inventory Stocker tile
		if (tile instanceof TileEntityInventoryStocker)
		{
			if (packetType == 0)
			{
				int Metainfo = 0;
				try
				{
					Metainfo = stream.readInt();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return;
				}

				((TileEntityInventoryStocker)tile).metaInfo = Metainfo;
				world.markBlockForUpdate(x, y, z);
				//if (Info.isDebugging) System.out.println("Packet 0 processed by client");
			}
		}
	}
}
