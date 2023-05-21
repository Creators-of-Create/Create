package com.simibubi.create.content.decoration;

import java.util.Random;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MetalScaffoldingBlock extends ScaffoldingBlock implements IWrenchable {

	public MetalScaffoldingBlock(Properties pProperties) {
		super(pProperties);
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRand) {}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		return true;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos,
		CollisionContext pContext) {
		if (pState.getValue(BOTTOM))
			return AllShapes.SCAFFOLD_HALF;
		return super.getCollisionShape(pState, pLevel, pPos, pContext);
	}
	
	@Override
	public boolean isScaffolding(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		if (pState.getValue(BOTTOM))
			return AllShapes.SCAFFOLD_HALF;
		if (!pContext.isHoldingItem(pState.getBlock()
			.asItem()))
			return AllShapes.SCAFFOLD_FULL;
		return Shapes.block();
	}

	@Override
	public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
		return Shapes.block();
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
		BlockPos pCurrentPos, BlockPos pFacingPos) {
		super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
		BlockState stateBelow = pLevel.getBlockState(pCurrentPos.below());
		return pFacing == Direction.DOWN ? pState.setValue(BOTTOM,
			!stateBelow.is(this) && !stateBelow.isFaceSturdy(pLevel, pCurrentPos.below(), Direction.UP)) : pState;
	}

	@Override
	public boolean supportsExternalFaceHiding(BlockState state) {
		return true;
	}

	@Override
	public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState,
		Direction dir) {
		if (!(neighborState.getBlock() instanceof MetalScaffoldingBlock))
			return false;
		if (!neighborState.getValue(BOTTOM) && state.getValue(BOTTOM))
			return false;
		return dir.getAxis() != Axis.Y;
	}

}
