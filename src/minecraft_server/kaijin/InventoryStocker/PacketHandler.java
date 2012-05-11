package kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import net.minecraft.server.*;
import kaijin.InventoryStocker.*;

public class PacketHandler implements IPacketHandler
{
    int packetType = 0;    
    int x = 0;
    int y = 0;
    int z = 0;
    boolean snapShot = false;
    
    // This is the listen function to obtain data FROM the client TO the server
    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] data)
    {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
        
        //grab the first four bytes from the incoming packet
        try
        {
            this.packetType = stream.readInt();
            this.x = stream.readInt();
            this.y = stream.readInt();
            this.z = stream.readInt();
            this.snapShot = stream.readBoolean();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        if (packetType == 0)
        {
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
                    ((TileEntityInventoryStocker)tile).recvSnapshotRequestClient(true);
                }
                else
                {
                    //clear a snapshot request from client
                    ((TileEntityInventoryStocker)tile).recvSnapshotRequestClient(false);
                }
            }
        }
    }
}