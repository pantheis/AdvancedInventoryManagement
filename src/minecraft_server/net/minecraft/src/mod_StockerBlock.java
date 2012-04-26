package net.minecraft.src;

import net.minecraft.src.forge.*;
import java.io.File;
import java.util.*;
import net.minecraft.server.*;
import kaijin.StockerBlock.*;

public class mod_StockerBlock extends NetworkMod
{
	static Configuration configuration = new Configuration(new File("config/StockerBlock.cfg"));
	static int stockerBlockID = configurationProperties();
    public static final Block stockerBlock = new StockerBlock(stockerBlockID, 0).setHardness(0.2F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("oreTitanium");

    public mod_StockerBlock()
    {

    }

    public void load()
    {
        ModLoader.registerBlock(stockerBlock);
        ModLoader.addRecipe(new ItemStack(stockerBlock, 16), new Object[] {"XX", "XX", Character.valueOf('X'), Block.dirt});                
    }
    
    public static int configurationProperties()
    {
    	configuration.load();
    	stockerBlockID = Integer.parseInt(configuration.getOrCreateBlockIdProperty("Inventory Stocker", 250).value);
    	configuration.save();
    	return stockerBlockID;
    }
    public String getVersion()
    {
        return "1.0.0";
    }
}
