package kaijin.StockerBlock;

import net.minecraft.src.forge.*;
import java.util.*;
import net.minecraft.src.*;

public class StockerBlock extends BlockContainer implements ITextureProvider {
        public StockerBlock(int i, int j) {
                super(i, j, Material.ground);
        }

        public void addCreativeItems(ArrayList itemList) {
                itemList.add(new ItemStack(this));
        }

        public String getTextureFile() {
                return "/Kaijin/StockerBlock/terrain.png";
        }

		@Override
		public TileEntity getBlockEntity() {
			// TODO Auto-generated method stub
			return new TileEntityStockerBlock();
		}
}
