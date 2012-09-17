/* Inventory Stocker
 *  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 *  Licensed as open source with restrictions. Please see attached LICENSE.txt.
 */

package com.kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import com.kaijin.InventoryStocker.*;
import net.minecraft.src.*;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.*;

public class ServerPacketHandler implements IPacketHandler
{
	int packetType = 0;
	int x = 0;
	int y = 0;
	int z = 0;
	boolean snapshot = false;

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
	 *         
	 *         Server:
	 *         0=
	 *             byte 1: x location of TileEntity
	 *             byte 2: y location of TileEntity
	 *             byte 3: z location of TileEntity
	 *             byte 4: boolean information, false = no valid snapshot, true = valid snapshot
	 *             
	 * remaining bytes: data for packet
	 */

	@Override
	public void onPacketData(NetworkManager network, Packet250CustomPayload packet, Player player)
	{
		if (Utils.isDebug()) System.out.println("ServerPacketHandler.onPacketData");
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
			if (Utils.isDebug()) System.out.println("ServerPacketHandler: Attempting to get worldObj");
			World world = ((EntityPlayerMP)player).worldObj;

			TileEntity tile = world.getBlockTileEntity(x, y, z);

			//check if the tile we're looking at is an Inventory Stocker tile
			if (tile instanceof TileEntityInventoryStocker)
			{
				String s = new Boolean(snapshot).toString();
				if (Utils.isDebug()) System.out.println("ServerPacketHandler: tile.recvSnapshotReqiest: " + s + ", guid: " + ((TileEntityInventoryStocker)tile).myGUID);
				((TileEntityInventoryStocker)tile).recvSnapshotRequest(snapshot);
			}
		}
	}
}
