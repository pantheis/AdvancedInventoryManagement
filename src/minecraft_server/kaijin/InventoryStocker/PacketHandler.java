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
    }
}
