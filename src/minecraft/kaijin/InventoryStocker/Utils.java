package kaijin.InventoryStocker;

import net.minecraft.src.*;

public class Utils {

    public static boolean isClient(World world)
    {
        return world instanceof WorldClient;
    }

	public static void dropItems (World world, ItemStack stack, int i, int j, int k) {
		float f1 = 0.7F;
		double d = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d1 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		double d2 = (double)(world.rand.nextFloat() * f1) + (double)(1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, (double) i + d,
				(double) j + d1, (double) k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(entityitem);
	}

    public static void dropItems (World world, IInventory inventory, int i, int j, int k) {
		for (int l = 0; l < inventory.getSizeInventory(); ++l) {
			ItemStack items = inventory.getStackInSlot(l);
			
			if (items != null && items.stackSize > 0) {
				dropItems (world, inventory.getStackInSlot(l).copy(), i, j, k);
			}
    	}
	}

    public static void preDestroyBlock (World world, int i, int j, int k) {
		TileEntity tile = world.getBlockTileEntity(i, j, k);
		
		if (tile instanceof IInventory && !isClient(world)) {
			dropItems(world, (IInventory) tile, i, j, k);
		}
	}

	
}
