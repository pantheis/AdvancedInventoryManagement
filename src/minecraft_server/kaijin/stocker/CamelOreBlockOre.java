package kaijin.stocker;

import net.minecraft.src.forge.*;
import java.util.*;
import net.minecraft.src.*;

public class CamelOreBlockOre extends Block implements ITextureProvider
{
        public CamelOreBlockOre(int i, int j)
        {
                super(i, j, Material.ground);
        }

        public void addCreativeItems(ArrayList itemList)
        {
                itemList.add(new ItemStack(this));
        }

        public String getTextureFile()
        {
                return "/CamelMod/CamelOre/terrain.png";
        }
}
