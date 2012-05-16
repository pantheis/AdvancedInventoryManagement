package kaijin.InventoryStocker;

import forge.ITextureProvider;
import java.util.ArrayList;
import net.minecraft.server.BlockContainer;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.IBlockAccess;
import net.minecraft.server.ItemStack;
import net.minecraft.server.Material;
import net.minecraft.server.MathHelper;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;
import net.minecraft.server.mod_InventoryStocker;

public class BlockInventoryStocker extends BlockContainer implements ITextureProvider
{
    public BlockInventoryStocker(int var1, int var2)
    {
        super(var1, var2, Material.EARTH);
    }

    public void addCreativeItems(ArrayList var1)
    {
        var1.add(new ItemStack(this));
    }

    public String getTextureFile()
    {
        return "/kaijin/InventoryStocker/terrain.png";
    }

    /**
     * Returns the block texture based on the side being looked at.  Args: side
     */
    public int a(int var1)
    {
        switch (var1)
        {
            case 0:
                return 16;

            case 1:
                return 0;

            case 2:
                return 16;

            case 3:
                return 16;

            default:
                return 16;
        }
    }

    public int getBlockTexture(IBlockAccess var1, int var2, int var3, int var4, int var5)
    {
        int var6 = var1.getData(var2, var3, var4);
        int var7 = var6 & 7;
        int var8 = Utils.lookupRotatedSide(var5, var7);
        int var9 = (var6 & 8) >> 3;
        TileEntity var10 = var1.getTileEntity(var2, var3, var4);
        int var11;

        if (var8 == 0)
        {
            var11 = (int)var10.world.getTime();
            return 2 + var9 * ((var11 >> 2 & 3) + 1);
        }
        else
        {
            var11 = var10 instanceof TileEntityInventoryStocker ? (((TileEntityInventoryStocker)var10).doorOpenOnSide(var5) ? 2 : 0) : 0;
            return var8 == 1 ? 32 + var9 + var11 : 16 + var9 + var11;
        }
    }

    private int determineOrientation(World var1, int var2, int var3, int var4, EntityHuman var5)
    {
        if ((double)var5.pitch > 45.0D)
        {
            return 0;
        }
        else if ((double)var5.pitch < -45.0D)
        {
            return 1;
        }
        else
        {
            int var6 = MathHelper.floor((double)(var5.yaw * 4.0F / 360.0F) + 0.5D) & 3;
            return var6 == 0 ? 3 : (var6 == 1 ? 4 : (var6 == 2 ? 2 : (var6 == 3 ? 5 : 0)));
        }
    }

    /**
     * Called when the block is placed in the world.
     */
    public void postPlace(World var1, int var2, int var3, int var4, EntityLiving var5)
    {
        int var6 = this.determineOrientation(var1, var2, var3, var4, (EntityHuman)var5);
        var1.setData(var2, var3, var4, var6);
    }

    /**
     * Called upon block activation (left or right click on the block.). The three integers represent x,y,z of the
     * block.
     */
    public boolean interact(World var1, int var2, int var3, int var4, EntityHuman var5)
    {
        if (var5.isSneaking())
        {
            if (var5.U() == null)
            {
                int var6 = var1.getData(var2, var3, var4);
                int var7 = var6 & 7;
                var6 ^= var7;
                ++var7;

                if (var7 > 5)
                {
                    var7 = 0;
                }

                var6 |= var7;
                var1.setData(var2, var3, var4, var6);
                var1.notify(var2, var3, var4);
            }

            return false;
        }
        else
        {
            if (!CommonProxy.isClient(var1))
            {
                var5.openGui(mod_InventoryStocker.instance, 1, var1, var2, var3, var4);
            }

            return true;
        }
    }

    /**
     * Returns the TileEntity used by this block.
     */
    public TileEntity a_()
    {
        return new TileEntityInventoryStocker();
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean isPowerSource()
    {
        return true;
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor blockID
     */
    public void doPhysics(World var1, int var2, int var3, int var4, int var5)
    {
        super.doPhysics(var1, var2, var3, var4, var5);
        TileEntityInventoryStocker var6 = (TileEntityInventoryStocker)var1.getTileEntity(var2, var3, var4);

        if (var6 != null)
        {
            var6.onUpdate();
        }
    }

    /**
     * Called when a block is placed using an item. Used often for taking the facing and figuring out how to position
     * the item. Args: x, y, z, facing
     */
    public void postPlace(World var1, int var2, int var3, int var4, int var5) {}

    /**
     * Called whenever the block is removed.
     */
    public void remove(World var1, int var2, int var3, int var4)
    {
        Utils.preDestroyBlock(var1, var2, var3, var4);
        super.remove(var1, var2, var3, var4);
    }
}
