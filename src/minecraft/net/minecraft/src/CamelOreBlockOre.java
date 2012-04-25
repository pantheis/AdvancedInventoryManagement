package net.minecraft.src;

import net.minecraft.src.forge.*;
import java.util.*;

public class CamelOreBlockOre extends Block implements ITextureProvider
{
        public CamelOreBlockOre(int i, int j)
        {
                super(i, j, Material.ground);
        }

// Commenting this out for now, not sure it's needed for having a block receive redstone
/*        public boolean canConnectRedstone(IBlockAccess iba, int i, int j, int k, int dir)
        {
            return true;
        }
*/
        public void addCreativeItems(ArrayList itemList)
        {
                itemList.add(new ItemStack(this));
        }

        public String getTextureFile()
        {
                return "/CamelMod/CamelOre/terrain.png";
        }
}
