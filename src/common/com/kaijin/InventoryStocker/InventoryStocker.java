/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.InventoryStocker;

import net.minecraft.src.Block;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "InventoryStocker", name="Inventory Stocker", version=Utils.VERSION, dependencies = "required-after:Forge@[4.1.1.251,)")
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {"InventoryStocker"}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = ("InventoryStocker"), packetHandler = ServerPacketHandler.class))
public class InventoryStocker
{
	@SidedProxy(clientSide = "com.kaijin.InventoryStocker.ClientProxy", serverSide = "com.kaijin.InventoryStocker.CommonProxy")
	public static CommonProxy proxy; //This object will be populated with the class that you choose for the environment
	
	@Instance("InventoryStocker")
	public static InventoryStocker instance; //The instance of the mod that will be defined, populated, and callable

	static int InventoryStockerBlockID;
	static public boolean isDebugging;

	@PreInit
	public static void preInit(FMLPreInitializationEvent event)
	{
		try
		{
			Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
			configuration.load();
			InventoryStockerBlockID = configuration.getBlock("InventoryStocker", 2490).getInt();
			isDebugging = Boolean.parseBoolean((configuration.get(configuration.CATEGORY_GENERAL, "debug", false).value));
			configuration.save();
		}
		catch (Exception var1)
		{
			System.out.println("[Inventory Stocker] Error while trying to access configuration!");
			throw new RuntimeException(var1);
		}
	}

	@Init
	public void load(FMLInitializationEvent event)
	{
		InventoryStocker = new BlockInventoryStocker(InventoryStockerBlockID, 0, Material.ground).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("InventoryStocker").setCreativeTab(CreativeTabs.tabBlock);
		LanguageRegistry.addName(InventoryStocker, "Inventory Stocker");
		GameRegistry.registerBlock(InventoryStocker);
		GameRegistry.registerTileEntity(TileEntityInventoryStocker.class, "InventoryStocker");
		if (Utils.isDebug())
		{
			GameRegistry.addRecipe(new ItemStack(InventoryStocker, 16), new Object[] {"XX", "XX", 'X', Block.dirt}); // Testing Recipe
		}
		GameRegistry.addRecipe(new ItemStack(InventoryStocker, 1), new Object[] {"RIR", "PCP", "RIR", 'C', Block.chest, 'I', Item.ingotIron, 'P', Block.pistonBase, 'R', Item.redstone});
		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
		proxy.load();
		if (proxy.isServer())
		{
			FMLLog.getLogger().info ("InventoryStocker loaded.");
		}
		if (isDebugging)
		{
			FMLLog.getLogger().info("InventoryStocker debugging enabled.");
		}
	}

	public static Block InventoryStocker; 
}
