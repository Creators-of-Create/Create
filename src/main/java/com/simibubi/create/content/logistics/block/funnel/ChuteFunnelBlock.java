package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public abstract class ChuteFunnelBlock extends HorizontalInteractionFunnelBlock {

	public ChuteFunnelBlock(BlockEntry<? extends FunnelBlock> parent, Properties p_i48377_1_) {
		super(parent, p_i48377_1_);
	}

	public static boolean isOnValidChute(BlockState state, IWorldReader world, BlockPos pos) {
		Direction direction = state.get(HORIZONTAL_FACING);
		if (world.getBlockState(pos.offset(direction))
			.getBlock() instanceof ChuteBlock)
			return true;
		return false;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.CHUTE_FUNNEL.get(state.get(HORIZONTAL_FACING));
	}

	@Override
	protected boolean canStillInteract(BlockState state, IWorldReader world, BlockPos pos) {
		return isOnValidChute(state, world, pos);
	}

}
