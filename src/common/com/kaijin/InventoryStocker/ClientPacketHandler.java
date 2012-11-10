/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

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
				world.markBlockNeedsUpdate(x, y, z);
				//if (Info.isDebugging) System.out.println("Packet 0 processed by client");
			}
		}
	}
}
