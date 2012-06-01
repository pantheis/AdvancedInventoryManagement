package com.kaijin.InventoryStocker;

import com.kaijin.InventoryStocker.*;

import net.minecraft.src.*;
import net.minecraft.src.forge.*;

public class GuiHandlerInventoryStocker implements IGuiHandler
{
    @Override
    public Object getGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        if (!world.blockExists(x, y, z))
        {
            return null;
        }

        TileEntity tile = world.getBlockTileEntity(x, y, z);

        if (!(tile instanceof TileEntityInventoryStocker))
        {
            return null;
        }

        return new GuiInventoryStocker(player.inventory, (TileEntityInventoryStocker)tile, player);
    }
}
