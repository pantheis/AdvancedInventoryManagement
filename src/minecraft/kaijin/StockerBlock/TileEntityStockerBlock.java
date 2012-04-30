package kaijin.StockerBlock;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class TileEntityStockerBlock extends TileEntity implements IInventory, ISidedInventory
{

    private ItemStack contents[];
    
	public TileEntityStockerBlock() {
		contents = new ItemStack [getSizeInventory()];		
	}	
	
	@Override
	public int getStartInventorySide(int side) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeInventorySide(int side) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return 26;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return contents[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		// TODO Auto-generated method stub
        if(contents[i] != null) {
            if(contents[i].stackSize <= j)
            {
                ItemStack itemstack = contents[i];
                contents[i] = null;
//                onInventoryChanged();
            }
            ItemStack itemstack1 = contents[i].splitStack(j);
            
            if(contents[i].stackSize == 0) {
                contents[i] = null;
            }
            return itemstack1;
//          onInventoryChanged();
        } else
        {
            return null;
        }
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (this.contents[var1] == null) return null;
		ItemStack stack = this.contents[var1];
		this.contents[var1] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
        contents[i] = itemstack;
        if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
	}

	@Override
	public String getInvName() {
		return "StockerBlock";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		// TODO Auto-generated method stub
        if(worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
            return false;
        }
        return entityplayer.getDistanceSq((double)xCoord + 0.5D, (double)yCoord + 0.5D, (double)zCoord + 0.5D) <= 64D;
    }

	@Override
	public void openChest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub
		
	}

}
