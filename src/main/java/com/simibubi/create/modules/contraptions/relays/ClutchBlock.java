package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class ClutchBlock extends GearshiftBlock {

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ClutchTileEntity();
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Red;
		return new ItemDescription(color).withSummary("A controllable rotation switch for connected shafts.")
				.withBehaviour("When Powered", h("Stops", color) + " conveying rotation to the other side.")
				.createTabs();
	}

}
