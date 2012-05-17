package net.minecraft.src;

import net.minecraft.src.forge.*;
import java.io.File;
import java.util.*;
import net.minecraft.client.Minecraft;
import kaijin.InventoryStocker.*;

public class mod_InventoryStocker extends NetworkMod
{
    static Configuration configuration = new Configuration(new File(Minecraft.getMinecraftDir(), "config/InventoryStocker.cfg"));
    static int InventoryStockerBlockID = configurationProperties();
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
        MinecraftForgeClient.preloadTexture("/kaijin/InventoryStocker/terrain.png");
        ModLoader.registerBlock(InventoryStocker);
        ModLoader.registerTileEntity(TileEntityInventoryStocker.class, "InventoryStocker");
        ModLoader.addName(InventoryStocker, "Inventory Stocker");
        ModLoader.addRecipe(new ItemStack(InventoryStocker, 16), new Object[] {"XX", "XX", 'X', Block.dirt}); // Testing Recipe
        ModLoader.addRecipe(new ItemStack(InventoryStocker, 1), new Object[] {"IWI", "PRP", "IWI", 'I', Item.ingotIron, 'W', Block.planks, 'P', Block.pistonBase, 'R', Item.redstone});
        MinecraftForge.setGuiHandler(this.instance, new GuiHandlerInventoryStocker());
    }

    public static int configurationProperties()
    {
        configuration.load();
        InventoryStockerBlockID = Integer.parseInt(configuration.getOrCreateBlockIdProperty("InventoryStocker", 249).value);
        configuration.save();
        return InventoryStockerBlockID;
    }

    @Override
    public String getVersion()
    {
        return Utils.getVersion();
    }
    @Override public boolean clientSideRequired()
    {
        return true;
    }
    @Override public boolean serverSideRequired()
    {
        return false;
    }
}
