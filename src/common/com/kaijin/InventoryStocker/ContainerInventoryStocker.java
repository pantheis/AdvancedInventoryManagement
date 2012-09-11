/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import com.kaijin.InventoryStocker.*;

import cpw.mods.fml.common.network.Player;

import net.minecraft.src.*;

public class ContainerInventoryStocker extends Container
{
    private IInventory playerinventory;
    private TileEntityInventoryStocker inventorystockerinventory;
    private List<String> guiPlayerList = new ArrayList<String>();
    private EntityPlayer human;

    public ContainerInventoryStocker(IInventory playerinventory, TileEntityInventoryStocker inventorystockerinventory, EntityPlayer player)
    {
        this.playerinventory = playerinventory;
        this.inventorystockerinventory = inventorystockerinventory;
        this.human = player;
        int xCol;
        int yRow;

        for (yRow = 0; yRow < 3; ++yRow)
        {
            for (xCol = 0; xCol < 3; ++xCol)
            {
                this.addSlotToContainer(new Slot(inventorystockerinventory, xCol + 3 * yRow, 8 + xCol * 18, 18 + yRow * 18));
            }
        }

        for (yRow = 0; yRow < 3; ++yRow)
        {
            for (xCol = 0; xCol < 3; ++xCol)
            {
                this.addSlotToContainer(new Slot(inventorystockerinventory, 9 + xCol + 3 * yRow, 116 + xCol * 18, 18 + yRow * 18));
            }
        }

        for (yRow = 0; yRow < 3; ++yRow)
        {
            for (xCol = 0; xCol < 9; ++xCol)
            {
                this.addSlotToContainer(new Slot(playerinventory, xCol + yRow * 9 + 9, 8 + xCol * 18, 86 + yRow * 18));
            }
        }

        for (xCol = 0; xCol < 9; ++xCol)
        {
            this.addSlotToContainer(new Slot(playerinventory, xCol, 8 + xCol * 18, 144));
        }
    }

    public boolean canInteractWith(EntityPlayer entityplayer)
    {
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

            if (par1 < 18)
            {
                if (!this.mergeItemStack(var4, 18, this.inventorySlots.size(), true))
                {
                    return null;
                }
            }
            else if (!this.mergeItemStack(var4, 0, 18, false))
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
    
    public void addCraftingToCrafters(ICrafting par1ICrafting)
    {
        super.addCraftingToCrafters(par1ICrafting);
        guiPlayerList.add(((EntityPlayerMP)par1ICrafting).username);
        inventorystockerinventory.sendSnapshotStateClient((Player)(par1ICrafting));
        inventorystockerinventory.entityOpenList(guiPlayerList);
        
    }
    /**
     * Callback for when the crafting gui is closed.
     */
    public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
    {
        super.onCraftGuiClosed(par1EntityPlayer);
        if (guiPlayerList.contains(par1EntityPlayer.username))
        {
            guiPlayerList.remove(par1EntityPlayer.username);
            inventorystockerinventory.entityOpenList(guiPlayerList);
        }
    }
}
