package kaijin.InventoryStocker;

import net.minecraft.client.*;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import kaijin.InventoryStocker.*;

public class TileEntityInventoryStocker extends TileEntity implements IInventory, ISidedInventory
{
    //ItemStack privates
    private ItemStack contents[];
    private ItemStack remoteItems[];

    //Boolean privates
    private boolean previousPoweredState = false;
    private boolean snapShotState = false;
    private boolean tileLoaded = false;

    //other privates
    private TileEntity lastTileEntity = null;
    private String targetTileName = "none";
    private int remoteNumSlots = 0;

    @Override
    public boolean canUpdate()
    {
        return true;
    }

    public TileEntityInventoryStocker()
    {
        contents = new ItemStack [this.getSizeInventory()];
    }

    public int getStartInventorySide(int i)
    {
        // Sides (0-5) are: Front, Back, Top, Bottom, Right, Left
        int side = getRotatedSideFromMetadata(i);

        if (side == 1)
        {
            return 9;    // access output section, 9-17
        }

        return 0; // access input section, 0-8
    }

    public int getSizeInventorySide(int i)
    {
        // Sides (0-5) are: Top, Bottom, Front, Back, Left, Right
        int side = getRotatedSideFromMetadata(i);

        if (side == 0)
        {
            return 0;    // Front has no inventory access
        }

        return 9;
    }

    public int getRotatedSideFromMetadata(int side)
    {
        int dir = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 7;
        return Utils.lookupRotatedSide(side, dir);
    }

    public TileEntity getTileAtFrontFace()
    {
        int dir = worldObj.getBlockMetadata(xCoord, yCoord, zCoord) & 7;
        /**
         *      0: -Y (bottom side)
         *      1: +Y (top side)
         *      2: -Z (west side)
         *      3: +Z (east side)
         *      4: -X (north side)
         *      5: +x (south side)
         */
        int x = xCoord;
        int y = yCoord;
        int z = zCoord;

        switch (dir)
        {
            case 0:
                y--;
                break;

            case 1:
                y++;
                break;

            case 2:
                z--;
                break;

            case 3:
                z++;
                break;

            case 4:
                x--;
                break;

            case 5:
                x++;
                break;
        }

        return worldObj.getBlockTileEntity(x, y, z);
    }

    public int getSizeInventory()
    {
        return 18;
    }

    public ItemStack getStackInSlot(int i)
    {
        return contents[i];
    }

