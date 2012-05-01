package kaijin.InventoryStocker;

import net.minecraft.src.forge.*;
import java.util.*;
import net.minecraft.src.*;
import net.minecraft.src.mod_InventoryStocker.*;
import kaijin.InventoryStocker.*;

public class BlockInventoryStocker extends BlockContainer implements ITextureProvider
{
    public BlockInventoryStocker(int i, int j)
    {
        super(i, j, Material.ground);
    }

    public void addCreativeItems(ArrayList itemList)
    {
        itemList.add(new ItemStack(this));
    }

    public String getTextureFile()
    {
        return "/Kaijin/StockerBlock/terrain.png";
    }

    public static boolean isClient(World world)
    {
        return world instanceof WorldClient;
    }

    @Override
    public boolean blockActivated(World world, int x, int y, int z, EntityPlayer entityplayer)
    {
//        	entityplayer.displayGUIChest((IInventory)tileentityinventorystocker);
        if (!isClient(world))
        {
            entityplayer.openGui(mod_InventoryStocker.instance, 1, world, x, y, z);
        }

        return true;
    }

    @Override
    public TileEntity getBlockEntity()
    {
        return new TileEntityInventoryStocker();
    }
}
