/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class BlockInventoryStocker extends BlockContainer
{
	public BlockInventoryStocker(int i, int j, Material material)
	{
		super(i, j, material);
	}

	@Override
	public String getTextureFile()
	{
		return Info.BLOCK_PNG;
	}

	@Override
	public int getBlockTextureFromSide(int i)
	{
		switch (i)
		{
		case 0: // Bottom
			return 16;

		case 1: // Top
			return 0;

		case 2: // North
			return 16;

		case 3: // South
			return 16;

		default: // 4-5 West-East
			return 16;
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getBlockTexture(IBlockAccess blocks, int x, int y, int z, int i)
	{
		TileEntity tile = blocks.getBlockTileEntity(x, y, z);
		if (tile instanceof TileEntityInventoryStocker)
		{
			int m = ((TileEntityInventoryStocker)tile).metaInfo;
			int dir = m & 7;
			int side = Utils.lookupRotatedSide(i, dir);
			int powered = (m & 8) >> 3;

			//if (InventoryStocker.isDebugging) System.out.println("getBlockTexture - m = " + m);

			// Sides (0-5) are: Front, Back, Top, Bottom, Left, Right
			if (side == 0) // Front
			{
				//TODO Removing animation for now, unless a way to do it without spamming renderer updates can be devised
				//int time = (int)tile.worldObj.getWorldTime();
				//return 2 + powered * (((time >> 2) & 3) + 1);
				return 1 + powered;
			}

			int open = (2 & (m >> (i + 3))); // Bit i + 4 shifted to the 2's place and isolated

			if (side == 1) // Back
			{
				return 32 + powered + open;
			}

			return 16 + powered + open; // Top, Bottom, Left, Right
		}
		return i;
	}

	private int determineOrientation(World world, int x, int y, int z, EntityLiving player)
	{
		if (player.rotationPitch > 45D)
		{
			return 0;
		}

		if (player.rotationPitch < -45D)
		{
			return 1;
		}

		int dir = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		return dir == 0 ? 3 : (dir == 1 ? 4 : (dir == 2 ? 2 : (dir == 3 ? 5 : 0)));
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving par5EntityLiving)
	{
		int dir = determineOrientation(world, x, y, z, par5EntityLiving);
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(tile instanceof TileEntityInventoryStocker)
		{
			((TileEntityInventoryStocker)tile).metaInfo = dir;
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
	{
		if (InventoryStocker.proxy.isClient())
		{
			return !entityplayer.isSneaking();
		}

		if (entityplayer.isSneaking())
		{
			// Prevent GUI pop-up and handle block rotation
			if (Info.isDebugging) System.out.println("BlockInvStock: isServer && isSneaking");
			if (entityplayer.getCurrentEquippedItem() == null)
			{
				TileEntity tile = world.getBlockTileEntity(x, y, z);
				if (tile instanceof TileEntityInventoryStocker)
				{
					((TileEntityInventoryStocker)tile).rotateBlock();
				}
			}

			// Prevent GUI popup when sneaking
			// Allows you to place things directly on the inventory stocker, or rotate it, handled above
			return false;
		}

		// If we got here, we're not sneaking, time to get to work opening the GUI
		// Duplicate part of onNeighborBlockChange to ensure status is up-to-date before GUI opens
		//TODO Do we really actually need to do this still? What problem did it solve? If it still exists, can we do it differently? 
		//TileEntity tile = world.getBlockTileEntity(x, y, z);
		//if (tile instanceof TileEntityInventoryStocker)
		//{
			//((TileEntityInventoryStocker)tile).onUpdate();
		//}

		/*if (InventoryStocker.isDebugging) System.out.println("BlockInventoryStocker.onBlockActivated.openGUI");
		if (InventoryStocker.isDebugging)
		{
			if (entityplayer instanceof EntityPlayerMP)
			{
				System.out.println("Block-EntityPlayer instance of EntityPlayerMP");
			}
			else
			{
				System.out.println("Block-EntityPlayer NOT instance of EntityPlayerMP");
			}
		}*/

		entityplayer.openGui(InventoryStocker.instance, 1, world, x, y, z);
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1)
	{
		return null;
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		//if (InventoryStocker.isDebugging) System.out.println("BlockInventoryStocker.createTileEntity");
		return new TileEntityInventoryStocker();
	}

	@Override
	public boolean canProvidePower()
	{
		return false; // Old means of causing visual RedPower wire connections.
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int X, int Y, int Z, int direction)
	{
		return true; // Will appear to connect to RedPower wires and such.
		//FIXME Currently still causes redstone dust to appear to connect in some cases where it shouldn't; Not our fault.
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	/**
	 * This is called when something changes near our block, we use it to detect placement of pipes/tubes
	 * so we can update our textures. We also use it to verify the attached storage inventory hasn't been
	 * removed or changed.
	 */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		if (Info.isDebugging) System.out.println("BlockInventoryStocker.onNeighborBlockChange");
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (tile instanceof TileEntityInventoryStocker)
		{
			((TileEntityInventoryStocker)tile).onBlockUpdate();
		}
		super.onNeighborBlockChange(world, x, y, z, blockID);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta)
	{
		preDestroyBlock(world, x, y, z);
	}

	public static void dropItem(World world, ItemStack stack, int i, int j, int k)
	{
		float f1 = 0.7F;
		double d = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d1 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d2 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, (double) i + d,
				(double) j + d1, (double) k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(entityitem);
	}

	public static void dropItems(World world, IInventory inventory, int i, int j, int k)
	{
		for (int l = 0; l < inventory.getSizeInventory(); ++l)
		{
			ItemStack items = inventory.getStackInSlot(l);

			if (items != null && items.stackSize > 0)
			{
				dropItem(world, inventory.getStackInSlot(l).copy(), i, j, k);
			}
		}
	}

	public static void preDestroyBlock(World world, int i, int j, int k)
	{
		if (InventoryStocker.proxy.isClient()) return;

		TileEntity tile = world.getBlockTileEntity(i, j, k);
		if (tile instanceof IInventory)
		{
			dropItems(world, (IInventory) tile, i, j, k);
			tile.invalidate();
		}
	}
}
