/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import java.io.File;
import java.util.*;
import com.kaijin.InventoryStocker.*;
import net.minecraftforge.common.*;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.network.*;

@Mod(modid = "InventoryStocker", name="Inventory Stocker", version="beta 1 for Minecraft 1.3.2")
@NetworkMod(channels = { "InventoryStocker" }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class mod_InventoryStocker
{
    static Configuration configuration = CommonProxy.getConfiguration();
    static int InventoryStockerBlockID;
    static public boolean isDebugging;
    static { configurationProperties(); }
    
    
    public static final Block InventoryStocker = new BlockInventoryStocker(InventoryStockerBlockID, 0).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("inventoryStocker");

    public static mod_InventoryStocker instance;

    public mod_InventoryStocker()
    {
        instance = this;
    }

    public void load()
    {
        MinecraftForge.versionDetect("Inventory Stocker", 3, 1, 2);
        MinecraftForge.registerConnectionHandler(new ConnectionHandler());
        ModLoader.registerBlock(InventoryStocker);
        ModLoader.registerTileEntity(TileEntityInventoryStocker.class, "InventoryStocker");
        if (Utils.isDebug())
        {
            ModLoader.addRecipe(new ItemStack(InventoryStocker, 16), new Object[] {"XX", "XX", 'X', Block.dirt}); // Testing Recipe
        }
        ModLoader.addRecipe(new ItemStack(InventoryStocker, 1), new Object[] {"RIR", "PCP", "RIR", 'C', Block.chest, 'I', Item.ingotIron, 'P', Block.pistonBase, 'R', Item.redstone});
        MinecraftForge.setGuiHandler(this.instance, new GuiHandlerInventoryStocker());
        CommonProxy.load();
        if (CommonProxy.isServer())
        {
            ModLoader.getLogger().info ("InventoryStocker " + getVersion() + " loaded.");
        }
        if (isDebugging)
        {
            ModLoader.getLogger().info("InventoryStocker debugging enabled.");
        }
    }

    public static void configurationProperties()
    {
        configuration.load();
        InventoryStockerBlockID = Integer.parseInt(configuration.getOrCreateBlockIdProperty("InventoryStocker", 249).value);
        isDebugging = Boolean.parseBoolean((configuration.getOrCreateBooleanProperty("debug", configuration.CATEGORY_GENERAL, false).value));
        configuration.save();
    }

    @Override
    public String getVersion()
    {
        return "beta 45 for Minecraft 1.2.5";
    }

    @Override
    public boolean clientSideRequired()
    {
        return true;
    }

    @Override
    public boolean serverSideRequired()
    {
        return false;
    }
}
