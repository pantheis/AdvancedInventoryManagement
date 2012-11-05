/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import buildcraft.api.core.Orientations;
import buildcraft.api.transport.IPipeConnection;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityChest;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import cpw.mods.fml.common.network.PacketDispatcher;

public class TileEntityInventoryStocker extends TileEntity implements IInventory, ISidedInventory
{
	private ItemStack[] contents = new ItemStack[this.getSizeInventory()];
	private ItemStack remoteSnapshot[];
	private ItemStack extendedChestSnapshot[];

	public int metaInfo = 0;
	public int operationMode = 0;

	private boolean guiTakeSnapshot = false;
	private boolean guiClearSnapshot = false;
	private boolean tileLoaded = false;
	private boolean previousPoweredState = false;
	private boolean lightState = false;

	public boolean hasSnapshot = false;
	private boolean lastSnapshotState = false;
	private boolean reactorWorkaround = false;
	private int reactorWidth = 0;
	private TileEntity lastTileEntity = null;
	private TileEntity tileFrontFace = null;
	private TileEntityChest extendedChest = null;
	private int remoteNumSlots = 0;
	private String targetTileName = "none";

	private final String classnameIC2ReactorCore = "TileEntityNuclearReactor";
	private final String classnameIC2ReactorChamber = "TileEntityReactorChamber";

	private final int tickDelay = 9; // How long (in ticks) to wait between stocking operations
	private int tickTime = 0;
	
	private int updateDelay = 0; // For delayed pipe/tube door updating

	//TODO Record relative offset from stocker to reactor core when reactor workaround in use, for testing if chunk is loaded
	private Coords reactorOffset = new Coords(0, 0, 0);

	public TileEntityInventoryStocker()
	{
		super();
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	/**
	 * Returns current value of hasSnapshot
	 * @return
	 */
	public boolean validSnapshot()
	{
		return hasSnapshot;
	}

	public void clearSnapshot()
	{
		if (Info.isDebugging) System.out.println("clearSnapshot()");
		lastTileEntity = null;
		hasSnapshot = false;
		targetTileName = "none";
		remoteSnapshot = null;
		remoteNumSlots = 0;
		extendedChest = null;
		extendedChestSnapshot = null;
		reactorWorkaround = false;
		reactorWidth = 0;
	}

	/**
	 * Called when neighboring blocks change or other cases where a state update is needed
	 */
	public void onBlockUpdate()
	{
		if (!InventoryStocker.proxy.isClient())
		{
			// If snapshot target's chunk is not loaded, snapshot tests are skipped
			if (isTargetChunkLoaded() && checkInvalidSnapshot() && validSnapshot())
			{
				if (Info.isDebugging) System.out.println("onUpdate.!isClient.checkInvalidSnapshot.clearSnapshot");
				clearSnapshot();
			}
			// Flag to check adjacent blocks for tubes or pipes on next entity update tick
			if (updateDelay <= 0) updateDelay = 1;
		}
	}

	/**
	 * Called to discover neighboring pipe and tube connections 
	 */
	private void updateDoorStates()
	{
		//if (InventoryStocker.isDebugging) System.out.println("Update Door States");
		int oldInfo = metaInfo;
		int doorFlags = 0;
		doorFlags |= findTubeOrPipeAt(xCoord,   yCoord-1, zCoord,   Orientations.YPos) ? 16 : 0;
		doorFlags |= findTubeOrPipeAt(xCoord,   yCoord+1, zCoord,   Orientations.YNeg) ? 32 : 0;
		doorFlags |= findTubeOrPipeAt(xCoord,   yCoord,   zCoord-1, Orientations.ZPos) ? 64 : 0;
		doorFlags |= findTubeOrPipeAt(xCoord,   yCoord,   zCoord+1, Orientations.ZNeg) ? 128 : 0;
		doorFlags |= findTubeOrPipeAt(xCoord-1, yCoord,   zCoord,   Orientations.XPos) ? 256 : 0;
		doorFlags |= findTubeOrPipeAt(xCoord+1, yCoord,   zCoord,   Orientations.XNeg) ? 512 : 0;
		metaInfo ^= (metaInfo & 1008); // 1008 = bits 4 through 9 (zero based), this sets them to 0 without having to know what other bits need preservation
		metaInfo |= doorFlags;
		if (metaInfo != oldInfo) worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}

	/**
	 * @param x coord of block to test
	 * @param y coord of block to test
	 * @param z coord of block to test
	 * @param direction in BC API Orientations
	 * @return boolean
	 * <pre>
	 * RedPower connections:
	 *
	 * Meta  Tile Entity
	 * 8     eloraam.machine.TileTube
	 * 9     eloraam.machine.TileRestrictTube
	 * 10    eloraam.machine.TileRedstoneTube
	 *
	 * All are block class: eloraam.base.BlockMicro
	 * 
	 * Buildcraft connections:
	 * 
	 * All pipe tile entities implement IPipeConnection.
	 * Call isPipeConnected with the direction pointing back toward our block.
	 */
	private boolean findTubeOrPipeAt(int x, int y, int z, Orientations dir)
	{
		if (worldObj.blockExists(x, y, z))
		{
			// BuildCraft Pipe test
			TileEntity tile = worldObj.getBlockTileEntity(x,  y, z);
			if (tile instanceof IPipeConnection)
			{
				return ((IPipeConnection)tile).isPipeConnected(dir);
			}

			// RedPower Tube test
			int ID = worldObj.getBlockId(x, y, z);
			if (ID > 0)
			{
				String type = Block.blocksList[ID].getClass().getName();
				if (type.endsWith("eloraam.base.BlockMicro"))
				{
					int m = worldObj.getBlockMetadata(x, y, z);
					return (m >= 8) && (m <= 10);
				}
			}
		}
		return false;
	}

	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		// TODO Make sure the numbering and orientation matches up properly!
		// Sides (0-5) are: Front, Back, Top, Bottom, Right, Left
		int i = getRotatedSideFromMetadata(side.ordinal());
		//if (InventoryStocker.isDebugging) System.out.println("side.ordinal(): " + side.ordinal() + " face: " + i);

		if (i == 1)
		{
			return 9;    // access output section, 9-17
		}
		return 0; // access input section, 0-8
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		// TODO Make sure the numbering and orientation matches up properly!
		// Sides (0-5) are: Top, Bottom, Front, Back, Left, Right
		int i = getRotatedSideFromMetadata(side.ordinal());
		//if (InventoryStocker.isDebugging) System.out.println("side.ordinal(): " + side.ordinal() + " face: " + i);

		if (i == 0)
		{
			return 0; // Front has no inventory access
		}
		return 9;
	}

