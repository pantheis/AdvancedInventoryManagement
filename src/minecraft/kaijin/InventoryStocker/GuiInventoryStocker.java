package kaijin.InventoryStocker;

import org.lwjgl.opengl.GL11;
import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class GuiInventoryStocker extends GuiContainer {
    private int inventoryRows = 0;
    private IInventory upperChestInventory;
    private IInventory lowerChestInventory;

	public GuiInventoryStocker(IInventory Container1, IInventory Container2) {
		super(new ContainerChest(Container1, Container2));
        this.upperChestInventory = Container1;
        this.lowerChestInventory = Container2;
        this.allowUserInput = false;
        short var3 = 222;
        int var4 = var3 - 108;
        this.inventoryRows = Container2.getSizeInventory() / 9;
        this.ySize = var4 + this.inventoryRows * 18;

		// TODO Auto-generated constructor stub
	}
	
    @Override
	protected void drawGuiContainerForegroundLayer() {        
        this.fontRenderer.drawString(StatCollector.translateToLocal(this.lowerChestInventory.getInvName()), 8, 6, 4210752);
        this.fontRenderer.drawString(StatCollector.translateToLocal(this.upperChestInventory.getInvName()), 8, this.ySize - 96 + 2, 4210752);
    }

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2,
			int var3) {
        int var4 = this.mc.renderEngine.getTexture("/gui/container.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(var4);
        int var5 = (this.width - this.xSize) / 2;
        int var6 = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(var5, var6, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(var5, var6 + this.inventoryRows * 18 + 17, 0, 126, this.xSize, 96);

		// TODO Auto-generated method stub
		
	}

}
