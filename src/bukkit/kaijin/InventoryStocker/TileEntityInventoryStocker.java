package kaijin.InventoryStocker;

import forge.ISidedInventory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.Block;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.Packet250CustomPayload;
import net.minecraft.server.TileEntity;

public class TileEntityInventoryStocker extends TileEntity implements IInventory, ISidedInventory
{
    private ItemStack[] contents = new ItemStack[this.getSize()];
    private ItemStack[] remoteSnapshot;
    private boolean previousPoweredState = false;
    private boolean hasSnapshot = false;
    private boolean tileLoaded = false;
    private boolean guiTakeSnapshot = false;
    private boolean guiClearSnapshot = false;
    private TileEntity lastTileEntity = null;
    public TileEntity tileFrontFace = null;
    private String targetTileName = "none";
    private int remoteNumSlots = 0;
    private List remoteUsers = new ArrayList();
    private boolean[] doorState;

    public boolean canUpdate()
    {
        return true;
    }

    public TileEntityInventoryStocker()
    {
        this.clearSnapshot();
        this.doorState = new boolean[6];
    }

    public void setSnapshotState(boolean var1)
    {
        if (CommonProxy.isClient(this.world))
        {
            this.hasSnapshot = var1;
        }
    }

    public void entityOpenList(List var1)
    {
        this.remoteUsers = var1;
    }

    public void recvSnapshotRequest(boolean var1)
    {
        if (var1)
        {
            System.out.println("GUI: take snapshot request");
            this.guiTakeSnapshot = true;
        }
        else
        {
            System.out.println("GUI: clear snapshot request");
            this.guiClearSnapshot = true;
        }
    }

    public boolean validSnapshot()
    {
        return this.hasSnapshot;
    }

    private Packet250CustomPayload createSnapshotPacket()
    {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        DataOutputStream var2 = new DataOutputStream(var1);

        try
        {
            var2.writeInt(0);
            var2.writeInt(this.x);
            var2.writeInt(this.y);
            var2.writeInt(this.z);
            var2.writeBoolean(this.hasSnapshot);
        }
        catch (IOException var4)
        {
            var4.printStackTrace();
        }

        Packet250CustomPayload var3 = new Packet250CustomPayload();
        var3.tag = "InvStocker";
        var3.data = var1.toByteArray();
        var3.length = var3.data.length;
        return var3;
    }

    public void sendSnapshotStateClient(String var1)
    {
        Packet250CustomPayload var2 = this.createSnapshotPacket();
        CommonProxy.sendPacketToPlayer(var1, var2);
    }

    private void sendSnapshotStateClients()
    {
        Packet250CustomPayload var1 = this.createSnapshotPacket();

        if (this.remoteUsers != null)
        {
            for (int var2 = 0; var2 < this.remoteUsers.size(); ++var2)
            {
                CommonProxy.sendPacketToPlayer((String)this.remoteUsers.get(var2), var1);
            }
        }
    }

    private void sendSnapshotRequestServer(boolean var1)
    {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        DataOutputStream var3 = new DataOutputStream(var2);

        try
        {
            var3.writeInt(0);
            var3.writeInt(this.x);
            var3.writeInt(this.y);
            var3.writeInt(this.z);
            var3.writeBoolean(var1);
        }
        catch (IOException var5)
        {
            var5.printStackTrace();
        }

        Packet250CustomPayload var4 = new Packet250CustomPayload();
        var4.tag = "InvStocker";
        var4.data = var2.toByteArray();
        var4.length = var4.data.length;
        CommonProxy.sendPacketToServer(var4);
    }

    public void guiTakeSnapshot()
    {
        if (CommonProxy.isClient(this.world))
        {
            this.sendSnapshotRequestServer(true);
        }
        else
        {
            this.guiTakeSnapshot = true;
        }
    }

    public void guiClearSnapshot()
    {
        if (CommonProxy.isClient(this.world))
        {
            this.sendSnapshotRequestServer(false);
        }
        else
        {
            this.guiClearSnapshot = true;
        }
    }

