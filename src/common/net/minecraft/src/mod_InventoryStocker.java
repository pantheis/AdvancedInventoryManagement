package net.minecraft.src;

import java.io.File;
import java.util.*;
import net.minecraft.src.forge.*;
import com.kaijin.InventoryStocker.*;

public class mod_InventoryStocker extends NetworkMod
{
    static Configuration configuration = CommonProxy.getConfiguration();
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
        ModLoader.registerBlock(InventoryStocker);
        ModLoader.registerTileEntity(TileEntityInventoryStocker.class, "InventoryStocker");
        ModLoader.addRecipe(new ItemStack(InventoryStocker, 16), new Object[] {"XX", "XX", 'X', Block.dirt}); // Testing Recipe
        ModLoader.addRecipe(new ItemStack(InventoryStocker, 1), new Object[] {"IWI", "PRP", "IWI", 'I', Item.ingotIron, 'W', Block.planks, 'P', Block.pistonBase, 'R', Item.redstone});
        MinecraftForge.setGuiHandler(this.instance, new GuiHandlerInventoryStocker());
        CommonProxy.load();
        if (CommonProxy.isServer())
        {
            ModLoader.getLogger().info ("InventoryStocker v" + getVersion() + " loaded.");
        }
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
        return "0.4.0a";
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
