package com.kaijin.InventoryStocker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils
{
    public String hashSHA1(String var1)
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

        return var9.toString();
    }

    public static int lookupRotatedSide(int var0, int var1)
    {
        int[][] var2 = new int[][] {{0, 1, 2, 2, 2, 2}, {1, 0, 3, 3, 3, 3}, {2, 3, 0, 1, 5, 4}, {3, 2, 1, 0, 4, 5}, {5, 5, 5, 4, 0, 1}, {4, 4, 4, 5, 1, 0}};
        return var2[var0][var1];
    }
}