    public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.contents[par1] != null)
        {
            ItemStack var3;

            if (this.contents[par1].stackSize <= par2)
            {
                var3 = this.contents[par1];
                this.contents[par1] = null;
                this.onInventoryChanged();
                return var3;
            }
            else
            {
                var3 = this.contents[par1].splitStack(par2);

                if (this.contents[par1].stackSize == 0)
                {
                    this.contents[par1] = null;
                }

                this.onInventoryChanged();
                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack getStackInSlotOnClosing(int var1)
    {
        if (this.contents[var1] == null)
        {
            return null;
        }

        ItemStack stack = this.contents[var1];
        this.contents[var1] = null;
        return stack;
    }

    public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        contents[i] = itemstack;

        if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }

    public String getInvName()
    {
        return "Stocker";
    }

    /**
     * Reads a tile entity from NBT.
     */
    public void readFromNBT(NBTTagCompound nbttagcompound)
    {
        if(!Utils.isClient(worldObj))
        {
            super.readFromNBT(nbttagcompound);
            //read extra NBT stuff here
            targetTileName = nbttagcompound.getString("targetTileName");
            remoteNumSlots = nbttagcompound.getInteger("remoteItemsSize");
            
            System.out.println("readNBI: "+targetTileName+" remoteInvSize:"+remoteNumSlots);
            
            NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
            NBTTagList nbttagremote = nbttagcompound.getTagList("remoteItems");
            
            this.contents = new ItemStack[this.getSizeInventory()];
            if (remoteNumSlots != 0)
            {
                remoteItems = new ItemStack[remoteNumSlots];
            }

            //our inventory
            for (int i = 0; i < nbttaglist.tagCount(); ++i)
            {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
                int j = nbttagcompound1.getByte("Slot") & 255;

                if (j >= 0 && j < this.contents.length)
                {
                    this.contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
                }
            }

            //remote inventory
            System.out.println("ReadNBT tagRemoteCount: "+nbttagremote.tagCount());
            if (nbttagremote.tagCount() != 0)
            {
                for (int i = 0; i < nbttagremote.tagCount(); ++i)
                {
                    NBTTagCompound remoteItems1 = (NBTTagCompound)nbttagremote.tagAt(i);
                    int j = remoteItems1.getByte("Slot") & 255;

                    if (j >= 0 && j < remoteItems.length)
                    {
                        remoteItems[j] = ItemStack.loadItemStackFromNBT(remoteItems1);
                    }
                }
                //System.out.println("ReadNBT Remote ItemID: "+remoteItems[0].itemID);
            }
        }
    }

    /**
     * Writes a tile entity to NBT.
     */
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        if(!Utils.isClient(worldObj))
        {
            super.writeToNBT(nbttagcompound);
            NBTTagList nbttaglist = new NBTTagList();
            NBTTagList nbttagremote = new NBTTagList();
            
            //our inventory
            for (int i = 0; i < this.contents.length; ++i)
            {
                if (this.contents[i] != null)
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setByte("Slot", (byte)i);
                    this.contents[i].writeToNBT(nbttagcompound1);
                    nbttaglist.appendTag(nbttagcompound1);
                }
            }
            
            //remote inventory
            if (remoteItems != null)
            {
                for (int i = 0; i < remoteItems.length; i++)
                {
                    if (remoteItems[i] != null)
                    {
                        NBTTagCompound remoteItems1 = new NBTTagCompound();
                        remoteItems1.setByte("Slot", (byte)i);
                        remoteItems[i].writeToNBT(remoteItems1);
                        nbttagremote.appendTag(remoteItems1);
                    }
                }
            }
                        
            //write stuff to NBT here
            nbttagcompound.setTag("Items", nbttaglist);
            nbttagcompound.setTag("remoteItems", nbttagremote);
            nbttagcompound.setString("targetTileName", targetTileName);
            nbttagcompound.setInteger("remoteItemsSize", remoteNumSlots);
            
            System.out.println("writeNBT: "+targetTileName+" remoteInvSize:"+remoteNumSlots);
            System.out.println("writeNBT Remote ItemID: "+remoteItems[0].itemID);
        }
    }

    public int getInventoryStackLimit()
    {
        return 64;
    }

    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
        {
            return false;
        }

        return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
    }

    public void openChest()
    {
        // TODO Auto-generated method stub
    }

    public void closeChest()
    {
        // TODO Auto-generated method stub
    }

    public boolean getSnapshotState()
    {
        /*
         * returns true if it has a snapshot, false if it doesn't have one
         * Does NOT test if the snapshot should still be valid
         * (remote inventory changed, removed, etc)
         */
        return snapShotState;
    }

    public ItemStack[] takeSnapShot(TileEntity tile)
    {
        /*
         * This function will take a snapshot the IInventory of the TileEntity passed to it
         * and return it as a new ItemStack. This will be a copy of the remote inventory as
         * it looks when this function is called.
         *
         * It will check that the TileEntity passed to it actually implements IInventory and
         * return null if it does not.
         */
        if (!(tile instanceof IInventory))
        {
            return null;
        }

        // Get number of slots in the remote inventory
        remoteNumSlots = ((IInventory)tile).getSizeInventory();
        ItemStack tempCopy;
        ItemStack returnCopy[] = new ItemStack[remoteNumSlots];

        // Iterate through remote slots and make a copy of it
        for (int i = 0; i < remoteNumSlots; i++)
        {
            tempCopy = ((IInventory)tile).getStackInSlot(i);

            if (tempCopy == null)
            {
                returnCopy[i] = null;
            }
            else
            {
                returnCopy[i] = new ItemStack(tempCopy.itemID, tempCopy.stackSize, tempCopy.getItemDamage());

                if (tempCopy.stackTagCompound != null)
                {
                    returnCopy[i].stackTagCompound = (NBTTagCompound)tempCopy.stackTagCompound.copy();
                }
            }
        }
        /*
         *  get remote entity class name and store it as targetTile, which also ends up being stored in our
         *  own NBT tables so our tile will remember what was there being chunk unloads/restarts/etc
         */
        targetTileName = tile.getClass().getName();
        System.out.println("StackSize: "+returnCopy[0].stackSize);
        return returnCopy;
    }

    public void storeRemoteInventory(TileEntity tile, int hash)
    {
        if(!Utils.isClient(worldObj))
        {
            
        }
    }
    
    public boolean stockInventory(TileEntity tile, ItemStack itemstack[])
    {
        /*
         * This function is the main worker for our block. It takes a TileEntity and
         * an ItemStack array as inputs and will attempt to use its internal inventory
         * object to do the following:
         *
         * Iterate through the remote inventory, comparing each slot with our local
         * snapshot we took earlier.
         *
         * If a remote inventory slot has the wrong item in it, remove it to the local inventory
         * output, then see if we can fill the remote slot with the correct item and quantity from
         * our local inventory, in reverse slot order. If the remote slot should be empty, set it
         * as such.
         *
         * If a remote inventory slot is empty and shouldn't be, attempt to correct that
         * using our local inventory in reverse slot order.
         *
         * If a remote inventory slot has the correct item, check that it has the correct quantity
         * of the item. If not, attempt to correct that using our local inventory in reverse slot
         * order.
         *
         * At the moment, this will only key on damage values if the ItemID is stackable. If it is
         * not stackable, then it will ignore damage values for determining if the remote inventory
         * has the correct item in it already.
         *
         */
        if(!Utils.isClient(worldObj))
        {
            // test to make sure we're actually passed stuff that makes sense
            if (tile != null && tile instanceof IInventory && itemstack != null)
            {
                // do code here
            }

            // return false for now to avoid errors
            return false;
        }
        return false;
    }

    public void onLoad()
    {
        /*
         * This function fires only once on first load of an instance of our tile and attempts to see
         * if we should have a valid inventory or not. it will set the lastTileEntity var, and
         * snapShotState. The actual remoteInventory object will be loaded (or not) via the NBT calls.
         */
        if(!Utils.isClient(worldObj))
        {
            tileLoaded = true;
            TileEntity tile = getTileAtFrontFace();
            if (tile != null)
            {
                String tempName = tile.getClass().getName();
                if (tempName.equals(targetTileName))
                {
                    System.out.println("onLoad, tname="+tempName+" tarname="+targetTileName+" MATCHED");
                    lastTileEntity = tile;
                    snapShotState = true;
                    return;
                }
                else
                {
                    System.out.println("onLoad, tname="+tempName+" tarname="+targetTileName+" NOT MATCHED");
                    clearSnapshot();
                    return;
                }
            }
            else
            {
                System.out.println("onLoad tile = null");
            }
        }
    }
        

    public void onUpdate()
    {
        if(!Utils.isClient(worldObj))
        {
            TileEntity tile = getTileAtFrontFace();
            if (tile != null)
            {
                String tempName = tile.getClass().getName();
                if (!tempName.equals(targetTileName))
                {
                    clearSnapshot();
                    System.out.println("onUpdate clear, tname="+tempName+" tarname="+targetTileName);
                    return;
                }
                else if (tile != lastTileEntity)
                {
                    clearSnapshot();
                    System.out.println("onUpdate clear, tileEntity does not match lastTileEntity");
                    return;
                }
                else
                {
                    return;
                }
            }
            else
            {
                System.out.println("onUpdate clear, tile = null");
                clearSnapshot();
            }
        }
    }
    
    public void clearSnapshot()
    {
        lastTileEntity = null;
        snapShotState = false;
        targetTileName = "none";
        remoteItems = null;
        remoteNumSlots = 0;
    }
    
    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if(!Utils.isClient(worldObj))
        {
            /*
             * See if this tileEntity instance has ever loaded, if not, do some onLoad stuff to restore prior state
             */
            if (!tileLoaded)
            {
                System.out.println("tileLoaded false, running onLoad");
                this.onLoad();
            }

            /*
             * Need to update this function to properly reference remote NBTTags and block ID to verify
             * if our block is still the same and store that information in our own NBTTag to compare
             * after we are saved to disk and reloaded (server restart, player quit SSP, chunk unload
             * etc)
             * 
             * String NBTTagKeyID = (String)classToNameMap.get(remoteTile.getClass());
             * get remote name
             * and x,y,z and store it in our own tag to match on load
             */
            
            /*
             * check if one of the blocks next to us or us is getting power from a neighboring block. 
             */
            
            boolean isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

            /*
             * If we're not powered, set the previousPoweredState to false
             */
            if (!isPowered)
            {
                previousPoweredState = false;
            }

            /*
             * If we are powered and the previous power state is false, it's time to go to
             * work. We test it this way so that we only trigger our work state once
             * per redstone power state cycle (pulse).
             */
            if (isPowered && !previousPoweredState)
            {
                //We're powered now, set the state flag to true
                previousPoweredState = true;
                System.out.println("Powered");

                //grab TileEntity at front face
                TileEntity tile = getTileAtFrontFace();
                
                //Verify that the tile we got back exists and implements IInventory            
                if (tile != null && tile instanceof IInventory)
                {
                    /*
                     * Code here deals with the adjacent inventory
                     */
                    System.out.println("Chest Found!");

                    /*
                     * Check if our snapshot is considered valid and/or the tile we just got doesn't
                     * match the one we had prior.
                     */
                    if (!getSnapshotState() || tile != lastTileEntity)
                    {
                        System.out.println("Taking snapshot");
                        /*
                         * Take a snapshot of the remote inventory, set the lastEntity to the current
                         * remote entity and set the snapshot flag to true
                         */
                        ItemStack remoteItems[] = takeSnapShot(tile);
                        System.out.println("StackSizeRR: "+remoteItems[0].stackSize);
                        System.out.println("OnUpdateRemote ItemID: "+remoteItems[0].itemID);
                        lastTileEntity = tile;
                        snapShotState = true;
                    }
                    else
                    {
                        /*
                         * If we've made it here, it's time to stock the remote inventory
                         */
                        stockInventory(tile, remoteItems);
                    }
                }
                else
                {
                    /*
                     * This code deals with us not getting a valid tile entity from
                     * the getTileAtFrontFace code. This can happen because there is no
                     * detected tileentity (returned false), or the tileentity that was returned
                     * does not implement IInventory. We will clear the last snapshot.
                     */
                    clearSnapshot();
                    System.out.println("entityUpdate snapshot clear");
                }
            }
        }
    }
}
