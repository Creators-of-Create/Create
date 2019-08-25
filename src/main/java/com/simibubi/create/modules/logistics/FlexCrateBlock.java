package com.simibubi.create.modules.logistics;

import com.simibubi.create.foundation.block.InfoBlock;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class FlexCrateBlock extends InfoBlock {

	public FlexCrateBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public boolean hasTileEntity() {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new FlexCrateTileEntity();
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Yellow;
		return new ItemDescription(color)
				.withSummary("This Storage Container allows Manual control over its capacity. Can hold up to "
						+ h("16 Stacks", color) + " of Items.");
	}

}
