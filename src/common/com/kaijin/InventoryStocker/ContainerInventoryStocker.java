/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

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
	private int guiInfo = -1;

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

	@Override
	public void updateCraftingResults()
	{
		super.updateCraftingResults(); // Make sure to synch inventory updates to the client too!

		int tileinfo = (tile.hasSnapshot ? 4 : 0) | (tile.operationMode.ordinal() & 3);
		for (int crafterIndex = 0; crafterIndex < crafters.size(); ++crafterIndex)
		{
			ICrafting crafter = (ICrafting)crafters.get(crafterIndex);
			if (guiInfo != tileinfo)
			{
				// Case 0
				crafter.updateCraftingInventoryInfo(this, 0, tileinfo & 65535); // packet uses 16 bit short int
			}
		}
		guiInfo = tileinfo;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int param, int value)
	{
		if (Info.isDebugging) System.out.println("updateProgressBar param: " + param + " value: " + value);
		switch (param)
		{
		case 0:
			tile.operationMode = StockMode.getMode(value & 3);
			tile.hasSnapshot = (value & 4) == 4;
		}
	}

	@Override
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
}
