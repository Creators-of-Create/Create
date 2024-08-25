package com.simibubi.create.content.kinetics.waterwheel;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class LargeWaterWheelBlock extends RotatedPillarKineticBlock implements IBE<LargeWaterWheelBlockEntity> {

	public static final BooleanProperty EXTENSION = BooleanProperty.create("extension");

	public LargeWaterWheelBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(EXTENSION, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(EXTENSION));
	}

	public Axis getAxisForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).getValue(AXIS);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState stateForPlacement = super.getStateForPlacement(context);
		BlockPos pos = context.getClickedPos();
		Axis axis = stateForPlacement.getValue(AXIS);

		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (axis.choose(x, y, z) != 0)
						continue;
					BlockPos offset = new BlockPos(x, y, z);
					if (offset.equals(BlockPos.ZERO))
						continue;
					BlockState occupiedState = context.getLevel()
						.getBlockState(pos.offset(offset));
					if (!occupiedState.getMaterial()
						.isReplaceable())
						return null;
				}
			}
		}

		if (context.getLevel()
			.getBlockState(pos.relative(Direction.fromAxisAndDirection(axis, AxisDirection.NEGATIVE)))
			.is(this))
			stateForPlacement = stateForPlacement.setValue(EXTENSION, true);

		return stateForPlacement;
	}

	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
		BlockHitResult pHit) {
		return onBlockEntityUse(pLevel, pPos, wwt -> wwt.applyMaterialIfValid(pPlayer.getItemInHand(pHand)));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.PASS;
	}

	@Override
	public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		if (pDirection != Direction.fromAxisAndDirection(pState.getValue(AXIS), AxisDirection.NEGATIVE))
			return pState;
		return pState.setValue(EXTENSION, pNeighborState.is(this));
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (!level.getBlockTicks()
			.hasScheduledTick(pos, this))
			level.scheduleTick(pos, this, 1);
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		Axis axis = pState.getValue(AXIS);
		for (Direction side : Iterate.directions) {
			if (side.getAxis() == axis)
				continue;
			for (boolean secondary : Iterate.falseAndTrue) {
				Direction targetSide = secondary ? side.getClockWise(axis) : side;
				BlockPos structurePos = (secondary ? pPos.relative(side) : pPos).relative(targetSide);
				BlockState occupiedState = pLevel.getBlockState(structurePos);
				BlockState requiredStructure = AllBlocks.WATER_WHEEL_STRUCTURAL.getDefaultState()
					.setValue(WaterWheelStructuralBlock.FACING, targetSide.getOpposite());
				if (occupiedState == requiredStructure)
					continue;
				if (!occupiedState.getMaterial()
					.isReplaceable()) {
					pLevel.destroyBlock(pPos, false);
					return;
				}
				pLevel.setBlockAndUpdate(structurePos, requiredStructure);
			}
		}
		withBlockEntityDo(pLevel, pPos, WaterWheelBlockEntity::determineAndApplyFlowScore);
	}

	@Override
	public RenderShape getRenderShape(BlockState pState) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockEntityType<? extends LargeWaterWheelBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.LARGE_WATER_WHEEL.get();
	}

	@Override
	public Class<LargeWaterWheelBlockEntity> getBlockEntityClass() {
		return LargeWaterWheelBlockEntity.class;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == getRotationAxis(state);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	@Override
	public float getParticleTargetRadius() {
		return 2.5f;
	}

	@Override
	public float getParticleInitialRadius() {
		return 2.25f;
	}

	public static Couple<Integer> getSpeedRange() {
		return Couple.create(4, 4);
	}
	
	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return false;
	}

}
