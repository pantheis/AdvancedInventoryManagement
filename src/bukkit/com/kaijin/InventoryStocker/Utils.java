package com.kaijin.InventoryStocker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils
{
    public static boolean isDebug()
    {
        return false;
    }

    public static String hashSHA1(String var0)
    {
        MessageDigest var1 = null;

        try
        {
            var1 = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException var7)
        {
            var7.printStackTrace();
        }

        var1.update(var0.getBytes());
        byte[] var2 = var1.digest();
        StringBuffer var3 = new StringBuffer();

        for (int var4 = 0; var4 < var2.length; ++var4)
        {
            var3.append(Integer.toString((var2[var4] & 255) + 256, 16).substring(1));
        }

        StringBuffer var8 = new StringBuffer();

        for (int var5 = 0; var5 < var2.length; ++var5)
        {
            String var6 = Integer.toHexString(255 & var2[var5]);

            if (var6.length() == 1)
            {
                var8.append('0');
            }

            var8.append(var6);
        }

        return var8.toString();
    }

    public static int lookupRotatedSide(int var0, int var1)
    {
        int[][] var2 = new int[][] {{0, 1, 2, 2, 2, 2}, {1, 0, 3, 3, 3, 3}, {2, 3, 0, 1, 5, 4}, {3, 2, 1, 0, 4, 5}, {5, 5, 5, 4, 0, 1}, {4, 4, 4, 5, 1, 0}};
        return var2[var0][var1];
    }
}