	public int getRotatedSideFromMetadata(int side)
	{
		final int dir = this.metaInfo & 7;
		return Utils.lookupRotatedSide(side, dir);
	}

	public int getBlockIDAtFace(int i)
	{
		final ForgeDirection face = ForgeDirection.getOrientation(i);
		return worldObj.getBlockId(xCoord + face.offsetX, yCoord + face.offsetY, zCoord + face.offsetZ);
	}

	public TileEntity getTileAtFrontFace()
	{
		final ForgeDirection face = ForgeDirection.getOrientation(metaInfo & 7);
		return worldObj.getBlockTileEntity(xCoord + face.offsetX, yCoord + face.offsetY, zCoord + face.offsetZ);
	}

	/**
	 * Will check if the chunk the block at the front face is in is loaded
	 * @return boolean
	 */
	private boolean isTargetChunkLoaded()
	{
		//TODO Test with reactors where stocker is attached to a chamber and separated from core by a block.
		if (reactorWorkaround)
		{
			// The chamber and core may not be in the same chunk! Store core location somewhere and check that instead.
			return worldObj.blockExists(xCoord + reactorOffset.x, yCoord + reactorOffset.y, zCoord + reactorOffset.z);
		}
		else
		{
			ForgeDirection face = ForgeDirection.getOrientation(metaInfo & 7);
			return worldObj.blockExists(xCoord + face.offsetX, yCoord + face.offsetY, zCoord + face.offsetZ);
		}
	}

	/**
	 * See code used in getTileAtFrontFace, getBlockIDAtFace, isTargetChunkLoaded.
	 * That method avoids unnecessarily frequent object creation and subsequent garbage collection
	 * @return
	 */
	@Deprecated
	public Coords getLocFrontFace()
	{
		int dir = this.metaInfo & 7;
		return getLocAtFace(dir);
	}

	/**
	 * See code used in getTileAtFrontFace, getBlockIDAtFace, isTargetChunkLoaded.
	 * That method avoids unnecessarily frequent object creation and subsequent garbage collection
	 * @return
	 */
	@Deprecated
	public Coords getLocAtFace(int i)
	{
		/**
		 *      0: -Y (bottom side)
		 *      1: +Y (top side)
		 *      2: -Z (west side)
		 *      3: +Z (east side)
		 *      4: -X (north side)
		 *      5: +x (south side)
		 */
		ForgeDirection side = ForgeDirection.getOrientation(i);
		return new Coords(xCoord + side.offsetX, yCoord + side.offsetY, zCoord + side.offsetZ);
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
		this.contents[i] = itemstack;

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		this.onInventoryChanged();
	}

