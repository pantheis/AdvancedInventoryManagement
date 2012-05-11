package kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import kaijin.InventoryStocker.*;

public class PacketHandler implements IPacketHandler
{
    int packetType = 0;
    int x = 0;
    int y = 0;
    int z = 0;
    boolean snapShot = false;
    
    //This is the listen function to obtain data FROM the server TO the client
    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] data)
    {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
        
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
        // assign packet out to x,y,z,isValid to make it easier
        if (this.packetType == 0)
        {
            TileEntity tile = ModLoader.getMinecraftInstance().theWorld.getBlockTileEntity(x, y, z);
            //check if the tile we're looking at is an Inventory Stocker tile
            if (tile instanceof TileEntityInventoryStocker)
            {
                //call a function on that tile to let it know if it has a valid state server side or not
                if (snapShot)
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
