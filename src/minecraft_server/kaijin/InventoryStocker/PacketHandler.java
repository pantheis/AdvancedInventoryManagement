package kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import cpw.mods.fml.server.FMLServerHandler;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import net.minecraft.server.*;
import kaijin.InventoryStocker.*;

public class PacketHandler implements IPacketHandler
{
    // This is the listen function to obtain data FROM the client TO the server
    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] data)
    {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
        int packet[] = new int[4];
        
        //grab the first four bytes from the incoming packet
        try
        {
            for(int i = 0; i < 4; i++)
            {
                packet[i] = stream.readInt();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if (packet[0] == 0)
        {
            int x = packet[1]; int y = packet[2]; int z = packet[3]; boolean snapShot = (packet[4] == 0 ? false : true);
            //server side needs to grab the world entity
            World world = ((NetServerHandler)network.getNetHandler()).getPlayerEntity().worldObj;
            TileEntity tile = world.getBlockTileEntity(x, y, z);
            //check if the tile we're looking at is an Inventory Stocker tile
            if (tile instanceof TileEntityInventoryStocker)
            {
                //call a function on that tile to ask it to clear or take a snapshot
                if (snapShot)
                {
                    //take a snapshot request from client
                    ((TileEntityInventoryStocker)tile).setSnapshotStateServer(true);
                }
                else
                {
                    //clear a snapshot request from client
                    ((TileEntityInventoryStocker)tile).setSnapshotStateServer(false);
                }
            }
        }
    }
}