	public String getInvName()
	{
		return "tile.kaijin.invStocker.name";
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		if (!InventoryStocker.proxy.isClient())
		{
			super.readFromNBT(nbttagcompound);

			// Read extra NBT stuff here
			targetTileName = nbttagcompound.getString("targetTileName");
			remoteNumSlots = nbttagcompound.getInteger("remoteSnapshotSize");
			reactorWorkaround = nbttagcompound.getBoolean("reactorWorkaround");
			reactorWidth = nbttagcompound.getInteger("reactorWidth");

			// Light status and direction
			metaInfo = nbttagcompound.getInteger("Metainfo");
			operationMode = nbttagcompound.getInteger("OpMode");

			boolean extendedChestFlag = nbttagcompound.getBoolean("extendedChestFlag");

			if (Info.isDebugging) System.out.println("ReadNBT: "+targetTileName+" remoteInvSize:"+remoteNumSlots);

			NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
			NBTTagList nbttagremote = nbttagcompound.getTagList("remoteSnapshot");

			contents = new ItemStack[this.getSizeInventory()];
			remoteSnapshot = null;
			if (remoteNumSlots != 0)
			{
				remoteSnapshot = new ItemStack[remoteNumSlots];
			}

			// Our inventory
			for (int i = 0; i < nbttaglist.tagCount(); ++i)
			{
				NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
				int j = nbttagcompound1.getByte("Slot") & 255;

				if (j >= 0 && j < contents.length)
				{
					contents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
				}
			}

			// Remote inventory snapshot
			if (Info.isDebugging) System.out.println("ReadNBT tagRemoteCount: " + nbttagremote.tagCount());
			if (nbttagremote.tagCount() != 0)
			{
				for (int i = 0; i < nbttagremote.tagCount(); ++i)
				{
					NBTTagCompound remoteSnapshot1 = (NBTTagCompound)nbttagremote.tagAt(i);
					int j = remoteSnapshot1.getByte("Slot") & 255;

					if (j >= 0 && j < remoteSnapshot.length)
					{
						remoteSnapshot[j] = ItemStack.loadItemStackFromNBT(remoteSnapshot1);
						if (Info.isDebugging) System.out.println("ReadNBT Remote Slot: " + j + " ItemID: " + remoteSnapshot[j].itemID);
					}
				}
			}

			// Double chest second inventory snapshot
			if (extendedChestFlag)
			{
				extendedChestSnapshot = new ItemStack[remoteNumSlots];
				NBTTagList nbttagextended = nbttagcompound.getTagList("extendedSnapshot");
				if (nbttagextended.tagCount() != 0)
				{
					for (int i = 0; i < nbttagextended.tagCount(); ++i)
					{
						NBTTagCompound extSnapshot1 = (NBTTagCompound)nbttagextended.tagAt(i);
						int j = extSnapshot1.getByte("Slot") & 255;

						if (j >= 0 && j < extendedChestSnapshot.length)
						{
							extendedChestSnapshot[j] = ItemStack.loadItemStackFromNBT(extSnapshot1);
							if (Info.isDebugging) System.out.println("ReadNBT Extended Slot: " + j + " ItemID: " + extendedChestSnapshot[j].itemID);
						}
					}
				}
			}
		}
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		if (!InventoryStocker.proxy.isClient())
		{
			super.writeToNBT(nbttagcompound);

			// Our inventory
			NBTTagList nbttaglist = new NBTTagList();
			for (int i = 0; i < contents.length; ++i)
			{
				if (this.contents[i] != null)
				{
					NBTTagCompound nbttagcompound1 = new NBTTagCompound();
					nbttagcompound1.setByte("Slot", (byte)i);
					contents[i].writeToNBT(nbttagcompound1);
					nbttaglist.appendTag(nbttagcompound1);
				}
			}
			nbttagcompound.setTag("Items", nbttaglist);

			// Remote inventory snapshot
			NBTTagList nbttagremote = new NBTTagList();
			if (remoteSnapshot != null)
			{
				if (Info.isDebugging) System.out.println("writeNBT Target: " + targetTileName + " remoteInvSize:" + remoteSnapshot.length);
				for (int i = 0; i < remoteSnapshot.length; i++)
				{
					if (remoteSnapshot[i] != null)
					{
						if (Info.isDebugging) System.out.println("writeNBT Remote Slot: " + i + " ItemID: " + remoteSnapshot[i].itemID + " StackSize: " + this.remoteSnapshot[i].stackSize + " meta: " + this.remoteSnapshot[i].getItemDamage());
						NBTTagCompound remoteSnapshot1 = new NBTTagCompound();
						remoteSnapshot1.setByte("Slot", (byte)i);
						remoteSnapshot[i].writeToNBT(remoteSnapshot1);
						nbttagremote.appendTag(remoteSnapshot1);
					}
				}
			}
			else
			{
				// if (InventoryStocker.isDebugging) System.out.println("writeNBT Remote Items is NULL!");
			}
			nbttagcompound.setTag("remoteSnapshot", nbttagremote);

			if (extendedChest != null)
			{
				// Double chest second inventory snapshot
				NBTTagList nbttagextended = new NBTTagList();
				for (int i = 0; i < extendedChestSnapshot.length; i++)
				{
					if (extendedChestSnapshot[i] != null)
					{
						if (Info.isDebugging) System.out.println("writeNBT Extended Slot: " + i + " ItemID: " + extendedChestSnapshot[i].itemID + " StackSize: " + this.extendedChestSnapshot[i].stackSize + " meta: " + this.extendedChestSnapshot[i].getItemDamage());
						NBTTagCompound extSnapshot1 = new NBTTagCompound();
						extSnapshot1.setByte("Slot", (byte)i);
						extendedChestSnapshot[i].writeToNBT(extSnapshot1);
						nbttagremote.appendTag(extSnapshot1);
					}
				}
				nbttagcompound.setTag("extendedSnapshot", nbttagextended);
			}

			nbttagcompound.setString("targetTileName", targetTileName);
			nbttagcompound.setInteger("remoteSnapshotSize", remoteNumSlots);
			nbttagcompound.setBoolean("reactorWorkaround", reactorWorkaround);
			nbttagcompound.setInteger("reactorWidth", reactorWidth);
			nbttagcompound.setBoolean("extendedChestFlag", extendedChest != null);

			// Light status and direction
			nbttagcompound.setInteger("Metainfo", metaInfo);
			nbttagcompound.setInteger("OpMode", operationMode);
		}
	}

