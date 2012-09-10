/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import cpw.mods.fml.common.network.IConnectionHandler;
import cpw.mods.fml.common.network.Player;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

public class ConnectionHandler implements IConnectionHandler
{
	@Override
	public void playerLoggedIn(Player player, NetHandler netHandler,
			NetworkManager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String connectionReceived(NetLoginHandler netHandler,
			NetworkManager manager) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler, String server,
			int port, NetworkManager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionOpened(NetHandler netClientHandler,
			MinecraftServer server, NetworkManager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionClosed(NetworkManager manager) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clientLoggedIn(NetHandler clientHandler,
			NetworkManager manager, Packet1Login login) {
		// TODO Auto-generated method stub
		
	}
}
