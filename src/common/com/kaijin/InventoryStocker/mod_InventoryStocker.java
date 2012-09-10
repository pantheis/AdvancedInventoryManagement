/* Inventory Stocker
*  Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
*  Licensed as open source with restrictions. Please see attached LICENSE.txt.
*/

package com.kaijin.InventoryStocker;

import java.io.File;
import java.util.*;
import com.kaijin.InventoryStocker.*;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraftforge.common.*;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.network.*;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "InventoryStocker", name="Inventory Stocker", version="beta 1 for Minecraft 1.3.2")
@NetworkMod(channels = { "InventoryStocker" }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class, connectionHandler = ConnectionHandler.class)
public class mod_InventoryStocker
{
	@SidedProxy(clientSide = "com.kaijin.InventoryStocker.ClientProxy", serverSide = "com.kaijin.InventoryStocker.CommonProxy")
	public static CommonProxy proxy; //This object will be populated with the class that you choose for the environment
	@Instance
	public static mod_InventoryStocker instance; //The instance of the mod that will be defined, populated, and callable
    
	static Configuration configuration = proxy.getConfiguration();
    static int InventoryStockerBlockID;
    static public boolean isDebugging;
    static { configurationProperties(); }
    
    
    public static final Block InventoryStocker = new BlockInventoryStocker(InventoryStockerBlockID, 0).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("inventoryStocker");

    @Init
    public void load()
    {
        GameRegistry.registerBlock(InventoryStocker);
        GameRegistry.registerTileEntity(TileEntityInventoryStocker.class, "InventoryStocker");
        if (Utils.isDebug())
        {
            GameRegistry.addRecipe(new ItemStack(InventoryStocker, 16), new Object[] {"XX", "XX", 'X', Block.dirt}); // Testing Recipe
        }
        GameRegistry.addRecipe(new ItemStack(InventoryStocker, 1), new Object[] {"RIR", "PCP", "RIR", 'C', Block.chest, 'I', Item.ingotIron, 'P', Block.pistonBase, 'R', Item.redstone});
        NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
        ClientProxy.load();
        if (ClientProxy.isServer())
        {
            ModLoader.getLogger().info ("InventoryStocker " +  + " loaded.");
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
}
