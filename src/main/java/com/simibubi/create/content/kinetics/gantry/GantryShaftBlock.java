package com.simibubi.create.content.kinetics.gantry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.placement.PoleHelper;

import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.lang.Lang;
import net.createmod.catnip.utility.placement.IPlacementHelper;
import net.createmod.catnip.utility.placement.PlacementHelpers;
import net.createmod.catnip.utility.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GantryShaftBlock extends DirectionalKineticBlock implements IBE<GantryShaftBlockEntity> {

	public static final Property<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public enum Part implements StringRepresentable {
		START, MIDDLE, END, SINGLE;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(PART, POWERED));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);

		IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
		if (!placementHelper.matchesItem(heldItem))
			return InteractionResult.PASS;

		return placementHelper.getOffset(player, world, state, pos, ray)
			.placeInWorld(world, ((BlockItem) heldItem.getItem()), player, hand, ray);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.EIGHT_VOXEL_POLE.get(state.getValue(FACING)
			.getAxis());
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor world,
		BlockPos pos, BlockPos neighbourPos) {
		Direction facing = state.getValue(FACING);
		Axis axis = facing.getAxis();
		if (direction.getAxis() != axis)
			return state;
		boolean connect = AllBlocks.GANTRY_SHAFT.has(neighbour) && neighbour.getValue(FACING) == facing;

		Part part = state.getValue(PART);
		if (direction.getAxisDirection() == facing.getAxisDirection()) {
			if (connect) {
				if (part == Part.END)
					part = Part.MIDDLE;
				if (part == Part.SINGLE)
					part = Part.START;
			} else {
				if (part == Part.MIDDLE)
					part = Part.END;
				if (part == Part.START)
					part = Part.SINGLE;
			}
		} else {
			if (connect) {
				if (part == Part.START)
					part = Part.MIDDLE;
				if (part == Part.SINGLE)
					part = Part.END;
			} else {
				if (part == Part.MIDDLE)
					part = Part.START;
				if (part == Part.END)
					part = Part.SINGLE;
			}
		}

		return state.setValue(PART, part);
	}

	public GantryShaftBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false)
			.setValue(PART, Part.SINGLE));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = super.getStateForPlacement(context);
		BlockPos pos = context.getClickedPos();
		Level world = context.getLevel();
		Direction face = context.getClickedFace();

		BlockState neighbour = world.getBlockState(pos.relative(state.getValue(FACING)
			.getOpposite()));

		BlockState clickedState =
			AllBlocks.GANTRY_SHAFT.has(neighbour) ? neighbour : world.getBlockState(pos.relative(face.getOpposite()));

		if (AllBlocks.GANTRY_SHAFT.has(clickedState) && clickedState.getValue(FACING)
			.getAxis() == state.getValue(FACING)
				.getAxis()) {
			Direction facing = clickedState.getValue(FACING);
			state = state.setValue(FACING, context.getPlayer() == null || !context.getPlayer()
				.isShiftKeyDown() ? facing : facing.getOpposite());
		}

		return state.setValue(POWERED, shouldBePowered(state, world, pos));
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		InteractionResult onWrenched = super.onWrenched(state, context);
		if (onWrenched.consumesAction()) {
			BlockPos pos = context.getClickedPos();
			Level world = context.getLevel();
			neighborChanged(world.getBlockState(pos), world, pos, state.getBlock(), pos, false);
		}
		return onWrenched;
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);

		if (!worldIn.isClientSide() && oldState.is(AllBlocks.GANTRY_SHAFT.get())) {
			Part oldPart = oldState.getValue(PART), part = state.getValue(PART);
			if ((oldPart != Part.MIDDLE && part == Part.MIDDLE) || (oldPart == Part.SINGLE && part != Part.SINGLE)) {
				BlockEntity be = worldIn.getBlockEntity(pos);
				if (be instanceof GantryShaftBlockEntity)
					((GantryShaftBlockEntity) be).checkAttachedCarriageBlocks();
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
		boolean p_220069_6_) {
		if (worldIn.isClientSide)
			return;
		boolean previouslyPowered = state.getValue(POWERED);
		boolean shouldPower = worldIn.hasNeighborSignal(pos); // shouldBePowered(state, worldIn, pos);

		if (!previouslyPowered && !shouldPower && shouldBePowered(state, worldIn, pos)) {
			worldIn.setBlock(pos, state.setValue(POWERED, true), 3);
			return;
		}

		if (previouslyPowered == shouldPower)
			return;

		// Collect affected gantry shafts
		List<BlockPos> toUpdate = new ArrayList<>();
		Direction facing = state.getValue(FACING);
		Axis axis = facing.getAxis();
		for (Direction d : Iterate.directionsInAxis(axis)) {
			BlockPos currentPos = pos.relative(d);
			while (true) {
				if (!worldIn.isLoaded(currentPos))
					break;
				BlockState currentState = worldIn.getBlockState(currentPos);
				if (!(currentState.getBlock() instanceof GantryShaftBlock))
					break;
				if (currentState.getValue(FACING) != facing)
					break;
				if (!shouldPower && currentState.getValue(POWERED) && worldIn.hasNeighborSignal(currentPos))
					return;
				if (currentState.getValue(POWERED) == shouldPower)
					break;
				toUpdate.add(currentPos);
				currentPos = currentPos.relative(d);
			}
		}

		toUpdate.add(pos);
		for (BlockPos blockPos : toUpdate) {
			BlockState blockState = worldIn.getBlockState(blockPos);
			BlockEntity be = worldIn.getBlockEntity(blockPos);
			if (be instanceof KineticBlockEntity)
				((KineticBlockEntity) be).detachKinetics();
			if (blockState.getBlock() instanceof GantryShaftBlock)
				worldIn.setBlock(blockPos, blockState.setValue(POWERED, shouldPower), 2);
		}
	}

	protected boolean shouldBePowered(BlockState state, Level worldIn, BlockPos pos) {
		boolean shouldPower = worldIn.hasNeighborSignal(pos);

		Direction facing = state.getValue(FACING);
		for (Direction d : Iterate.directionsInAxis(facing.getAxis())) {
			BlockPos neighbourPos = pos.relative(d);
			if (!worldIn.isLoaded(neighbourPos))
				continue;
			BlockState neighbourState = worldIn.getBlockState(neighbourPos);
			if (!(neighbourState.getBlock() instanceof GantryShaftBlock))
				continue;
			if (neighbourState.getValue(FACING) != facing)
				continue;
			shouldPower |= neighbourState.getValue(POWERED);
		}

		return shouldPower;
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(FACING)
			.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING)
			.getAxis();
	}

	@Override
	protected boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
		return super.areStatesKineticallyEquivalent(oldState, newState)
			&& oldState.getValue(POWERED) == newState.getValue(POWERED);
	}

	@Override
	public float getParticleTargetRadius() {
		return .35f;
	}

	@Override
	public float getParticleInitialRadius() {
		return .25f;
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	public static class PlacementHelper extends PoleHelper<Direction> {

		public PlacementHelper() {
			super(AllBlocks.GANTRY_SHAFT::has, s -> s.getValue(FACING)
				.getAxis(), FACING);
		}

		@Override
		public Predicate<ItemStack> getItemPredicate() {
			return AllBlocks.GANTRY_SHAFT::isIn;
		}

		@Override
		public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
			BlockHitResult ray) {
			PlacementOffset offset = super.getOffset(player, world, state, pos, ray);
			offset.withTransform(offset.getTransform()
				.andThen(s -> s.setValue(POWERED, state.getValue(POWERED))));
			return offset;
		}
	}

	@Override
	public Class<GantryShaftBlockEntity> getBlockEntityClass() {
		return GantryShaftBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends GantryShaftBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.GANTRY_SHAFT.get();
	}

}
