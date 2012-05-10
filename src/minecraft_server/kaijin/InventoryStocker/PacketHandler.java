package kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import kaijin.InventoryStocker.*;

public class PacketHandler implements IPacketHandler
{
    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] data)
    {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

        try
        {
            // NetClientHandler net = (NetClientHandler)network.getNetHandler();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        ((NetServerHandler)network.getNetHandler()).getPlayerEntity();
    }
}
