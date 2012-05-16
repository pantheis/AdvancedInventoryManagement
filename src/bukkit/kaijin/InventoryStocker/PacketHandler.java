package kaijin.InventoryStocker;

import forge.IPacketHandler;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;

public class PacketHandler implements IPacketHandler
{
    int packetType = 0;
    int x = 0;
    int y = 0;
    int z = 0;
    boolean snapshot = false;

    public void onPacketData(NetworkManager var1, String var2, byte[] var3)
    {
        DataInputStream var4 = new DataInputStream(new ByteArrayInputStream(var3));

        try
        {
            this.packetType = var4.readInt();
        }
        catch (Exception var8)
        {
            var8.printStackTrace();
        }

        if (this.packetType == 0)
        {
            try
            {
                this.x = var4.readInt();
                this.y = var4.readInt();
                this.z = var4.readInt();
                this.snapshot = var4.readBoolean();
            }
            catch (Exception var7)
            {
                var7.printStackTrace();
            }

            World var5 = CommonProxy.PacketHandlerGetWorld(var1);
            TileEntity var6 = var5.getTileEntity(this.x, this.y, this.z);

            if (var6 instanceof TileEntityInventoryStocker)
            {
                if (CommonProxy.isClient(var6.world))
                {
                    ((TileEntityInventoryStocker)var6).setSnapshotState(this.snapshot);
                }
                else
                {
                    ((TileEntityInventoryStocker)var6).recvSnapshotRequest(this.snapshot);
                }
            }
        }
    }
}
