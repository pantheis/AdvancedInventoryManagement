package kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class PacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] data)
    {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

        try
        {
            // NetClientHandler net = (NetClientHandler)network.getNetHandler();
            int packetID = stream.read();

            if (packetID == 0) // Figure out what ID or IDs to use, not to mention how to set up a packet properly
            {
                // TODO Auto-generated method stub
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
