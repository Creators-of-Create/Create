package com.simibubi.create.content.logistics.trains.track;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FakeTrackBlock extends Block implements EntityBlock, ProperWaterloggedBlock {

	public FakeTrackBlock(Properties p_49795_) {
		super(p_49795_.randomTicks()
			.noCollission()
			.noOcclusion());
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return Shapes.empty();
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public @Nullable BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos,
		@Nullable Mob mob) {
		return BlockPathTypes.DAMAGE_OTHER;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(WATERLOGGED));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		return withWater(super.getStateForPlacement(pContext), pContext);
	}
	
	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		updateWater(pLevel, pState, pCurrentPos);
		return pState;
	}
	
	@Override
	public FluidState getFluidState(BlockState pState) {
		return fluidState(pState);
	}
	
	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pLevel.getBlockEntity(pPos) instanceof FakeTrackBlockEntity be)
			be.randomTick();
	}

	public static void keepAlive(LevelAccessor level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof FakeTrackBlockEntity be)
			be.keepAlive();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return AllBlockEntityTypes.FAKE_TRACK.create(pPos, pState);
	}
	
	@Override
	public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
		LivingEntity entity, int numberOfParticles) {
		return true;
	}
	
	@Override
	public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
		return true;
	}

}