    public void clearSnapshot()
    {
        this.lastTileEntity = null;
        this.hasSnapshot = false;
        this.targetTileName = "none";
        this.remoteSnapshot = null;
        this.remoteNumSlots = 0;

        if (CommonProxy.isServer())
        {
            this.sendSnapshotStateClients();
        }
    }

    public void onUpdate()
    {
        if (!CommonProxy.isClient(this.world) && this.checkInvalidSnapshot())
        {
            this.clearSnapshot();
        }

        if (!CommonProxy.isServer())
        {
            this.updateDoorStates();
        }
    }

    private void updateDoorStates()
    {
        this.doorState[0] = this.findTubeOrPipeAt(this.x, this.y - 1, this.z);
        this.doorState[1] = this.findTubeOrPipeAt(this.x, this.y + 1, this.z);
        this.doorState[2] = this.findTubeOrPipeAt(this.x, this.y, this.z - 1);
        this.doorState[3] = this.findTubeOrPipeAt(this.x, this.y, this.z + 1);
        this.doorState[4] = this.findTubeOrPipeAt(this.x - 1, this.y, this.z);
        this.doorState[5] = this.findTubeOrPipeAt(this.x + 1, this.y, this.z);
    }

    private boolean findTubeOrPipeAt(int var1, int var2, int var3)
    {
        int var4 = this.world.getTypeId(var1, var2, var3);

        if (var4 > 0)
        {
            String var5 = Block.byId[var4].getClass().toString();

            if (var5.endsWith("GenericPipe"))
            {
                return true;
            }

            if (var5.endsWith("eloraam.base.BlockMicro"))
            {
                int var6 = this.world.getData(var1, var2, var3);
                return var6 >= 8 && var6 <= 10;
            }
        }

        return false;
    }

    public boolean doorOpenOnSide(int var1)
    {
        return this.doorState[var1];
    }

    public int getStartInventorySide(int var1)
    {
        int var2 = this.getRotatedSideFromMetadata(var1);
        return var2 == 1 ? 9 : 0;
    }

    public int getSizeInventorySide(int var1)
    {
        int var2 = this.getRotatedSideFromMetadata(var1);
        return var2 == 0 ? 0 : 9;
    }

    public int getRotatedSideFromMetadata(int var1)
    {
        int var2 = this.world.getData(this.x, this.y, this.z) & 7;
        return Utils.lookupRotatedSide(var1, var2);
    }

    public int getBlockIDAtFace(int var1)
    {
        int var2 = this.x;
        int var3 = this.y;
        int var4 = this.z;

        switch (var1)
        {
            case 0:
                --var3;
                break;

            case 1:
                ++var3;
                break;

            case 2:
                --var4;
                break;

            case 3:
                ++var4;
                break;

            case 4:
                --var2;
                break;

            case 5:
                ++var2;
                break;

            default:
                return 0;
        }

        return this.world.getTypeId(var2, var3, var4);
    }

