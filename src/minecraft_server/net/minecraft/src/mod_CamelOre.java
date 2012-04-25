package net.minecraft.src;

import net.minecraft.src.forge.*;
import java.io.File;
import java.util.*;
import net.minecraft.server.*;


public class mod_CamelOre extends NetworkMod
{
	static Configuration configuration = new Configuration(new File("config/CamelOre.cfg"));
	static int oreTitaniumBlockID = configurationProperties();
    public static final Block oreTitanium = new CamelOreBlockOre(oreTitaniumBlockID, 0).setHardness(0.2F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("oreTitanium");

    public mod_CamelOre()
    {

    }

    public void load()
    {
//      MinecraftForgeClient.preloadTexture("/CamelMod/CamelOre/terrain.png");
        ModLoader.registerBlock(oreTitanium);
//      ModLoader.addName(oreTitanium, "Titanium Ore");
        ModLoader.addRecipe(new ItemStack(oreTitanium, 16), new Object[] {"XX", "XX", Character.valueOf('X'), Block.dirt});                
    }
    
    public static int configurationProperties()
    {
    	configuration.load();
    	oreTitaniumBlockID = Integer.parseInt(configuration.getOrCreateBlockIdProperty("Titanium Ore", 250).value);
    	configuration.save();
    	return oreTitaniumBlockID;
    }
    public String getVersion()
    {
        return "1.0.0";
    }
}
