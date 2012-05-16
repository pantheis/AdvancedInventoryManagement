package kaijin.InventoryStocker;

import forge.IConnectionHandler;
import forge.MessageManager;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet1Login;

public class ConnectionHandler implements IConnectionHandler
{
    public void onConnect(NetworkManager var1)
    {
        MessageManager.getInstance().registerChannel(var1, new PacketHandler(), "InvStocker");
    }

    public void onLogin(NetworkManager var1, Packet1Login var2) {}

    public void onDisconnect(NetworkManager var1, String var2, Object[] var3) {}
}
