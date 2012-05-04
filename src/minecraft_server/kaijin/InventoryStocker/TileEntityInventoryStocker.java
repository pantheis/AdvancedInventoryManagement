package kaijin.InventoryStocker;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import kaijin.InventoryStocker.*;

public class TileEntityInventoryStocker extends TileEntity implements IInventory, ISidedInventory
{
    private ItemStack contents[];
    private boolean previousPoweredState = false;
    private boolean snapShotState = false;

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
			return 9; // access output section, 9-17
		return 0; // access input section, 0-8
	}
	
	public int getSizeInventorySide(int i)
	{
		// Sides (0-5) are: Top, Bottom, Front, Back, Left, Right
		int side = getRotatedSideFromMetadata(i);
		if (side == 0)
			return 0; // Front has no inventory access
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
        
        switch(dir)
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
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        this.contents = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 0 && j < this.contents.length)
            {
                this.contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
    }

    /**
     * Writes a tile entity to NBT.
     */
    public void writeToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeToNBT(nbttagcompound);
        NBTTagList nbttaglist = new NBTTagList();

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

        nbttagcompound.setTag("Items", nbttaglist);
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
		int numSlots = ((IInventory)tile).getSizeInventory();
		ItemStack tempCopy;
		ItemStack returnCopy[] = new ItemStack[numSlots];

		// Iterate through remote slots and make a copy of it
		for (int i = 0; i < numSlots; i++)
		{
			tempCopy = ((IInventory)tile).getStackInSlot(i);
			if (tempCopy == null){
				returnCopy[i] = null;
			}
			else
			{
				returnCopy[i] = new ItemStack(tempCopy.itemID, tempCopy.stackSize, tempCopy.getItemDamage());
				if(tempCopy.stackTagCompound != null)
				{
					returnCopy[i].stackTagCompound = (NBTTagCompound)tempCopy.stackTagCompound.copy();
				}
			}
		}
        return returnCopy;
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

		// test to make sure we're actually passed stuff that makes sense
		if (tile != null && tile instanceof IInventory && itemstack != null)
		{
			// do code here
		}
		// return false for now to avoid errors
		return false;
	}

	@Override
	public void updateEntity ()
	{
		super.updateEntity();
		boolean isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord,yCoord,zCoord);
		if (!isPowered) previousPoweredState = false;
		if (isPowered && !previousPoweredState)
		{
	        previousPoweredState = true;
			TileEntity tile = getTileAtFrontFace();
			if(tile != null && tile instanceof IInventory)
			{
				/*
				 * Put code here that will deal with the adjacent inventory
				 * 
				 * ModLoader.getMinecraftInstance().thePlayer.addChatMessage("It works!");
				 * 
				 */
			    ItemStack remoteItems[] = takeSnapShot(tile);
			    stockInventory(tile, remoteItems);
			}
		}
	}
}
