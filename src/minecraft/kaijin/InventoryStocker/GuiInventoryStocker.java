package kaijin.InventoryStocker;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class GuiInventoryStocker extends GuiContainer {
    private int inventoryRows = 0;
    private IInventory lowerChestInventory;

	public GuiInventoryStocker(IInventory Container1) {
		super(new ContainerInventoryStocker(Container1));
        this.lowerChestInventory = Container1;
        this.allowUserInput = false;
        short var3 = 222;
        int var4 = var3 - 108;
        this.inventoryRows = Container1.getSizeInventory() / 9;

		// TODO Auto-generated constructor stub
	}
	
    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     */
    protected void drawGuiContainerForegroundLayer()
    {
        this.fontRenderer.drawString(StatCollector.translateToLocal(this.lowerChestInventory.getInvName()), 8, 6, 4210752);
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
        int var4 = this.mc.renderEngine.getTexture("/gui/container.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(var4);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(var5, var6 + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);
    }

}
