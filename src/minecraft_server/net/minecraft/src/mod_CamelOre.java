package net.minecraft.src;

import net.minecraft.src.forge.*;

import java.io.File;
import java.util.*;

import forge.MinecraftForge;
import net.minecraft.server.*;

public class mod_CamelOre extends NetworkMod
{
	static Configuration configuration = new Configuration(new File(, "config/CamelOre.cfg"));	
    public static final Block oreTitanium = new CamelOreBlockOre(250, 0).setHardness(0.2F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("oreTitanium");
    public static final Item ingotTitanium = (new Item(127)).setIconIndex(0).setItemName("ingotTitanium");

    public mod_CamelOre()
    {

    }

    public void load()
    {
//      MinecraftForgeClient.preloadTexture("/CamelMod/CamelOre/terrain.png");
//      MinecraftForgeClient.preloadTexture("/CamelMod/CamelOre/gui/items.png");
        ModLoader.registerBlock(oreTitanium);
//      ModLoader.addName(oreTitanium, "Titanium Ore");
//      ModLoader.addName(ingotTitanium, "Titanium Ingot");
        ModLoader.addRecipe(new ItemStack(ingotTitanium, 16), new Object[] {"XX", "XX", Character.valueOf('X'), Block.dirt});                
    }

    public String getVersion()
    {
        return "1.0.0";
    }
}