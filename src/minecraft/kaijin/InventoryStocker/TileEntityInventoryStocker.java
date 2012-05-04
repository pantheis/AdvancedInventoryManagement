package kaijin.InventoryStocker;

import net.minecraft.client.*;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import kaijin.InventoryStocker.*;

public class TileEntityInventoryStocker extends TileEntity implements IInventory, ISidedInventory
{
	private ItemStack contents[];
	
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
	
	@Override
	public void updateEntity ()
	{
		super.updateEntity();
		boolean isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord,yCoord,zCoord);
		if (isPowered)
		{
			TileEntity tile = getTileAtFrontFace();
			if(tile != null && tile instanceof IInventory)
			{
				/*
				 * Put code here that will deal with the adjacent inventory
				 * 
				 * ModLoader.getMinecraftInstance().thePlayer.addChatMessage("It works!");
				 * 
				 */
			    ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Chest Found!");
				
			}
		}
	}
}
