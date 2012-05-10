package kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import kaijin.InventoryStocker.*;

public class PacketHandler implements IPacketHandler
{
    //This is the listen function to obtain data FROM the server TO the client
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
        // assign packet out to x,y,z,isValid to make it easier
        if (packet[0] == 0)
        {
            int x = packet[1]; int y = packet[2]; int z = packet[3]; boolean isValid = (packet[4] == 0 ? false : true);
            TileEntity tile = ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z);
            //check if the tile we're looking at is an Inventory Stocker tile
            if (tile instanceof TileEntityInventoryStocker)
            {
                //call a function on that tile to let it know if it has a valid state server side or not
                if (isValid)
                {
                    ((TileEntityInventoryStocker)tile).setSnapshotState(true);
                }
                else
                {
                    ((TileEntityInventoryStocker)tile).setSnapshotState(false);
                }
            }
        }
    }
}