	/**
	 * This function fires only once on first load of an instance of our tile and attempts to see
	 * if we should have a valid inventory or not. It will set the lastTileEntity and
	 * hasSnapshot state. The actual remoteInventory object will be loaded (or not) via the NBT calls.
	 */
	public void onLoad()
	{
		if (!InventoryStocker.proxy.isClient())
		{
			tileLoaded = true;
			if (Info.isDebugging) System.out.println("onLoad, remote inv size = " + remoteNumSlots);
			TileEntity tile = getTileAtFrontFace();
			if (tile == null)
			{
				if (Info.isDebugging) System.out.println("onLoad tile = null");
				clearSnapshot();
			}
			else
			{
				String tempName = tile.getClass().getName();
				if (tempName.equals(targetTileName) && ((IInventory)tile).getSizeInventory() == remoteNumSlots)
				{
					if (Info.isDebugging) System.out.println("onLoad, target name="+tempName+" stored name="+targetTileName+" MATCHED!");
					lastTileEntity = tile;
					if (tile instanceof TileEntityChest)
					{
						extendedChest = findDoubleChest();
					}
					else
					{
						//TODO Something needs to be checking for target-chunk-not-loaded state in THIS function
						// and make sure that the process isn't considered complete until that state has resolved.
						// Reactor cores could be in an adjacent chunk even when stocker is vertically oriented.
						// When horizontal, any tile entity could potentially be in a not-yet-loaded chunk.
						// Also, update javadoc for this function.
						findReactorCore(tile);
					}
					hasSnapshot = true;
				}
				else
				{
					if (Info.isDebugging) System.out.println("onLoad, target name="+tempName+" stored name="+targetTileName+" NOT matched.");
					clearSnapshot();
				}
			}
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

	public void openChest() {}

	public void closeChest() {}

	/**
	 * 
	 * @param tile
	 * @return boolean<p>
	 * This function will take a snapshot of the IInventory of the TileEntity passed to it.
	 * This will be a copy of the remote inventory as it looks when this function is called.
	 * 
	 * It will check that the TileEntity passed to it actually implements IInventory and
	 * return false doing nothing if it does not.
	 * 
	 * Will return true if it successfully took a snapshot.
	 */
	public boolean takeSnapShot(TileEntity tile)
	{
		if (!(tile instanceof IInventory))
		{
			return false;
		}

		ItemStack tempCopy;

		// Get number of slots in the remote inventory
		TileEntity core = findReactorCore(tile);
		if (core != null)
		{
			// IC2 nuclear reactors with under 6 chambers do not correctly report the size of their inventory - it's always 54 regardless.
			// Instead they internally remap slots in all "nonexistent" columns to the rightmost valid column.
			// Also, because the inventory contents are listed row by row, correcting the size manually is not sufficient.
			// The snapshot and stocking loops must skip over the remapped slots on each row to reach the valid slots in the next row.
			reactorWorkaround = true;
			reactorWidth = countReactorChambers(core) + 3;
			remoteNumSlots = 54;
			remoteSnapshot = new ItemStack[remoteNumSlots];

			// Iterate through remote slots and make a copy of it
			for (int row = 0; row < 6; row++)
			{
				// skip the useless mirrored slots
				for (int i = 0; i < reactorWidth; i++)
				{
					// Reactor inventory rows are always 9 wide internally
					tempCopy = ((IInventory)tile).getStackInSlot(row * 9 + i);
					if (tempCopy == null)
					{
						remoteSnapshot[row * 9 + i] = null;
					}
					else
					{
						remoteSnapshot[row * 9 + i] = new ItemStack(tempCopy.itemID, tempCopy.stackSize, tempCopy.getItemDamage());

						if (tempCopy.stackTagCompound != null)
						{
							remoteSnapshot[row * 9 + i].stackTagCompound = (NBTTagCompound)tempCopy.stackTagCompound.copy();
						}
					} // else
				} // for i
			} // for row
		} // if (core != null)
		else
		{
			remoteNumSlots = ((IInventory)tile).getSizeInventory();
			remoteSnapshot = new ItemStack[remoteNumSlots];

			if (tile instanceof TileEntityChest)
				extendedChest = findDoubleChest();

			// Iterate through remote slots and make a copy of it
			for (int i = 0; i < remoteNumSlots; i++)
			{
				tempCopy = ((IInventory)tile).getStackInSlot(i);

				if (tempCopy == null)
				{
					remoteSnapshot[i] = null;
				}
				else
				{
					remoteSnapshot[i] = new ItemStack(tempCopy.itemID, tempCopy.stackSize, tempCopy.getItemDamage());

					if (tempCopy.stackTagCompound != null)
					{
						remoteSnapshot[i].stackTagCompound = (NBTTagCompound)tempCopy.stackTagCompound.copy();
					}
				}
			}

			if (extendedChest != null)
			{
				// More work to do: Record the other half of the double chest too!
				extendedChestSnapshot = new ItemStack[remoteNumSlots];
				for (int i = 0; i < remoteNumSlots; i++)
				{
					tempCopy = ((IInventory)extendedChest).getStackInSlot(i);
					if (tempCopy == null)
					{
						extendedChestSnapshot[i] = null;
					}
					else
					{
						extendedChestSnapshot[i] = new ItemStack(tempCopy.itemID, tempCopy.stackSize, tempCopy.getItemDamage());

						if (tempCopy.stackTagCompound != null)
						{
							extendedChestSnapshot[i].stackTagCompound = (NBTTagCompound)tempCopy.stackTagCompound.copy();
						}
					}
				}
			} // if extendedChest
		} // else (core == null)

		/*
		 *  get remote entity class name and store it as targetTile, which also ends up being stored in our
		 *  own NBT tables so our tile will remember what was there after chunk unloads/restarts/etc
		 */
		this.targetTileName = tile.getClass().getName();
		lastTileEntity = tile;
		hasSnapshot = true;
		if (Info.isDebugging) System.out.println("Shapshot taken of targetTileName: " + this.targetTileName);
		return true;
	}

	public boolean inputGridIsEmpty()
	{
		for (int i=0; i<9; i++)
		{
			if (contents[i] != null)
			{
				return false;
			}
		}
		return true;
	}

	protected void stockInventory()
	{
		// Verify target is in a loaded chunk before beginning
		if (!isTargetChunkLoaded())
		{
			return;
		}

		int startSlot = 0;
		int endSlot = remoteNumSlots;

		boolean workDone;
		int pass = 0;

		// Check special cases first
		if (reactorWorkaround)
		{
			do {
				workDone = false;
				for (int row = 0; row < 6; row++)
				{
					for (int col = 0; col < reactorWidth; col++)
					{
						int slot = row * 9 + col;
						workDone |= processSlot(slot, (IInventory)lastTileEntity, remoteSnapshot);
					} // for slot
				} // for row
				pass++;
			} while (workDone && pass < 100);
		}
		else if (extendedChest != null)
		{
			do {
				workDone = false;
				for (int slot = startSlot; slot < endSlot; slot++)
				{
					workDone |= processSlot(slot, (IInventory)lastTileEntity, remoteSnapshot);
					workDone |= processSlot(slot, extendedChest, extendedChestSnapshot); // Concurrent second chest processing, for great justice! (and less looping)
				}
				pass++;
			} while (workDone && pass < 100);
		}
		else do
		{
			workDone = false;
			for (int slot = startSlot; slot < endSlot; slot++)
			{
				workDone |= processSlot(slot, (IInventory)lastTileEntity, remoteSnapshot);
			}
			pass++;
		} while (workDone && pass < 100);
	}

	protected boolean processSlot(int slot, IInventory tile, ItemStack[] snapshot)
	{
		ItemStack i = tile.getStackInSlot(slot);
		ItemStack s = snapshot[slot];
		if (i == null)
		{
			if (s == null)
				return false; // Slot is and should be empty. Next!

			// Slot is empty but shouldn't be. Add what belongs there.
			return addItemToRemote(slot, tile, snapshot, snapshot[slot].stackSize);
		}
		else
		{
			// Slot is occupied. Figure out if contents belong there.
			if (s == null)
			{
				// Nope! Slot should be empty. Need to remove this.
				// Call helper function to do that here, and then move on to next slot
				return removeItemFromRemote(slot, tile, tile.getStackInSlot(slot).stackSize);
			}

			// Compare contents of slot between remote inventory and snapshot.
			if (checkItemTypesMatch(i, s))
			{
				// Matched. Compare stack sizes. Try to ensure there's not too much or too little.
				int amtNeeded = snapshot[slot].stackSize - tile.getStackInSlot(slot).stackSize;
				if (amtNeeded > 0)
					return addItemToRemote(slot, tile, snapshot, amtNeeded);
				if (amtNeeded < 0)
					return removeItemFromRemote(slot, tile, -amtNeeded); // Note the negation.
				// The size is already the same and we've nothing to do. Hooray!
				return false;
			}
			else
			{
				// Wrong item type in slot! Try to remove what doesn't belong and add what does.
				boolean ret;
				ret = removeItemFromRemote(slot, tile, tile.getStackInSlot(slot).stackSize);
				if (tile.getStackInSlot(slot) == null)
					ret = addItemToRemote(slot, tile, snapshot, snapshot[slot].stackSize);
				return ret;
			}
		} // else
	}

	// Test if two item stacks' types match, while ignoring damage level if needed.  
	protected boolean checkItemTypesMatch(ItemStack a, ItemStack b)
	{
		// if (InventoryStocker.isDebugging) System.out.println("checkItemTypesMatch: a: "+ a +" b: "+ b +"");
		// if (InventoryStocker.isDebugging) System.out.println("checkItemTypesMatch: .isStackable() a: "+ a.isStackable() +" b: "+ b.isStackable() +"");
		// if (InventoryStocker.isDebugging) System.out.println("checkItemTypesMatch: .getItemDamage() a: "+ a.getItemDamage() +" b: "+ b.getItemDamage() +"");
		// if (InventoryStocker.isDebugging) System.out.println("checkItemTypesMatch: .isItemStackDamageable() a: "+ a.isItemStackDamageable() +" b: "+ b.isItemStackDamageable() +"");

		if (a.itemID == b.itemID)
		{
			//FIXME Make sure to add test for stack tag equality!
			// Ignore damage value of damageable items while testing for match!
			if (a.isItemStackDamageable())
				return true;

			// Already tested ItemID, so a.isItemEqual(b) would be partially redundant.
			if (a.getItemDamage() == b.getItemDamage())
				return true;
		}
		return false;
	}

	protected boolean removeItemFromRemote(int slot, IInventory remote, int amount)
	{
		// Find room in output grid
		// Use checkItemTypesMatch on any existing contents to see if the new output will stack
		// If all existing ItemStacks become full, and there is no room left for a new stack,
		// leave the untransferred remainder in the remote inventory.

		boolean partialMove = false;
		ItemStack remoteStack = remote.getStackInSlot(slot);
		if (remoteStack == null)
			return false;
		int max = remoteStack.getMaxStackSize();
		int amtLeft = amount;
		if (amtLeft > max)
			amtLeft = max;

		int delayedDestination = -1;
		for (int i = 9; i < 18; i++) // Pull only into the Output section
		{
			if (contents[i] == null)
			{
				if (delayedDestination == -1) // Remember this parking space in case we don't find a matching partial slot. 
					delayedDestination = i; // Remember to car-pool, boys and girls!
			}
			else if (checkItemTypesMatch(contents[i], remoteStack))
			{
				int room = max - contents[i].stackSize;
				if (room >= amtLeft)
				{
					// Space for all, so toss it in.
					contents[i].stackSize += amtLeft;
					remoteStack.stackSize -= amtLeft;
					if (remoteStack.stackSize <= 0)
						remote.setInventorySlotContents(slot, null);
					return true;
				}
				else
				{
					// Room for some of it, so add what we can, then keep looking.
					contents[i].stackSize += room;
					remoteStack.stackSize -= room;
					amtLeft -= room;
					partialMove = true;
				}
			}
		}

		if (amtLeft > 0 && delayedDestination >= 0)
		{
			// Not enough room in existing stacks, so transfer whatever's left to a new one.
			contents[delayedDestination] = remoteStack;
			remote.setInventorySlotContents(slot, null);
			return true;
		}
		return partialMove;
	}

	protected boolean addItemToRemote(int slot, IInventory remote, ItemStack[] snapshot, int amount)
	{
		boolean partialMove = false;
		int max = snapshot[slot].getMaxStackSize();
		int amtNeeded = amount;
		if (amtNeeded > max)
			amtNeeded = max;

		for (int i = 17; i >= 0; i--) // Scan Output section as well in case desired items were removed for being in the wrong slot
		{
			if (contents[i] != null && checkItemTypesMatch(contents[i], snapshot[slot]))
			{
				if (remote.getStackInSlot(slot) == null)
				{
					// It's currently empty, so toss what we can into remote slot.
					if (contents[i].stackSize > amtNeeded)
					{
						// Found more than enough to meet the quota, so shift it on over.
						ItemStack extra = contents[i].splitStack(amtNeeded);
						remote.setInventorySlotContents(slot, extra);
						return true;
					}
					else
					{
						amtNeeded -= contents[i].stackSize;
						remote.setInventorySlotContents(slot, contents[i]);
						contents[i] = null;
						if (amtNeeded <= 0)
							return true;
						partialMove = true;
					}
				}
				else
				{
					// There's already some present, so transfer from one stack to the other.
					if (contents[i].stackSize > amtNeeded)
					{
						// More than enough here, just add and subtract.
						contents[i].stackSize -= amtNeeded;
						remote.getStackInSlot(slot).stackSize += amtNeeded;
						return true;
					}
					else
					{
						// This stack matches or is smaller than what we need. Consume it entirely.
						amtNeeded -= contents[i].stackSize;
						remote.getStackInSlot(slot).stackSize += contents[i].stackSize;
						contents[i] = null;
						if (amtNeeded <= 0)
							return true;
						partialMove = true;
					}
				} // else
			} // if
		} // for
		return partialMove;
	}

	private void debugSnapshotDataClient()
	{
		if (InventoryStocker.proxy.isClient())
		{
			TileEntity tile = getTileAtFrontFace();
			if (!(tile instanceof IInventory)) // A null pointer will fail an instanceof test, so there's no need to independently check it.
			{
				return;
			}
			String tempName = tile.getClass().getName();
			System.out.println("Client detected TileName=" + tempName + " expected TileName=" + targetTileName);
		}
	}

	private void debugSnapshotDataServer()
	{
		if (InventoryStocker.proxy.isServer())
		{
			TileEntity tile = getTileAtFrontFace();
			if (!(tile instanceof IInventory)) // A null pointer will fail an instanceof test, so there's no need to independently check it.
			{
				return;
			}
			String tempName = tile.getClass().getName();
			System.out.println("Server detected TileName=" + tempName + " expected TileName=" + targetTileName);
		}
	}

	/**
	 * Will check if our snapshot should be invalidated.
	 * Returns true if snapshot is invalid, false otherwise.
	 * @return boolean
	 */
	public boolean checkInvalidSnapshot()
	{
		/* TODO Add code here to check if the chunk that the tile at front face
		 *      is in is actually loaded or not. Return false immediately if it
		 *      isn't loaded so that other code doesn't clear the snapshot.
		 *      
		 *      Needs testing
		 */
		if (!isTargetChunkLoaded())
		{
			return false;
		}

		TileEntity tile = getTileAtFrontFace();
		if (!(tile instanceof IInventory)) // A null pointer will fail an instanceof test, so there's no need to independently check it.
		{
			if (Info.isDebugging)
			{
				if (tile == null) System.out.println("Invalid snapshot: Tile = null");
				else System.out.println("Invalid snapshot: tileEntity has no IInventory interface");
			}
			return true;
		}

		String tempName = tile.getClass().getName();
		if (!tempName.equals(targetTileName))
		{
			if (Info.isDebugging) System.out.println("Invalid snapshot: TileName Mismatched, detected TileName=" + tempName + " expected TileName=" + targetTileName);
			return true;
		}

		if (tile != lastTileEntity)
		{
			if (Info.isDebugging) System.out.println("Invalid snapshot: tileEntity does not match lastTileEntity");
			return true;
		}

		if (((IInventory)tile).getSizeInventory() != this.remoteNumSlots)
		{
			if (Info.isDebugging)
			{
				System.out.println("Invalid snapshot: tileEntity inventory size has changed");
				System.out.println("RemoteInvSize: " + ((IInventory)tile).getSizeInventory()+", Expecting: "+this.remoteNumSlots);
			}
			return true;
		}

		// Deal with double-chest special case
		if (tile instanceof TileEntityChest)
		{
			// Look for adjacent chest
			TileEntityChest foundChest = findDoubleChest();

			if (Info.isDebugging)
			{
				if (foundChest == null)
					System.out.println("Single Wooden Chest Found");
				else
					System.out.println("Double Wooden Chest Found");
			}

			// Check if it matches previous conditions
			if (extendedChest != foundChest)
			{
				if (Info.isDebugging) System.out.println("Invalid snapshot: Double chest configuration changed!");
				return true;
			}
		}

		// Deal with nuclear reactor special case
		if (reactorWorkaround)
		{
			TileEntity core = verifyReactorCore();
			if (core != null)
			{
				int currentWidth = countReactorChambers(core) + 3;
				if (currentWidth != reactorWidth)
				{
					if (Info.isDebugging) System.out.println("Invalid snapshot: Reactor size has changed!");
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Will find a double chest
	 * @return TileEntityChest
	 */
	private TileEntityChest findDoubleChest()
	{
		TileEntity temp;
		TileEntity front = getTileAtFrontFace();
		if (front == null) return null;

		temp = worldObj.getBlockTileEntity(front.xCoord + 1, front.yCoord, front.zCoord);
		if (temp instanceof TileEntityChest)
			return (TileEntityChest)temp;

		temp = worldObj.getBlockTileEntity(front.xCoord - 1, front.yCoord, front.zCoord);
		if (temp instanceof TileEntityChest)
			return (TileEntityChest)temp;

		temp = worldObj.getBlockTileEntity(front.xCoord, front.yCoord, front.zCoord + 1);
		if (temp instanceof TileEntityChest)
			return (TileEntityChest)temp;

		temp = worldObj.getBlockTileEntity(front.xCoord, front.yCoord, front.zCoord - 1);
		if (temp instanceof TileEntityChest)
			return (TileEntityChest)temp;

		return null;
	}

	private TileEntity findReactorCore(TileEntity start)
	{
		reactorOffset.set(0, 0, 0);
		TileEntity temp;

		if (start == null)
			return null;

		if (start.getClass().getSimpleName().endsWith(classnameIC2ReactorCore))
		{
			reactorOffset.set(start.xCoord - this.xCoord, start.yCoord - this.yCoord, start.zCoord - this.zCoord);
			return start;
		}

		if (start.getClass().getSimpleName().endsWith(classnameIC2ReactorChamber))
		{
			for (int i = 0; i < 6; i++)
			{
				ForgeDirection dir = ForgeDirection.getOrientation(i);
				int x = start.xCoord + dir.offsetX;
				int y = start.yCoord + dir.offsetY;
				int z = start.zCoord + dir.offsetZ;
				temp = worldObj.getBlockTileEntity(x, y, z);
				if (temp != null && temp.getClass().getSimpleName().endsWith(classnameIC2ReactorCore))
				{
					reactorOffset.set(x - this.xCoord, y - this.yCoord, z - this.zCoord);
					return temp;
				}
			}
		}

		// If it's not a core and it's not a chamber we're done here.
		return null;
	}

	private TileEntity verifyReactorCore()
	{
		TileEntity temp = worldObj.getBlockTileEntity(xCoord + reactorOffset.x, yCoord + reactorOffset.y, zCoord + reactorOffset.z);
		if (temp != null && temp.getClass().getSimpleName().endsWith(classnameIC2ReactorCore))
		{
			return temp;
		}
		
		return null;
	}

	private int addIfChamber(int x, int y, int z)
	{
		TileEntity temp = worldObj.getBlockTileEntity(x, y, z);
		if (temp != null)
			return temp.getClass().getSimpleName().endsWith(classnameIC2ReactorChamber) ? 1 : 0;
		return 0;
	}

	private int countReactorChambers(TileEntity core)
	{
		int count = 0;
		if (core == null) return 0;

		count += addIfChamber(core.xCoord + 1, core.yCoord, core.zCoord);
		count += addIfChamber(core.xCoord - 1, core.yCoord, core.zCoord);
		count += addIfChamber(core.xCoord, core.yCoord, core.zCoord + 1);
		count += addIfChamber(core.xCoord, core.yCoord, core.zCoord - 1);
		count += addIfChamber(core.xCoord, core.yCoord + 1, core.zCoord);
		count += addIfChamber(core.xCoord, core.yCoord - 1, core.zCoord);

		return count;
	}

	private void lightsOff()
	{
		lightState = false;
		int lights = this.metaInfo & 8; // Grab current state of lights
		this.metaInfo ^= lights; // Toggles lights off if they're on (this two step method avoids worrying about retaining an unknown number of other bits)
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}

	private void lightsOn()
	{
		lightState = true;
		// Turn on das blinkenlights!
		this.metaInfo |= 8; // Turn lights on
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void updateEntity() //TODO Marked for easy access
	{
		// Doing the adjacent chunk loaded check here. If it isn't loaded, return from the tick
		// without doing anything
		if (!isTargetChunkLoaded())
		{
			return;
		}

		//debugSnapshotDataClient();
		//debugSnapshotDataServer();

		// Check if this or one of the blocks next to this is getting power from a neighboring block.
		boolean isPowered = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);

		// This code is probably not needed, but maintaining it here just in case
		// The above comment is probably no longer needed, but was left here despite the referenced code apparently being MIA

		if (InventoryStocker.proxy.isClient())
		{
			//if (isPowered)
			//{
				// This allows client-side animation of texture over time, which would not happen without updating the block
				//TODO Removing animation for now, unless a way to do it without spamming renderer updates can be devised
				//worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
			//}
			return;
		}

		// See if this tileEntity instance has been properly loaded, if not, do some onLoad stuff to initialize or restore prior state
		if (!tileLoaded)
		{
			if (Info.isDebugging) System.out.println("tileLoaded false, running onLoad");
			onLoad();
		}

		// Handle updating door states if needed
		if (updateDelay > 0)
		{
			updateDelay--;
			if (updateDelay == 0) updateDoorStates();
		}

		// Check if a snapshot has been requested
		if (guiTakeSnapshot)
		{
			guiTakeSnapshot = false;
			if (Info.isDebugging) System.out.println("GUI take snapshot request");
			TileEntity tile = getTileAtFrontFace();
			if (tile != null && tile instanceof IInventory)
			{
				if (takeSnapShot(tile))
				{
					//sendSnapshotStateClients();
				}
				else
				{
					// Failed to get a valid snapshot. Run this just in case to cleanly reset everything.
					clearSnapshot();
					if (Info.isDebugging) System.out.println("updateEntity.guiTakeSnapshot_failed.clearSnapshot()");
				}
			}
		}

		// Check if a snapshot clear has been requested
		if (guiClearSnapshot)
		{
			guiClearSnapshot = false;
			if (Info.isDebugging) System.out.println("updateEntity.guiClearSnapshot.clearSnapshot()");
			clearSnapshot();
		}

		if (!isPowered)
		{
			// Reset tick time on losing power
			tickTime = 0;

			// Shut off glowing light textures.
			if (lightState) lightsOff();
		}

		// If we are powered and previously weren't or timer has expired, it's time to go to work.
		if (isPowered && tickTime == 0)
		{
			tickTime = tickDelay;
			if (Info.isDebugging) System.out.println("Powered");

			// Turn on das blinkenlights!
			if (!lightState) lightsOn();

			if (hasSnapshot)
			{
				// Check for any situation in which the snapshot should be invalidated.
				if (checkInvalidSnapshot())
				{
					if (Info.isDebugging) System.out.println("updateEntity.checkInvalidSnapshot.clearSnapshot()");
					clearSnapshot();
				}
				else
				{
					// If we've made it here, it's time to stock the remote inventory.
					if (Info.isDebugging) System.out.println("updateEntity.stockInventory()");
					stockInventory();
				}
			}
		}
		else if (tickTime > 0)
		{
			tickTime--;
		}
	}

	public void rotateBlock()
	{
		int dir = metaInfo & 7; // Get orientation from first 3 bits of meta data
		metaInfo ^= dir; // Clear those bits
		++dir; // Rotate
		if (dir > 5) dir = 0; // Start over
		metaInfo |= dir; // Write orientation back to meta data value
		worldObj.markBlockNeedsUpdate(xCoord, yCoord, zCoord);
	}

	// Start networking section

	public void receiveSnapshotRequest()
	{
		if (hasSnapshot)
		{
			if (Info.isDebugging) System.out.println("GUI: clear snapshot request");
			guiClearSnapshot = true;
		}
		else
		{
			if (Info.isDebugging) System.out.println("GUI: take snapshot request");
			guiTakeSnapshot = true;
		}
	}

	public void receiveModeRequest() //TODO
	{
		operationMode++;
		if (operationMode > 2) operationMode = 0;
		if (Info.isDebugging) System.out.println("Operation Mode: " + operationMode);
	}

	/**<pre>
	 * This function returns a Packet250CustomPayload:
	 *   byte 1: x location of TileEntity
	 *   byte 2: y location of TileEntity
	 *   byte 3: z location of TileEntity
	 *   byte 4: int "metadata", with rotation and light state info as bits</pre>

	 * @return Packet250CustomPayload
	 */
	@Override
	public Packet250CustomPayload getDescriptionPacket()
	{
		if (Info.isDebugging) System.out.println("te.getAuxillaryInfoPacket()");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(0);
			data.writeInt(this.xCoord);
			data.writeInt(this.yCoord);
			data.writeInt(this.zCoord);
			data.writeInt(this.metaInfo);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = Info.PACKET_CHANNEL; // CHANNEL MAX 16 CHARS
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;

		return packet;
	}

	/**
	 * Sends a button click to the server.
	 * @param button - 0 = snapshot, 1 = mode
	 */
	public void sendButtonCommand(int id)
	{
		if (Info.isDebugging) System.out.println("sendButtonCommand");
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		try
		{
			data.writeInt(0);
			data.writeInt(this.xCoord);
			data.writeInt(this.yCoord);
			data.writeInt(this.zCoord);
			data.writeInt(id);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = Info.PACKET_CHANNEL; // CHANNEL MAX 16 CHARS
		packet.data = bytes.toByteArray();
		packet.length = packet.data.length;

		InventoryStocker.proxy.sendPacketToServer(packet);
	}

	// End networking section
}
