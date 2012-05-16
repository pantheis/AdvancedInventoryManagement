package kaijin.InventoryStocker;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.Container;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ICrafting;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;
import net.minecraft.server.Slot;

public class ContainerInventoryStocker extends Container
{
    private IInventory playerinventory;
    private TileEntityInventoryStocker inventorystockerinventory;
    private List guiPlayerList = new ArrayList();

    public ContainerInventoryStocker(IInventory var1, TileEntityInventoryStocker var2)
    {
        this.playerinventory = var1;
        this.inventorystockerinventory = var2;
        int var3;
        int var4;

        for (var4 = 0; var4 < 3; ++var4)
        {
            for (var3 = 0; var3 < 3; ++var3)
            {
                this.a(new Slot(var2, var3 + 3 * var4, 8 + var3 * 18, 18 + var4 * 18));
            }
        }

        for (var4 = 0; var4 < 3; ++var4)
        {
            for (var3 = 0; var3 < 3; ++var3)
            {
                this.a(new Slot(var2, 9 + var3 + 3 * var4, 116 + var3 * 18, 18 + var4 * 18));
            }
        }

        for (var4 = 0; var4 < 3; ++var4)
        {
            for (var3 = 0; var3 < 9; ++var3)
            {
                this.a(new Slot(var1, var3 + var4 * 9 + 9, 8 + var3 * 18, 86 + var4 * 18));
            }
        }

        for (var3 = 0; var3 < 9; ++var3)
        {
            this.a(new Slot(var1, var3, 8 + var3 * 18, 144));
        }
    }

    public boolean b(EntityHuman var1)
    {
        return this.inventorystockerinventory.a(var1);
    }

    /**
     * Called to transfer a stack from one inventory to the other eg. when shift clicking.
     */
    public ItemStack a(int var1)
    {
        ItemStack var2 = null;
        Slot var3 = (Slot)this.e.get(var1);

        if (var3 != null && var3.c())
        {
            ItemStack var4 = var3.getItem();
            var2 = var4.cloneItemStack();

            if (var1 < 18)
            {
                if (!this.a(var4, 18, this.e.size(), true))
                {
                    return null;
                }
            }
            else if (!this.a(var4, 0, 18, false))
            {
                return null;
            }

            if (var4.count == 0)
            {
                var3.set((ItemStack)null);
            }
            else
            {
                var3.d();
            }
        }

        return var2;
    }

    public void addSlotListener(ICrafting var1)
    {
        super.addSlotListener(var1);
        this.guiPlayerList.add(((EntityPlayer)var1).name);
        this.inventorystockerinventory.sendSnapshotStateClient(((EntityPlayer)var1).name);
        this.inventorystockerinventory.entityOpenList(this.guiPlayerList);
    }

    /**
     * Callback for when the crafting gui is closed.
     */
    public void a(EntityHuman var1)
    {
        super.a(var1);

        if (this.guiPlayerList.contains(var1.name))
        {
            this.guiPlayerList.remove(var1.name);
            this.inventorystockerinventory.entityOpenList(this.guiPlayerList);
        }
    }
}
