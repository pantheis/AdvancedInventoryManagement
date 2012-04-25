package net.minecraft.src;

import net.minecraft.src.forge.*;

public class CamelOreItem extends Item implements ITextureProvider
{
        public CamelOreItem(int i)
        {
                super(i);
        }

        public String getTextureFile()
        {
                return "/CamelMod/CamelOre/gui/items.png";
        }
}