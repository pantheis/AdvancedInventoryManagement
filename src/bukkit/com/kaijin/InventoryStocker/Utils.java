package com.kaijin.InventoryStocker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import net.minecraft.server.EntityItem;
import net.minecraft.server.IInventory;
import net.minecraft.server.ItemStack;
import net.minecraft.server.TileEntity;
import net.minecraft.server.World;

public class Utils
{
    public byte[] hashSHA1(String var1)
    {
        MessageDigest var2 = null;

        try
        {
            var2 = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException var8)
        {
            var8.printStackTrace();
        }

        var2.update(var1.getBytes());
        byte[] var3 = var2.digest();
        StringBuffer var4 = new StringBuffer();

        for (int var5 = 0; var5 < var3.length; ++var5)
        {
            var4.append(Integer.toString((var3[var5] & 255) + 256, 16).substring(1));
        }

        StringBuffer var9 = new StringBuffer();

        for (int var6 = 0; var6 < var3.length; ++var6)
        {
            String var7 = Integer.toHexString(255 & var3[var6]);

            if (var7.length() == 1)
            {
                var9.append('0');
            }

            var9.append(var7);
        }

        return var3;
    }

    public static void dropItems(World var0, ItemStack var1, int var2, int var3, int var4)
    {
        float var5 = 0.7F;
        double var6 = (double)(var0.random.nextFloat() * var5) + (double)(1.0F - var5) * 0.5D;
        double var8 = (double)(var0.random.nextFloat() * var5) + (double)(1.0F - var5) * 0.5D;
        double var10 = (double)(var0.random.nextFloat() * var5) + (double)(1.0F - var5) * 0.5D;
        EntityItem var12 = new EntityItem(var0, (double)var2 + var6, (double)var3 + var8, (double)var4 + var10, var1);
        var12.pickupDelay = 10;
        var0.addEntity(var12);
    }

    public static void dropItems(World var0, IInventory var1, int var2, int var3, int var4)
    {
        for (int var5 = 0; var5 < var1.getSize(); ++var5)
        {
            ItemStack var6 = var1.getItem(var5);

            if (var6 != null && var6.count > 0)
            {
                dropItems(var0, var1.getItem(var5).cloneItemStack(), var2, var3, var4);
            }
        }
    }

    public static void preDestroyBlock(World var0, int var1, int var2, int var3)
    {
        TileEntity var4 = var0.getTileEntity(var1, var2, var3);

        if (var4 instanceof IInventory && !CommonProxy.isClient(var0))
        {
            dropItems(var0, (IInventory)var4, var1, var2, var3);
        }
    }

    public static int lookupRotatedSide(int var0, int var1)
    {
        int[][] var2 = new int[][] {{0, 1, 2, 2, 2, 2}, {1, 0, 3, 3, 3, 3}, {2, 3, 0, 1, 5, 4}, {3, 2, 1, 0, 4, 5}, {5, 5, 5, 4, 0, 1}, {4, 4, 4, 5, 1, 0}};
        return var2[var0][var1];
    }
}
