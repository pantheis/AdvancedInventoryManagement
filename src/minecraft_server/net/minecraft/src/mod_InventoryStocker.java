package net.minecraft.src;

import net.minecraft.src.forge.*;
import java.io.File;
import java.util.*;
import net.minecraft.server.*;
import kaijin.InventoryStocker.*;

public class mod_InventoryStocker extends NetworkMod
{
    static Configuration configuration = new Configuration(new File("config/InventoryStocker.cfg"));
    static int InventoryStockerBlockID = configurationProperties();
    public static final Block InventoryStocker = new BlockInventoryStocker(InventoryStockerBlockID, 0).setHardness(0.75F).setResistance(5F).setStepSound(Block.soundStoneFootstep).setBlockName("inventoryStocker");

    public static mod_InventoryStocker instance;

    public mod_InventoryStocker()
    {
        instance = this;
    }

    public void load()
    {
        ModLoader.registerBlock(InventoryStocker);
        ModLoader.registerTileEntity(TileEntityInventoryStocker.class, "InventoryStocker");
        ModLoader.addRecipe(new ItemStack(InventoryStocker, 16), new Object[] {"XX", "XX", 'X', Block.dirt});
        MinecraftForge.setGuiHandler(this.instance, new GuiHandlerInventoryStocker());
    }

    public static int configurationProperties()
    {
        configuration.load();
        InventoryStockerBlockID = Integer.parseInt(configuration.getOrCreateBlockIdProperty("InventoryStocker", 249).value);
        configuration.save();
        return InventoryStockerBlockID;
    }
    public String getVersion()
    {
        return "0.0.2";
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
