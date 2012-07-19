/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class ConnectionHandler implements IConnectionHandler
{
    @Override
    public void onConnect(NetworkManager network)
    {
        MessageManager.getInstance().registerChannel(network, new PacketHandler(), "InvStocker");
    }

    @Override
    public void onLogin(NetworkManager network, Packet1Login login)
    {
        
    }

    @Override
    public void onDisconnect(NetworkManager network, String message,Object[] args)
    {
        
    }
}
