package kaijin.InventoryStocker;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;
import kaijin.InventoryStocker.*;

public class ContainerInventoryStocker extends Container
{
    private IInventory playerinventory;
    private IInventory inventorystockerinventory;
    private int numRows;

    public ContainerInventoryStocker(IInventory playerinventory, IInventory inventorystockerinventory)
    {
        this.numRows = inventorystockerinventory.getSizeInventory() / 9;
        this.playerinventory = playerinventory;
        this.inventorystockerinventory = inventorystockerinventory;
        int var3 = (this.numRows - 4) * 18;
        int var4;
        int var5;

        for (var4 = 0; var4 < this.numRows; ++var4)
        {
            for (var5 = 0; var5 < 9; ++var5)
            {
                this.addSlot(new Slot(inventorystockerinventory, var5 + var4 * 9, 8 + var5 * 18, 18 + var4 * 18));
            }
        }

        for (var4 = 0; var4 < 3; ++var4)
        {
            for (var5 = 0; var5 < 9; ++var5)
            {
                this.addSlot(new Slot(playerinventory, var5 + var4 * 9 + 9, 8 + var5 * 18, 103 + var4 * 18 + var3));
            }
        }

        for (var4 = 0; var4 < 9; ++var4)
        {
            this.addSlot(new Slot(playerinventory, var4, 8 + var4 * 18, 161 + var3));
        }
    }

    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        // TODO Auto-generated method stub
        return this.inventorystockerinventory.isUseableByPlayer(entityplayer);
    }

    public ItemStack transferStackInSlot(int par1)
    {
        ItemStack var2 = null;
        Slot var3 = (Slot)this.inventorySlots.get(par1);

        if (var3 != null && var3.getHasStack())
        {
            ItemStack var4 = var3.getStack();
            var2 = var4.copy();

            if (par1 < this.numRows * 9)
            {
                if (!this.mergeItemStack(var4, this.numRows * 9, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(var4, 0, this.numRows * 9, false))
            {
                return null;
            }

            if (var4.stackSize == 0)
            {
                var3.putStack((ItemStack)null);
            }
            else
            {
                var3.onSlotChanged();
            }
        }

        return var2;
    }
    /**
     * Callback for when the crafting gui is closed.
     */
    public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
    {
        super.onCraftGuiClosed(par1EntityPlayer);
        this.inventorystockerinventory.closeChest();
    }
}