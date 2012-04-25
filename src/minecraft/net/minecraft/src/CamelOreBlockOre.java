package net.minecraft.src;

import net.minecraft.src.forge.*;

public class CamelOreBlockOre extends Block implements ITextureProvider
{
        public CamelOreBlockOre(int i, int j)
        {
                super(i, j, Material.ground);
        }

        public String getTextureFile()
        {
                return "/CamelMod/CamelOre/terrain.png";
        }
}