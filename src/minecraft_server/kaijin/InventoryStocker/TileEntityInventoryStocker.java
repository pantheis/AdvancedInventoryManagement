package kaijin.InventoryStocker;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class TileEntityInventoryStocker extends TileEntity implements IInventory, ISidedInventory
{
    private ItemStack contents[];

    public TileEntityInventoryStocker()
    {
        contents = new ItemStack [this.getSizeInventory()];
    }

    public int getStartInventorySide(int side)
    {
       	if (side < 2)
    		return 9; // Sides 0 and 1 access output section, 9-17
    	
    	return 0; // Sides 2-5 access input section, 0-8
     }

    public int getSizeInventorySide(int side)
    {
    	return 9;
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
}
