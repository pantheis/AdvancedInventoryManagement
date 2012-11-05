package com.kaijin.InventoryStocker;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.src.Block;

public class Info
{
	public static final String VERSION = "@VERSION@";
	public static final String BUILD_NUMBER = "@BUILD_NUMBER@";

	public static final String BLOCK_PNG = "/com/kaijin/InventoryStocker/textures/blockInventoryStocker.png";
	public static final String GUI_PNG = "/com/kaijin/InventoryStocker/textures/guiInventoryStocker.png";

	public static final String MOD_ID = "InventoryStocker";
	public static final String MOD_NAME = "Inventory Stocker";
	public static final String MOD_DEPENDENCIES = "required-after:Forge@[4.1.1.251,)";
	public static final String PROXY_CLIENT = "com.kaijin.InventoryStocker.ClientProxy";
	public static final String PROXY_SERVER = "com.kaijin.InventoryStocker.CommonProxy";
	public static final String PACKET_CHANNEL = MOD_ID;

	public static final String KEY_GUI_INPUT = "kaijin.invStocker.guiStrings.input";
	public static final String KEY_GUI_OUTPUT = "kaijin.invStocker.guiStrings.output";
	public static final String KEY_GUI_READY = "kaijin.invStocker.guiStrings.ready";
	public static final String KEY_GUI_NOTREADY = "kaijin.invStocker.guiStrings.notready";
	//public static final String 

	// Global variables
	public static boolean isDebugging;

	public static Block blockInventoryStocker;

	public static int blockIDInventoryStocker;

	public static void registerStrings()
	{
		final LanguageRegistry lang = LanguageRegistry.instance();
		lang.addName(Info.blockInventoryStocker, "Inventory Stocker");
		lang.addStringLocalization(KEY_GUI_INPUT, "Input");
		lang.addStringLocalization(KEY_GUI_OUTPUT, "Output");
		lang.addStringLocalization(KEY_GUI_READY, "Ready");
		lang.addStringLocalization(KEY_GUI_NOTREADY, "Not Ready");
	}
}
