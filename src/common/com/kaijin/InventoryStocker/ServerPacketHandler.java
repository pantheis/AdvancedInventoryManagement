/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class ServerPacketHandler implements IPacketHandler
{
	int packetType = -1;
	int x = 0;
	int y = 0;
	int z = 0;
	int Metainfo = 0;

	boolean snapshot = false;
	boolean rotateRequest = false;

	/*
	 * Packet format:
	 * byte 0: Packet Type
	 *     Currently available packet types
	 *         Client:
	 *         0=
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: boolean request, false = clear snapshot, true = take snapshot
	 *         1=
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: boolean request, false = not used, true = rotate request
	 *         
	 *         Server:
	 *         0=
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: boolean information, false = no valid snapshot, true = valid snapshot
	 *         1=@Deprecated
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: int "metadata", sync client TE rotation and lights with server
	 *             
	 * remaining bytes: data for packet
	 */

	@Override
	public void onPacketData(INetworkManager network, Packet250CustomPayload packet, Player player)
	{
		if (Info.isDebugging) System.out.println("ServerPacketHandler.onPacketData");
		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		//Read the first int to determine packet type
		try
		{
			this.packetType = stream.readInt();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		/*
		 * each packet type needs to implement an if and then whatever other read functions it needs
		 * complete with try/catch blocks
		 */
		if (this.packetType == 0)
		{
			try
			{
				this.x = stream.readInt();
				this.y = stream.readInt();
				this.z = stream.readInt();
				this.snapshot = stream.readBoolean();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			if (Info.isDebugging) System.out.println("ServerPacketHandler: Attempting to get worldObj");
			World world = ((EntityPlayerMP)player).worldObj;

			TileEntity tile = world.getBlockTileEntity(x, y, z);

			//check if the tile we're looking at is an Inventory Stocker tile
			if (tile instanceof TileEntityInventoryStocker)
			{
				//				String s = new Boolean(snapshot).toString();
				//				if (InventoryStocker.isDebugging) System.out.println("ServerPacketHandler: tile.recvSnapshotReqiest: " + s + ", guid: " + ((TileEntityInventoryStocker)tile).myGUID);
				((TileEntityInventoryStocker)tile).recvSnapshotRequest(snapshot);
			}
		}
		if (this.packetType == 1)
		{
			try
			{
				this.x = stream.readInt();
				this.y = stream.readInt();
				this.z = stream.readInt();
				this.rotateRequest = stream.readBoolean();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
			if (rotateRequest)
			{
				World world = ((EntityPlayerMP)player).worldObj;
				TileEntity tile = world.getBlockTileEntity(x, y, z);
				if (tile instanceof TileEntityInventoryStocker)
				{
					this.Metainfo = ((TileEntityInventoryStocker)tile).metaInfo;
					int dir = Metainfo & 7; // Get orientation from first 3 bits of meta data
					this.Metainfo ^= dir; // Clear those bits
					++dir; // Rotate

					if (dir > 5)
					{
						dir = 0;    // Start over
					}

					this.Metainfo |= dir; // Write orientation back to meta data value

					((TileEntityInventoryStocker)tile).metaInfo = this.Metainfo;
					world.markBlockNeedsUpdate(x, y, z);
//					((TileEntityInventoryStocker)tile).sendExtraTEData();
				}
			}
		}
	}
}
