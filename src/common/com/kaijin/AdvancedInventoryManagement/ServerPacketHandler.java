/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvancedInventoryManagement;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{
	/*
	 * Packet format:
	 *   byte 0: Packet Type
	 *   int 1: x location of TileEntity
	 *   int 2: y location of TileEntity
	 *   int 3: z location of TileEntity
	 * Currently available packet types
	 * Client:
	 *   0= button click
	 *      int 4: button ID (0 = snapshot, 1 = mode)
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

		World world;
		TileEntity tile;
		int x;
		int y;
		int z;

		try
		{
			x = stream.readInt();
			y = stream.readInt();
			z = stream.readInt();
			world = ((EntityPlayerMP)player).worldObj;
			tile = world.getBlockTileEntity(x, y, z);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}

		if (tile instanceof TileEntityStocker)
		{
			if (packetType == 0)
			{
				int button = 0;
				try
				{
					button = stream.readInt();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
					return;
				}

				//System.out.println("Packet 0 processed by server. Button ID: " + button);
				switch (button)
				{
				case 0:
					((TileEntityStocker)tile).receiveSnapshotRequest();
					break;
				case 1:
					((TileEntityStocker)tile).receiveModeRequest();
					break;
				default:
					System.out.println("Unknown button field in received packet: " + button);
				}
			}
		}
	}
}
