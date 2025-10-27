package com.simibubi.create.content.logistics.crate;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

public class CrateBlock extends WrenchableDirectionalBlock implements IWrenchable {

	public static final MapCodec<CrateBlock> CODEC = simpleCodec(CrateBlock::new);

	public CrateBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.CRATE_BLOCK_SHAPE;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
		return CODEC;
	}
}
