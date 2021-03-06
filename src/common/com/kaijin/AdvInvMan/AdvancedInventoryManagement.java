/*******************************************************************************
 * Copyright (c) 2012 Yancarlo Ramsey and CJ Bowman
 * Licensed as open source with restrictions. Please see attached LICENSE.txt.
 ******************************************************************************/

package com.kaijin.AdvInvMan;

import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.FingerprintWarning;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLFingerprintViolationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = Info.MOD_ID, name=Info.MOD_NAME, version=Info.VERSION, /* @CERTIFICATE_SUM@ */ dependencies = Info.MOD_DEPENDENCIES)
@NetworkMod(clientSideRequired = true, serverSideRequired = false,
clientPacketHandlerSpec = @SidedPacketHandler(channels = {Info.PACKET_CHANNEL}, packetHandler = ClientPacketHandler.class),
serverPacketHandlerSpec = @SidedPacketHandler(channels = (Info.PACKET_CHANNEL), packetHandler = ServerPacketHandler.class))
public class AdvancedInventoryManagement
{
	@SidedProxy(clientSide = Info.PROXY_CLIENT, serverSide = Info.PROXY_SERVER)
	public static CommonProxy proxy; //This object will be populated with the class that you choose for the environment
	
	@Instance(Info.MOD_ID)
	public static AdvancedInventoryManagement instance; //The instance of the mod that will be defined, populated, and callable

	@PreInit
	public static void preInit(FMLPreInitializationEvent event)
	{
		try
		{
			Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
			configuration.load();
			Info.blockIDInventoryStocker = configuration.getBlock("InventoryStocker", 2490).getInt();
			Info.isDebugging = (configuration.get(configuration.CATEGORY_GENERAL, "debug", Info.isDebugging).getBoolean(Info.isDebugging));
			configuration.save();
		}
		catch (Exception var1)
		{
			FMLLog.getLogger().info("[" + Info.MOD_NAME + "] Error while trying to access configuration!");
			throw new RuntimeException(var1);
		}
	}

	@Init
	public void load(FMLInitializationEvent event)
	{
		Info.blockInventoryStocker = new BlockStocker(Info.blockIDInventoryStocker, Material.ground).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("kaijin.invStocker").setCreativeTab(CreativeTabs.tabDecorations);
		GameRegistry.registerBlock(Info.blockInventoryStocker, "InventoryStocker");

		GameRegistry.registerTileEntity(TileEntityStocker.class, "InventoryStocker");
		GameRegistry.registerTileEntity(TileEntityStocker.class, "kaijin.inventoryStocker"); // Better TE reg key

		GameRegistry.addRecipe(new ItemStack(Info.blockInventoryStocker, 1), new Object[] {"RIR", "PCP", "RIR", 'C', Block.chest, 'I', Item.ingotIron, 'P', Block.pistonBase, 'R', Item.redstone});

		NetworkRegistry.instance().registerGuiHandler(this.instance, proxy);
		proxy.load();

		if (proxy.isServer())
		{
			FMLLog.getLogger().info(Info.MOD_NAME + " loaded.");
		}

		if (Info.isDebugging)
		{
			FMLLog.getLogger().info(Info.MOD_NAME + " debugging enabled.");
		}

		Info.registerStrings();
	}

	@FingerprintWarning
	public void certificateWarning(FMLFingerprintViolationEvent warning)
	{
		FMLLog.getLogger().log(Level.SEVERE, "[" + Info.MOD_NAME + "] [Certificate Error] Fingerprint does not match! This mod's jar file has been corrupted or modified from the original version.");
		FMLLog.getLogger().log(Level.SEVERE, "[" + Info.MOD_NAME + "] [Certificate Error] Expected fingerprint: " + warning.expectedFingerprint);
		FMLLog.getLogger().log(Level.SEVERE, "[" + Info.MOD_NAME + "] [Certificate Error] File: " + warning.source.getAbsolutePath());
	}
}
