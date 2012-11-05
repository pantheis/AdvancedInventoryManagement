/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ICrafting;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Slot;

public class ContainerInventoryStocker extends Container
{
	private TileEntityInventoryStocker tile;
	private List<EntityPlayerMP> guiPlayerList = new ArrayList<EntityPlayerMP>();

	public ContainerInventoryStocker(InventoryPlayer playerinventory, TileEntityInventoryStocker stocker)
	{
		tile = stocker;
		int xCol;
		int yRow;

		for (yRow = 0; yRow < 3; ++yRow)
		{
			for (xCol = 0; xCol < 3; ++xCol)
			{
				this.addSlotToContainer(new Slot(stocker, xCol + 3 * yRow, 8 + xCol * 18, 28 + yRow * 18));
			}
		}

		for (yRow = 0; yRow < 3; ++yRow)
		{
			for (xCol = 0; xCol < 3; ++xCol)
			{
				this.addSlotToContainer(new Slot(stocker, 9 + xCol + 3 * yRow, 116 + xCol * 18, 28 + yRow * 18));
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
		return this.tile.isUseableByPlayer(entityplayer);
	}

	//Updated function for transferStackInSlot(int slot)
	@Override
	public ItemStack func_82846_b(EntityPlayer p, int i)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(i);
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (i < 18)
			{
				if (!mergeItemStack(itemstack1, 18, inventorySlots.size(), true))
				{
					return null;
				}
			} else if (!mergeItemStack(itemstack1, 0, 18, false))
			{
				return null;
			}
			if (itemstack1.stackSize == 0)
			{
				slot.putStack(null);
			} else
			{
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}

//	public ItemStack transferStackInSlot(int par1)
//	{
//		ItemStack var2 = null;
//		Slot var3 = (Slot)this.inventorySlots.get(par1);
//
//		if (var3 != null && var3.getHasStack())
//		{
//			ItemStack var4 = var3.getStack();
//			var2 = var4.copy();
//
//			if (par1 < 18)
//			{
//				if (!this.mergeItemStack(var4, 18, this.inventorySlots.size(), true))
//				{
//					return null;
//				}
//			}
//			else if (!this.mergeItemStack(var4, 0, 18, false))
//			{
//				return null;
//			}
//
//			if (var4.stackSize == 0)
//			{
//				var3.putStack((ItemStack)null);
//			}
//			else
//			{
//				var3.onSlotChanged();
//			}
//		}
//
//		return var2;
//	}

	@Override
	public void addCraftingToCrafters(ICrafting par1ICrafting)
	{
		super.addCraftingToCrafters(par1ICrafting);
		if (Info.isDebugging)
		{
			System.out.println("gui.addCraftingToCrafters");
			String n = ((EntityPlayerMP)par1ICrafting).username;
			System.out.println("container.addCraftingToCrafters.server: " + n);
		}
		guiPlayerList.add(((EntityPlayerMP)par1ICrafting));
		tile.sendSnapshotStateClient((EntityPlayerMP)(par1ICrafting));
		tile.entityOpenList(guiPlayerList);
	}
	/**
	 * Callback for when the crafting gui is closed.
	 */
	@Override
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer)
	{
		super.onCraftGuiClosed(par1EntityPlayer);
		if (Info.isDebugging) System.out.println("gui.onCraftGuiClosed-client+server");
		if (InventoryStocker.proxy.isServer())
		{
			if (Info.isDebugging) System.out.println("gui.onCraftGuiClosed-SERVER");
			if (guiPlayerList.contains(((EntityPlayerMP)par1EntityPlayer)))
			{
				if (Info.isDebugging)
				{
					System.out.println("gui.addCraftingToCrafters");
					String n = ((EntityPlayerMP)par1EntityPlayer).username;
					System.out.println("gui.onCraftGuiClosed.RemoveNameFromList: " + n);
				}
				guiPlayerList.remove(((EntityPlayerMP)par1EntityPlayer));
				tile.entityOpenList(guiPlayerList);
			}
		}
	}
}
