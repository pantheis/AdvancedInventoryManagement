package com.kaijin.InventoryStocker;

import forge.IGuiHandler;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;

public class GuiHandlerInventoryStocker implements IGuiHandler
{
    public Object getGuiElement(int var1, EntityHuman var2, World var3, int var4, int var5, int var6)
    {
        if (!var3.isLoaded(var4, var5, var6))
        {
            return null;
        }
        else
        {
            TileEntity var7 = var3.getTileEntity(var4, var5, var6);
            return !(var7 instanceof TileEntityInventoryStocker) ? null : new ContainerInventoryStocker(var2.inventory, (TileEntityInventoryStocker)var7, var2);
        }
    }
}
