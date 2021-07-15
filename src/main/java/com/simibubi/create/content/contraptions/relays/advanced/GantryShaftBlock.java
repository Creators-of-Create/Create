package com.simibubi.create.content.contraptions.relays.advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;
import com.simibubi.create.foundation.utility.placement.util.PoleHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import net.minecraft.block.AbstractBlock.Properties;

public class GantryShaftBlock extends DirectionalKineticBlock {

	public static final Property<Part> PART = EnumProperty.create("part", Part.class);
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

	public enum Part implements IStringSerializable {
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
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray) {
		ItemStack heldItem = player.getItemInHand(hand);

		IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
		if (!placementHelper.matchesItem(heldItem))
			return ActionResultType.PASS;

		return placementHelper.getOffset(player, world, state, pos, ray).placeInWorld(world, ((BlockItem) heldItem.getItem()), player, hand, ray);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.EIGHT_VOXEL_POLE.get(state.getValue(FACING)
			.getAxis());
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, IWorld world,
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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = super.getStateForPlacement(context);
		BlockPos pos = context.getClickedPos();
		World world = context.getLevel();
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
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		ActionResultType onWrenched = super.onWrenched(state, context);
		if (onWrenched.consumesAction()) {
			BlockPos pos = context.getClickedPos();
			World world = context.getLevel();
			neighborChanged(world.getBlockState(pos), world, pos, state.getBlock(), pos, false);
		}
		return onWrenched;
	}

	@Override
	public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, worldIn, pos, oldState, isMoving);

		if (!worldIn.isClientSide() && oldState.getBlock().is(AllBlocks.GANTRY_SHAFT.get())) {
			Part oldPart = oldState.getValue(PART), part = state.getValue(PART);
			if ((oldPart != Part.MIDDLE && part == Part.MIDDLE) || (oldPart == Part.SINGLE && part != Part.SINGLE)) {
				TileEntity te = worldIn.getBlockEntity(pos);
				if (te instanceof GantryShaftTileEntity)
					((GantryShaftTileEntity) te).checkAttachedCarriageBlocks();
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block p_220069_4_, BlockPos p_220069_5_,
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
			TileEntity te = worldIn.getBlockEntity(blockPos);
			if (te instanceof KineticTileEntity)
				((KineticTileEntity) te).detachKinetics();
			if (blockState.getBlock() instanceof GantryShaftBlock)
				worldIn.setBlock(blockPos, blockState.setValue(POWERED, shouldPower), 2);
		}
	}

	protected boolean shouldBePowered(BlockState state, World worldIn, BlockPos pos) {
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
	public boolean hasShaftTowards(IWorldReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(FACING)
			.getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING)
			.getAxis();
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.GANTRY_SHAFT.create();
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
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
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
		public PlacementOffset getOffset(PlayerEntity player, World world, BlockState state, BlockPos pos, BlockRayTraceResult ray) {
			PlacementOffset offset = super.getOffset(player, world, state, pos, ray);
			offset.withTransform(offset.getTransform().andThen(s -> s.setValue(POWERED, state.getValue(POWERED))));
			return offset;
		}
	}

}