    public TileEntity getTileAtFrontFace()
    {
        int var1 = this.world.getData(this.x, this.y, this.z) & 7;
        int var2 = this.x;
        int var3 = this.y;
        int var4 = this.z;

        switch (var1)
        {
            case 0:
                --var3;
                break;

            case 1:
                ++var3;
                break;

            case 2:
                --var4;
                break;

            case 3:
                ++var4;
                break;

            case 4:
                --var2;
                break;

            case 5:
                ++var2;
                break;

            default:
                return null;
        }

        return this.world.getTileEntity(var2, var3, var4);
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getSize()
    {
        return 18;
    }

    /**
     * Returns the stack in slot i
     */
    public ItemStack getItem(int var1)
    {
        return this.contents[var1];
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    public ItemStack splitStack(int var1, int var2)
    {
        if (this.contents[var1] != null)
        {
            ItemStack var3;

            if (this.contents[var1].count <= var2)
            {
                var3 = this.contents[var1];
                this.contents[var1] = null;
                this.update();
                return var3;
            }
            else
            {
                var3 = this.contents[var1].a(var2);

                if (this.contents[var1].count == 0)
                {
                    this.contents[var1] = null;
                }

                this.update();
                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     */
    public ItemStack splitWithoutUpdate(int var1)
    {
        if (this.contents[var1] == null)
        {
            return null;
        }
        else
        {
            ItemStack var2 = this.contents[var1];
            this.contents[var1] = null;
            return var2;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public void setItem(int var1, ItemStack var2)
    {
        this.contents[var1] = var2;

        if (var2 != null && var2.count > this.getMaxStackSize())
        {
            var2.count = this.getMaxStackSize();
        }
    }

    /**
     * Returns the name of the inventory.
     */
    public String getName()
    {
        return "Stocker";
    }

    /**
     * Reads a tile entity from NBT.
     */
    public void a(NBTTagCompound var1)
    {
        if (!CommonProxy.isClient(this.world))
        {
            super.a(var1);
            this.targetTileName = var1.getString("targetTileName");
            this.remoteNumSlots = var1.getInt("remoteSnapshotSize");
            System.out.println("ReadNBT: " + this.targetTileName + " remoteInvSize:" + this.remoteNumSlots);
            NBTTagList var2 = var1.getList("Items");
            NBTTagList var3 = var1.getList("remoteSnapshot");
            this.contents = new ItemStack[this.getSize()];
            this.remoteSnapshot = null;

            if (this.remoteNumSlots != 0)
            {
                this.remoteSnapshot = new ItemStack[this.remoteNumSlots];
            }

            int var4;
            NBTTagCompound var5;
            int var6;

            for (var4 = 0; var4 < var2.size(); ++var4)
            {
                var5 = (NBTTagCompound)var2.get(var4);
                var6 = var5.getByte("Slot") & 255;

                if (var6 >= 0 && var6 < this.contents.length)
                {
                    this.contents[var6] = ItemStack.a(var5);
                }
            }

            System.out.println("ReadNBT tagRemoteCount: " + var3.size());

            if (var3.size() != 0)
            {
                for (var4 = 0; var4 < var3.size(); ++var4)
                {
                    var5 = (NBTTagCompound)var3.get(var4);
                    var6 = var5.getByte("Slot") & 255;

                    if (var6 >= 0 && var6 < this.remoteSnapshot.length)
                    {
                        this.remoteSnapshot[var6] = ItemStack.a(var5);
                        System.out.println("ReadNBT Remote Slot: " + var6 + " ItemID: " + this.remoteSnapshot[var6].id);
                    }
                }
            }
        }
    }

    /**
     * Writes a tile entity to NBT.
     */
    public void b(NBTTagCompound var1)
    {
        if (!CommonProxy.isClient(this.world))
        {
            super.b(var1);
            NBTTagList var2 = new NBTTagList();
            NBTTagList var3 = new NBTTagList();
            int var4;
            NBTTagCompound var5;

            for (var4 = 0; var4 < this.contents.length; ++var4)
            {
                if (this.contents[var4] != null)
                {
                    var5 = new NBTTagCompound();
                    var5.setByte("Slot", (byte)var4);
                    this.contents[var4].save(var5);
                    var2.add(var5);
                }
            }

            if (this.remoteSnapshot != null)
            {
                System.out.println("writeNBT Target: " + this.targetTileName + " remoteInvSize:" + this.remoteSnapshot.length);

                for (var4 = 0; var4 < this.remoteSnapshot.length; ++var4)
                {
                    if (this.remoteSnapshot[var4] != null)
                    {
                        System.out.println("writeNBT Remote Slot: " + var4 + " ItemID: " + this.remoteSnapshot[var4].id + " StackSize: " + this.remoteSnapshot[var4].count + " meta: " + this.remoteSnapshot[var4].getData());
                        var5 = new NBTTagCompound();
                        var5.setByte("Slot", (byte)var4);
                        this.remoteSnapshot[var4].save(var5);
                        var3.add(var5);
                    }
                }
            }
            else
            {
                System.out.println("writeNBT Remote Items is NULL!");
            }

            var1.set("Items", var2);
            var1.set("remoteSnapshot", var3);
            var1.setString("targetTileName", this.targetTileName);
            var1.setInt("remoteSnapshotSize", this.remoteNumSlots);
        }
    }

    public void onLoad()
    {
        if (!CommonProxy.isClient(this.world))
        {
            this.tileLoaded = true;
            System.out.println("onLoad, remote inv size = " + this.remoteNumSlots);
            TileEntity var1 = this.getTileAtFrontFace();

            if (var1 == null)
            {
                System.out.println("onLoad tile = null");
                this.clearSnapshot();
            }
            else
            {
                String var2 = var1.getClass().getName();

                if (var2.equals(this.targetTileName) && ((IInventory)var1).getSize() == this.remoteNumSlots)
                {
                    System.out.println("onLoad, target name=" + var2 + " stored name=" + this.targetTileName + " MATCHED!");
                    this.lastTileEntity = var1;
                    this.hasSnapshot = true;
                }
                else
                {
                    System.out.println("onLoad, target name=" + var2 + " stored name=" + this.targetTileName + " NOT matched.");
                    this.clearSnapshot();
                }
            }
        }
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended. *Isn't
     * this more of a set than a get?*
     */
    public int getMaxStackSize()
    {
        return 64;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    public boolean a(EntityHuman var1)
    {
        return this.world.getTileEntity(this.x, this.y, this.z) != this ? false : var1.e((double)this.x + 0.5D, (double)this.y + 0.5D, (double)this.z + 0.5D) <= 64.0D;
    }

    public void f() {}

    public void g() {}

    public boolean takeSnapShot(TileEntity var1)
    {
        if (!(var1 instanceof IInventory))
        {
            return false;
        }
        else
        {
            this.remoteNumSlots = ((IInventory)var1).getSize();
            ItemStack[] var3 = new ItemStack[this.remoteNumSlots];

            for (int var4 = 0; var4 < this.remoteNumSlots; ++var4)
            {
                ItemStack var2 = ((IInventory)var1).getItem(var4);

                if (var2 == null)
                {
                    var3[var4] = null;
                }
                else
                {
                    var3[var4] = new ItemStack(var2.id, var2.count, var2.getData());

                    if (var2.tag != null)
                    {
                        var3[var4].tag = (NBTTagCompound)var2.tag.clone();
                    }
                }
            }

            this.targetTileName = var1.getClass().getName();
            this.remoteSnapshot = var3;
            this.lastTileEntity = var1;
            this.hasSnapshot = true;
            return true;
        }
    }

    public boolean inputGridIsEmpty()
    {
        for (int var1 = 0; var1 < 9; ++var1)
        {
            if (this.contents[var1] != null)
            {
                return false;
            }
        }

        return true;
    }

    protected void stockInventory(IInventory var1)
    {
        byte var2 = 0;
        int var3 = var2 + var1.getSize();
        int var5 = 0;
        boolean var4;

        do
        {
            var4 = false;

            for (int var6 = var2; var6 < var3; ++var6)
            {
                ItemStack var7 = var1.getItem(var6);
                ItemStack var8 = this.remoteSnapshot[var6];

                if (var7 == null)
                {
                    if (var8 != null)
                    {
                        var4 = this.addItemToRemote(var6, var1, this.remoteSnapshot[var6].count);
                    }
                }
                else if (var8 == null)
                {
                    var4 = this.removeItemFromRemote(var6, var1, var1.getItem(var6).count);
                }
                else if (this.checkItemTypesMatch(var7, var8))
                {
                    int var9 = this.remoteSnapshot[var6].count - var1.getItem(var6).count;

                    if (var9 > 0)
                    {
                        var4 = this.addItemToRemote(var6, var1, var9);
                    }
                    else if (var9 < 0)
                    {
                        var4 = this.removeItemFromRemote(var6, var1, -var9);
                    }
                }
                else
                {
                    var4 = this.removeItemFromRemote(var6, var1, var1.getItem(var6).count);

                    if (var1.getItem(var6) == null)
                    {
                        var4 = this.addItemToRemote(var6, var1, this.remoteSnapshot[var6].count);
                    }
                }
            }

            ++var5;
        }
        while (var4 && var5 < 100);
    }

    protected boolean checkItemTypesMatch(ItemStack var1, ItemStack var2)
    {
        if (var1.id == var2.id)
        {
            if (var1.d())
            {
                return true;
            }

            if (var1.getData() == var2.getData())
            {
                return true;
            }
        }

        return false;
    }

    protected boolean removeItemFromRemote(int var1, IInventory var2, int var3)
    {
        boolean var4 = false;
        ItemStack var5 = var2.getItem(var1);

        if (var5 == null)
        {
            return false;
        }
        else
        {
            int var6 = var5.getMaxStackSize();
            int var7 = var3;

            if (var3 > var6)
            {
                var7 = var6;
            }

            int var8 = -1;

            for (int var9 = 9; var9 < 18; ++var9)
            {
                if (this.contents[var9] == null)
                {
                    if (var8 == -1)
                    {
                        var8 = var9;
                    }
                }
                else if (this.checkItemTypesMatch(this.contents[var9], var5))
                {
                    int var10 = var6 - this.contents[var9].count;

                    if (var10 >= var7)
                    {
                        this.contents[var9].count += var7;
                        var5.count -= var7;

                        if (var5.count <= 0)
                        {
                            var2.setItem(var1, (ItemStack)null);
                        }

                        return true;
                    }

                    this.contents[var9].count += var10;
                    var5.count -= var10;
                    var7 -= var10;
                    var4 = true;
                }
            }

            if (var7 > 0 && var8 >= 0)
            {
                this.contents[var8] = var5;
                var2.setItem(var1, (ItemStack)null);
                return true;
            }
            else
            {
                return var4;
            }
        }
    }

    protected boolean addItemToRemote(int var1, IInventory var2, int var3)
    {
        boolean var4 = false;
        int var5 = this.remoteSnapshot[var1].getMaxStackSize();
        int var6 = var3;

        if (var3 > var5)
        {
            var6 = var5;
        }

        for (int var7 = 17; var7 >= 0; --var7)
        {
            if (this.contents[var7] != null && this.checkItemTypesMatch(this.contents[var7], this.remoteSnapshot[var1]))
            {
                if (var2.getItem(var1) == null)
                {
                    if (this.contents[var7].count > var6)
                    {
                        ItemStack var8 = this.contents[var7].a(var6);
                        var2.setItem(var1, var8);
                        return true;
                    }

                    var6 -= this.contents[var7].count;
                    var2.setItem(var1, this.contents[var7]);
                    this.contents[var7] = null;

                    if (var6 <= 0)
                    {
                        return true;
                    }

                    var4 = true;
                }
                else
                {
                    ItemStack var10000;

                    if (this.contents[var7].count > var6)
                    {
                        this.contents[var7].count -= var6;
                        var10000 = var2.getItem(var1);
                        var10000.count += var6;
                        return true;
                    }

                    var6 -= this.contents[var7].count;
                    var10000 = var2.getItem(var1);
                    var10000.count += this.contents[var7].count;
                    this.contents[var7] = null;

                    if (var6 <= 0)
                    {
                        return true;
                    }

                    var4 = true;
                }
            }
        }

        return var4;
    }

    public boolean checkInvalidSnapshot()
    {
        TileEntity var1 = this.getTileAtFrontFace();

        if (var1 == null)
        {
            System.out.println("Invalid: Tile = null");
            return true;
        }
        else
        {
            String var2 = var1.getClass().getName();

            if (!var2.equals(this.targetTileName))
            {
                System.out.println("Invalid: TileName Mismatched, detected TileName=" + var2 + " expected TileName=" + this.targetTileName);
                return true;
            }
            else if (var1 != this.lastTileEntity)
            {
                System.out.println("Invalid: tileEntity does not match lastTileEntity");
                return true;
            }
            else if (((IInventory)var1).getSize() != this.remoteNumSlots)
            {
                System.out.println("Invalid: tileEntity inventory size has changed");
                System.out.println("RemoteInvSize: " + ((IInventory)var1).getSize() + ", Expecting: " + this.remoteNumSlots);
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    private void lightsOff()
    {
        int var1 = this.world.getData(this.x, this.y, this.z);
        var1 &= 7;
        this.world.setData(this.x, this.y, this.z, var1);
        this.world.k(this.x, this.y, this.z);
    }

    private void lightsOn()
    {
        int var1 = this.world.getData(this.x, this.y, this.z);
        var1 |= 8;
        this.world.setData(this.x, this.y, this.z, var1);
        this.world.k(this.x, this.y, this.z);
    }

    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
     * ticks and creates a new spawn inside its implementation.
     */
    public void q_()
    {
        super.q_();
        boolean var3;

        if (CommonProxy.isClient(this.world))
        {
            this.updateDoorStates();
            var3 = this.world.isBlockIndirectlyPowered(this.x, this.y, this.z);

            if (!var3 && this.previousPoweredState)
            {
                this.previousPoweredState = false;
                this.lightsOff();
            }
            else if (var3 && !this.previousPoweredState)
            {
                this.previousPoweredState = true;
                this.lightsOn();
            }
        }
        else
        {
            if (!this.tileLoaded)
            {
                System.out.println("tileLoaded false, running onLoad");
                this.onLoad();
            }

            if (this.guiTakeSnapshot)
            {
                this.guiTakeSnapshot = false;
                System.out.println("GUI take snapshot request");
                TileEntity var1 = this.getTileAtFrontFace();

                if (var1 != null && var1 instanceof IInventory)
                {
                    if (this.takeSnapShot(var1))
                    {
                        if (CommonProxy.isServer())
                        {
                            this.sendSnapshotStateClients();
                        }
                    }
                    else
                    {
                        this.clearSnapshot();
                    }
                }
            }

            if (this.guiClearSnapshot)
            {
                this.guiClearSnapshot = false;
                this.clearSnapshot();
            }

            var3 = this.world.isBlockIndirectlyPowered(this.x, this.y, this.z);

            if (var3 && !CommonProxy.isServer())
            {
                this.world.k(this.x, this.y, this.z);
            }

            if (!var3 && this.previousPoweredState)
            {
                this.previousPoweredState = false;

                if (!CommonProxy.isServer())
                {
                    this.lightsOff();
                }
            }

            if (var3 && !this.previousPoweredState)
            {
                this.previousPoweredState = true;
                System.out.println("Powered");

                if (!CommonProxy.isServer())
                {
                    this.lightsOn();
                }

                TileEntity var2 = this.getTileAtFrontFace();

                if (var2 != null && var2 instanceof IInventory)
                {
                    if (this.hasSnapshot && !this.checkInvalidSnapshot())
                    {
                        this.stockInventory((IInventory)var2);
                    }
                    else
                    {
                        System.out.println("Redstone pulse: No valid snapshot, doing nothing");

                        if (this.hasSnapshot)
                        {
                            this.clearSnapshot();
                        }
                    }
                }
                else
                {
                    if (this.hasSnapshot)
                    {
                        this.clearSnapshot();
                    }

                    System.out.println("entityUpdate snapshot clear");
                }
            }
        }
    }

    @Override
    public ItemStack[] getContents()
    {
        return contents;
    }

    @Override
    public void setMaxStackSize(int arg0)
    {
        // TODO Auto-generated method stub
    }
}
