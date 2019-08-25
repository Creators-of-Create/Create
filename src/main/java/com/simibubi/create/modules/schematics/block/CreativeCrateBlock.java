package com.simibubi.create.modules.schematics.block;

import com.simibubi.create.foundation.block.InfoBlock;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;

import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class CreativeCrateBlock extends InfoBlock {

	protected static final VoxelShape shape = makeCuboidShape(1, 0, 1, 15, 14, 15);

	public CreativeCrateBlock() {
		super(Properties.create(Material.WOOD));
	}

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return shape;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos,
			ISelectionContext context) {
		return shape;
	}

	@Override
	public ItemDescription getDescription() {
		Palette blue = Palette.Blue;
		return new ItemDescription(blue)
				.withSummary("Grants an attached " + h("Schematicannon", blue) + " unlimited access to blocks.")
				.createTabs();
	}

}
