package net.minecraft.src;

import net.minecraft.src.forge.*;
import java.io.File;
import java.util.*;
import net.minecraft.server.*;
import kaijin.InventoryStocker.*;

public class mod_InventoryStocker extends NetworkMod {
	static Configuration configuration = new Configuration(new File("config/InventoryStocker.cfg"));
	static int InventoryStockerBlockID = configurationProperties();
    public static final Block InventoryStockerBlock = new BlockInventoryStocker(InventoryStockerBlockID, 0).setHardness(0.2F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("oreTitanium");

    public mod_InventoryStocker() {

    }

    public void load() {
        ModLoader.registerBlock(InventoryStockerBlock);
        ModLoader.addRecipe(new ItemStack(InventoryStockerBlock, 16), new Object[] {"XX", "XX", Character.valueOf('X'), Block.dirt});                
    }
    
    public static int configurationProperties() {
    	configuration.load();
    	InventoryStockerBlockID = Integer.parseInt(configuration.getOrCreateBlockIdProperty("Inventory Stocker", 250).value);
    	configuration.save();
    	return InventoryStockerBlockID;
    }
    public String getVersion() {
        return "1.0.0";
    }
}
