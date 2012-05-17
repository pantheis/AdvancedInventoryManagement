package com.kaijin.InventoryStocker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.kaijin.InventoryStocker.*;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class PacketHandler implements IPacketHandler
{
    int packetType = 0;
    int x = 0;
    int y = 0;
    int z = 0;
    boolean snapshot = false;

    /*
     * Packet format:
     * byte 0: Packet Type
     *     Currently available packet types
     *         Client:
     *         0=
     *             byte 1: x location of TileEntity
     *             byte 2: y location of TileEntity
     *             byte 3: z location of TileEntity
     *             byte 4: boolean request, false = clear snapshot, true = take snapshot
     *         
     *         Server:
     *         0=
     *             byte 1: x location of TileEntity
     *             byte 2: y location of TileEntity
     *             byte 3: z location of TileEntity
     *             byte 4: boolean information, false = no valid snapshot, true = valid snapshot
     *             
     * remaining bytes: data for packet
     */

    //This is the listen function to obtain data FROM the server TO the client
    @Override
    public void onPacketData(NetworkManager network, String channel, byte[] data)
    {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));
        //Read the first int to determine packet type
        try
        {
            this.packetType = stream.readInt();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        /*
         * each packet type needs to implement an if and then whatever other read functions it needs
         * complete with try/catch blocks
         */
        if (this.packetType == 0)
        {
            try
            {
                this.x = stream.readInt();
                this.y = stream.readInt();
                this.z = stream.readInt();
                this.snapshot = stream.readBoolean();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            World world = CommonProxy.PacketHandlerGetWorld(network);
            TileEntity tile = world.getBlockTileEntity(x, y, z);

            //check if the tile we're looking at is an Inventory Stocker tile
            if (tile instanceof TileEntityInventoryStocker)
            {
                //call a function on that tile to let it know if it has a valid state server side or not
                if(CommonProxy.isClient(tile.worldObj))
                {
                    //snapshot state message from server
                    ((TileEntityInventoryStocker)tile).setSnapshotState(snapshot);
                }
                else
                {
                    //take or clear a snapshot request from client
                    ((TileEntityInventoryStocker)tile).recvSnapshotRequest(snapshot);
                }
            }
        }
    }
}
