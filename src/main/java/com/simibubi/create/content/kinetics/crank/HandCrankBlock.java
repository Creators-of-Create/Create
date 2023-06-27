package com.simibubi.create.content.kinetics.crank;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HandCrankBlock extends DirectionalKineticBlock
	implements IBE<HandCrankBlockEntity>, ProperWaterloggedBlock {

	public HandCrankBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.CRANK.get(state.getValue(FACING));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(WATERLOGGED));
	}

	public int getRotationSpeed() {
		return 32;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
		BlockHitResult hit) {
		if (player.isSpectator())
			return InteractionResult.PASS;

		withBlockEntityDo(worldIn, pos, be -> be.turn(player.isShiftKeyDown()));
		if (!player.getItemInHand(handIn)
			.is(AllItems.EXTENDO_GRIP.get()))
			player.causeFoodExhaustion(getRotationSpeed() * AllConfigs.server().kinetics.crankHungerMultiplier.getF());

		if (player.getFoodData()
			.getFoodLevel() == 0)
			AllAdvancements.HAND_CRANK.awardTo(player);

		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferred = getPreferredFacing(context);
		BlockState defaultBlockState = withWater(defaultBlockState(), context);
		if (preferred == null || (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown()))
			return defaultBlockState.setValue(FACING, context.getClickedFace());
		return defaultBlockState.setValue(FACING, preferred.getOpposite());
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		Direction facing = state.getValue(FACING)
			.getOpposite();
		BlockPos neighbourPos = pos.relative(facing);
		BlockState neighbour = worldIn.getBlockState(neighbourPos);
		return !neighbour.getCollisionShape(worldIn, neighbourPos)
			.isEmpty();
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
		boolean isMoving) {
		if (worldIn.isClientSide)
			return;

		Direction blockFacing = state.getValue(FACING);
		if (fromPos.equals(pos.relative(blockFacing.getOpposite()))) {
			if (!canSurvive(state, worldIn, pos)) {
				worldIn.destroyBlock(pos, true);
				return;
			}
		}
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
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING)
			.getOpposite();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING)
			.getAxis();
	}

	@Override
	public Class<HandCrankBlockEntity> getBlockEntityClass() {
		return HandCrankBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends HandCrankBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.HAND_CRANK.get();
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	public static Couple<Integer> getSpeedRange() {
		return Couple.create(32, 32);
	}

}
