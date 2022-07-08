package com.simibubi.create.content.logistics.trains.management.display;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;

public class FlapDisplayBlock extends HorizontalKineticBlock
	implements ITE<FlapDisplayTileEntity>, IWrenchable, ICogWheel, SimpleWaterloggedBlock {

	public static final BooleanProperty UP = BooleanProperty.create("up");
	public static final BooleanProperty DOWN = BooleanProperty.create("down");

	public FlapDisplayBlock(Properties p_49795_) {
		super(p_49795_);
		registerDefaultState(defaultBlockState().setValue(UP, false)
			.setValue(DOWN, false)
			.setValue(WATERLOGGED, false));
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return super.areStatesKineticallyEquivalent(oldState, newState);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(UP, DOWN, WATERLOGGED));
	}

	@Override
	public SpeedLevel getMinimumRequiredSpeedLevel() {
		return SpeedLevel.MEDIUM;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction face = context.getClickedFace();
		BlockPos clickedPos = context.getClickedPos();
		BlockPos placedOnPos = clickedPos.relative(face.getOpposite());
		Level level = context.getLevel();
		BlockState blockState = level.getBlockState(placedOnPos);
		BlockState stateForPlacement = defaultBlockState();
		FluidState ifluidstate = context.getLevel()
			.getFluidState(context.getClickedPos());

		if ((blockState.getBlock() != this) || (context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown()))
			stateForPlacement = super.getStateForPlacement(context);
		else {
			Direction otherFacing = blockState.getValue(HORIZONTAL_FACING);
			stateForPlacement = stateForPlacement.setValue(HORIZONTAL_FACING, otherFacing);
		}

		return updateColumn(level, clickedPos,
			stateForPlacement.setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER)), true);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {

		if (player.isShiftKeyDown())
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);

		IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
		if (placementHelper.matchesItem(heldItem))
			return placementHelper.getOffset(player, world, state, pos, ray)
				.placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);

		FlapDisplayTileEntity flapTe = getTileEntity(world, pos);

		if (flapTe == null)
			return InteractionResult.PASS;
		flapTe = flapTe.getController();
		if (flapTe == null)
			return InteractionResult.PASS;

		double yCoord = ray.getLocation()
			.add(Vec3.atLowerCornerOf(ray.getDirection()
				.getOpposite()
				.getNormal())
				.scale(.125f)).y;

		int lineIndex = flapTe.getLineIndexAt(yCoord);

		if (heldItem.isEmpty()) {
			if (!flapTe.isSpeedRequirementFulfilled())
				return InteractionResult.PASS;
			flapTe.applyTextManually(lineIndex, null);
			return InteractionResult.SUCCESS;
		}

		if (heldItem.getItem() == Items.GLOW_INK_SAC) {
			if (!world.isClientSide) {
				world.playSound(null, pos, SoundEvents.INK_SAC_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
				flapTe.setGlowing(lineIndex);
			}
			return InteractionResult.SUCCESS;
		}

		boolean display = heldItem.getItem() == Items.NAME_TAG && heldItem.hasCustomHoverName();
		DyeColor dye = DyeColor.getColor(heldItem);

		if (!display && dye == null)
			return InteractionResult.PASS;
		if (dye == null && !flapTe.isSpeedRequirementFulfilled())
			return InteractionResult.PASS;
		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		CompoundTag tag = heldItem.getTagElement("display");
		String tagElement = tag != null && tag.contains("Name", Tag.TAG_STRING) ? tag.getString("Name") : null;

		if (display)
			flapTe.applyTextManually(lineIndex, tagElement);
		if (dye != null) {
			world.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
			flapTe.setColour(lineIndex, dye);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return AllShapes.FLAP_DISPLAY.get(pState.getValue(HORIZONTAL_FACING));
	}

	@Override
	public Class<FlapDisplayTileEntity> getTileEntityClass() {
		return FlapDisplayTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends FlapDisplayTileEntity> getTileEntityType() {
		return AllTileEntities.FLAP_DISPLAY.get();
	}

	@Override
	public float getParticleTargetRadius() {
		return .85f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .75f;
	}

	private BlockState updateColumn(Level level, BlockPos pos, BlockState state, boolean present) {
		MutableBlockPos currentPos = new MutableBlockPos();
		Axis axis = getConnectionAxis(state);

		for (Direction connection : Iterate.directionsInAxis(Axis.Y)) {
			boolean connect = true;

			Move: for (Direction movement : Iterate.directionsInAxis(axis)) {
				currentPos.set(pos);
				for (int i = 0; i < 1000; i++) {
					if (!level.isLoaded(currentPos))
						break;

					BlockState other1 = currentPos.equals(pos) ? state : level.getBlockState(currentPos);
					BlockState other2 = level.getBlockState(currentPos.relative(connection));
					boolean col1 = canConnect(state, other1);
					boolean col2 = canConnect(state, other2);
					currentPos.move(movement);

					if (!col1 && !col2)
						break;
					if (col1 && col2)
						continue;

					connect = false;
					break Move;
				}
			}
			state = setConnection(state, connection, connect);
		}
		return state;
	}

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
		if (pOldState.getBlock() == this)
			return;
		LevelTickAccess<Block> blockTicks = pLevel.getBlockTicks();
		if (!blockTicks.hasScheduledTick(pPos, this))
			pLevel.scheduleTick(pPos, this, 1);
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
		if (pState.getBlock() != this)
			return;
		BlockPos belowPos =
			pPos.relative(Direction.fromAxisAndDirection(getConnectionAxis(pState), AxisDirection.NEGATIVE));
		BlockState belowState = pLevel.getBlockState(belowPos);
		if (!canConnect(pState, belowState))
			KineticTileEntity.switchToBlockState(pLevel, pPos, updateColumn(pLevel, pPos, pState, true));
		withTileEntityDo(pLevel, pPos, FlapDisplayTileEntity::updateControllerStatus);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		return updatedShapeInner(state, pDirection, pNeighborState, pLevel, pCurrentPos);
	}

	private BlockState updatedShapeInner(BlockState state, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED))
			pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
		if (!canConnect(state, pNeighborState))
			return setConnection(state, pDirection, false);
		if (pDirection.getAxis() == getConnectionAxis(state))
			return withPropertiesOf(pNeighborState).setValue(WATERLOGGED, state.getValue(WATERLOGGED));
		return setConnection(state, pDirection, getConnection(pNeighborState, pDirection.getOpposite()));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false)
			: Fluids.EMPTY.defaultFluidState();
	}

	protected boolean canConnect(BlockState state, BlockState other) {
		return other.getBlock() == this && state.getValue(HORIZONTAL_FACING) == other.getValue(HORIZONTAL_FACING);
	}

	protected Axis getConnectionAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise()
			.getAxis();
	}

	public static boolean getConnection(BlockState state, Direction side) {
		BooleanProperty property = side == Direction.DOWN ? DOWN : side == Direction.UP ? UP : null;
		return property != null && state.getValue(property);
	}

	public static BlockState setConnection(BlockState state, Direction side, boolean connect) {
		BooleanProperty property = side == Direction.DOWN ? DOWN : side == Direction.UP ? UP : null;
		if (property != null)
			state = state.setValue(property, connect);
		return state;
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (pState.hasBlockEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasBlockEntity()))
			pLevel.removeBlockEntity(pPos);
		if (pIsMoving || pNewState.getBlock() == this)
			return;
		for (Direction d : Iterate.directionsInAxis(getConnectionAxis(pState))) {
			BlockPos relative = pPos.relative(d);
			BlockState adjacent = pLevel.getBlockState(relative);
			if (canConnect(pState, adjacent))
				KineticTileEntity.switchToBlockState(pLevel, relative, updateColumn(pLevel, relative, adjacent, false));
		}
	}

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	@MethodsReturnNonnullByDefault
	private static class PlacementHelper implements IPlacementHelper {
		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.DISPLAY_BOARD::isIn;
		}

		@Override
		public Predicate<BlockState> getStatePredicate() {
			return AllBlocks.DISPLAY_BOARD::has;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(),
				state.getValue(FlapDisplayBlock.HORIZONTAL_FACING)
					.getAxis(),
				dir -> world.getBlockState(pos.relative(dir))
					.getMaterial()
					.isReplaceable());

			return directions.isEmpty() ? PlacementOffset.fail()
				: PlacementOffset.success(pos.relative(directions.get(0)), s -> AllBlocks.DISPLAY_BOARD.get()
					.updateColumn(world, pos.relative(directions.get(0)),
						s.setValue(HORIZONTAL_FACING, state.getValue(FlapDisplayBlock.HORIZONTAL_FACING)), true));
		}
	}

}
