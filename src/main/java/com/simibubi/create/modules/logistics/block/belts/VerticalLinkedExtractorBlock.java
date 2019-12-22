package com.simibubi.create.modules.logistics.block.belts;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.IHaveNoBlockItem;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

public class VerticalLinkedExtractorBlock extends LinkedExtractorBlock implements IHaveNoBlockItem {

	public static BooleanProperty UPWARD = BooleanProperty.create("upward");

	public VerticalLinkedExtractorBlock() {
		super();
		setDefaultState(getDefaultState().with(UPWARD, true));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(UPWARD));
	}

	@Override
	public Direction getBlockFacing(BlockState state) {
		return state.get(UPWARD) ? Direction.UP : Direction.DOWN;
	}

	@Override
	public ResourceLocation getLootTable() {
		return AllBlocks.LINKED_EXTRACTOR.get().getLootTable();
	}

	@Override
	public Vec3d getFilterPosition(BlockState state) {
		Direction facing = state.get(HORIZONTAL_FACING).getOpposite();
		return filterLocations.get((state.get(UPWARD) ? 4 : 8) + facing.getHorizontalIndex());
	}

}
