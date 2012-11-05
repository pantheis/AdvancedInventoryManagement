/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.InventoryPlayer;
import net.minecraft.src.StringTranslate;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiInventoryStocker extends GuiContainer
{
	private TileEntityInventoryStocker tile;

	// define button class wide
	private GuiButton buttonSnap = null;

	protected static StringTranslate lang = StringTranslate.getInstance();

	public GuiInventoryStocker(InventoryPlayer playerinventory, TileEntityInventoryStocker tileentityinventorystocker)
	{
		super(new ContainerInventoryStocker(playerinventory, tileentityinventorystocker));
		tile = tileentityinventorystocker;
		xSize = 176;
		ySize = 168;
		buttonSnap = new GuiButton(0, 0, 0, 40, 20, "");
	}

	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
	{
		int GuiTex = mc.renderEngine.getTexture(InventoryStocker.proxy.BLOCK_PNG);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiTex);

		// Upper left corner of GUI panel
		int xLoc = (width - xSize) / 2; // Half the difference between screen width and GUI width
		int yLoc = (height - ySize) / 2; // Half the difference between screen height and GUI height

		this.drawTexturedModalRect(xLoc, yLoc, 0, 0, xSize, ySize);

		fontRenderer.drawString(lang.translateKey("kaijin.invStocker.guiStrings.input"), xLoc + 8, yLoc + 6, 4210752);
		fontRenderer.drawString(lang.translateKey(tile.getInvName()), xLoc + 68, yLoc + 6, 4210752);
		fontRenderer.drawString(lang.translateKey("kaijin.invStocker.guiStrings.output"), xLoc + 116, yLoc + 6, 4210752);
		fontRenderer.drawString(lang.translateKey("container.inventory"), xLoc + 8, yLoc + 74, 4210752);

		//Add snapshot text
		if (tile.serverSnapshotState())
		{
			fontRenderer.drawString(lang.translateKey("kaijin.invStocker.guiStrings.ready"), xLoc + 73, yLoc + 20, 0x0000FF);
		}
		else
		{
			fontRenderer.drawString(lang.translateKey("kaijin.invStocker.guiStrings.notready"), xLoc + 63, yLoc + 20, 0xFF0000);
		}

		//GuiButton(int ID, int XOffset, int YOffset, int Width, int Height, string Text)
		//button definition is the full one with width and height
		//defining button below, setting it look enabled and drawing it
		//If you make changes to the button state, you must call .drawButton(mc, XOffset, YOffset)
		buttonSnap.xPosition = (width / 2) - 20;
		buttonSnap.yPosition = yLoc + 43;
		buttonSnap.displayString = tile.serverSnapshotState() ? "Clear" : "Scan";
		buttonSnap.drawButton(mc, mouseX, mouseY);
	}

	//Copied mouseClicked function to get our button to make the "click" noise when clicked
	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (par3 == 0)
		{
			if (buttonSnap.mousePressed(this.mc, par1, par2))
			{
				mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
				this.actionPerformed(buttonSnap);
			}
		}
		super.mouseClicked(par1, par2, par3);
	}

	/*
	 * This function actually handles what happens when you click on a button, by ID
	 */
	@Override
	public void actionPerformed(GuiButton button)
	{
		if (!button.enabled)
		{
			return;
		}
		if (button.id == 0)
		{
			if (tile.serverSnapshotState())
			{
				if (Utils.isDebug()) System.out.println("Button Pressed, clearing snapshot");
				tile.guiClearSnapshot();
			}
			else
			{
				if (Utils.isDebug()) System.out.println("Button Pressed, taking snapshot");
				tile.guiTakeSnapshot();
			}
		}

	}
}
