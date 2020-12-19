package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedTileEntityBehaviour;
import com.simibubi.create.content.contraptions.wrench.IWrenchableWithBracket;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SixWayBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class FluidPipeBlock extends SixWayBlock implements IWaterLoggable, IWrenchableWithBracket {

	public FluidPipeBlock(Properties properties) {
		super(4 / 16f, properties);
		this.setDefaultState(super.getDefaultState().with(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		if (tryRemoveBracket(context))
			return ActionResultType.SUCCESS;

		World world = context.getWorld();
		BlockPos pos = context.getPos();
		Axis axis = getAxis(world, pos, state);
		if (axis == null)
			return ActionResultType.PASS;
		if (context.getFace()
			.getAxis() == axis)
			return ActionResultType.PASS;
		if (!world.isRemote)
			world.setBlockState(pos, AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
				.with(GlassFluidPipeBlock.AXIS, axis).with(BlockStateProperties.WATERLOGGED, state.get(BlockStateProperties.WATERLOGGED)));
		return ActionResultType.SUCCESS;
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult hit) {
		if (!AllBlocks.COPPER_CASING.isIn(player.getHeldItem(hand)))
			return ActionResultType.PASS;
		AllTriggers.triggerFor(AllTriggers.CASING_PIPE, player);
		if (!world.isRemote)
			world.setBlockState(pos,
				EncasedPipeBlock.transferSixWayProperties(state, AllBlocks.ENCASED_FLUID_PIPE.getDefaultState()));
		return ActionResultType.SUCCESS;
	}

	@Nullable
	private Axis getAxis(IBlockReader world, BlockPos pos, BlockState state) {
		return FluidPropagator.getStraightPipeAxis(state);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.FLUID_PIPE.create();
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockTypeChanged = state.getBlock() != newState.getBlock();
		if (blockTypeChanged && !world.isRemote)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state != newState && !isMoving)
			removeBracket(world, pos, true).ifPresent(stack -> Block.spawnAsEntity(world, pos, stack));
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.removeTileEntity(pos);
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (world.isRemote)
			return;
		if (state != oldState)
			world.getPendingBlockTicks()
				.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugPacketSender.func_218806_a(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!isOpenAt(state, d))
			return;
		world.getPendingBlockTicks()
			.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	public static boolean isPipe(BlockState state) {
		return state.getBlock() instanceof FluidPipeBlock;
	}

	public static boolean canConnectTo(IBlockDisplayReader world, BlockPos neighbourPos, BlockState neighbour, Direction direction) {
		if (FluidPropagator.hasFluidCapability(world, neighbourPos, direction.getOpposite()))
			return true;
		FluidTransportBehaviour transport = TileEntityBehaviour.get(world, neighbourPos, FluidTransportBehaviour.TYPE);
		BracketedTileEntityBehaviour bracket = TileEntityBehaviour.get(world, neighbourPos, BracketedTileEntityBehaviour.TYPE);
		if (isPipe(neighbour))
			return bracket == null || !bracket.isBacketPresent()
				|| FluidPropagator.getStraightPipeAxis(neighbour) == direction.getAxis();
		if (transport == null)
			return false;
		return transport.canHaveFlowToward(neighbour, direction.getOpposite());
	}

	public static boolean shouldDrawRim(IBlockDisplayReader world, BlockPos pos, BlockState state,
		Direction direction) {
		BlockPos offsetPos = pos.offset(direction);
		BlockState facingState = world.getBlockState(offsetPos);
		if (!isPipe(facingState))
			return true;
		if (!canConnectTo(world, offsetPos, facingState, direction))
			return true;
		if (!isCornerOrEndPipe(world, pos, state))
			return false;
		if (FluidPropagator.getStraightPipeAxis(facingState) != null)
			return true;
		if (!shouldDrawCasing(world, pos, state) && shouldDrawCasing(world, offsetPos, facingState))
			return true;
		if (isCornerOrEndPipe(world, offsetPos, facingState))
			return direction.getAxisDirection() == AxisDirection.POSITIVE;
		return true;
	}

	public static boolean isOpenAt(BlockState state, Direction direction) {
		return state.get(FACING_TO_PROPERTY_MAP.get(direction));
	}

	public static boolean isCornerOrEndPipe(IBlockDisplayReader world, BlockPos pos, BlockState state) {
		return isPipe(state) && FluidPropagator.getStraightPipeAxis(state) == null
			&& !shouldDrawCasing(world, pos, state);
	}

	public static boolean shouldDrawCasing(IBlockDisplayReader world, BlockPos pos, BlockState state) {
		if (!isPipe(state))
			return false;
		for (Axis axis : Iterate.axes) {
			int connections = 0;
			for (Direction direction : Iterate.directions)
				if (direction.getAxis() != axis && isOpenAt(state, direction))
					connections++;
			if (connections > 2)
				return true;
		}
		return false;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, BlockStateProperties.WATERLOGGED);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		FluidState FluidState = context.getWorld()
			.getFluidState(context.getPos());
		return updateBlockState(getDefaultState(), context.getNearestLookingDirection(), null, context.getWorld(),
			context.getPos()).with(BlockStateProperties.WATERLOGGED,
				Boolean.valueOf(FluidState.getFluid() == Fluids.WATER));
	}

	@Override
	public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState neighbourState,
		IWorld world, BlockPos pos, BlockPos neighbourPos) {
		if (state.get(BlockStateProperties.WATERLOGGED))
			world.getPendingFluidTicks()
				.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		if (isOpenAt(state, direction) && neighbourState.contains(BlockStateProperties.WATERLOGGED))
			world.getPendingBlockTicks()
				.scheduleTick(pos, this, 1, TickPriority.HIGH);
		return updateBlockState(state, direction, direction.getOpposite(), world, pos);
	}

	public BlockState updateBlockState(BlockState state, Direction preferredDirection, @Nullable Direction ignore,
		IBlockDisplayReader world, BlockPos pos) {

		BracketedTileEntityBehaviour bracket = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (bracket != null && bracket.isBacketPresent())
			return state;

		// Update sides that are not ignored
		for (Direction d : Iterate.directions)
			if (d != ignore) {
				boolean shouldConnect = canConnectTo(world, pos.offset(d), world.getBlockState(pos.offset(d)), d);
				state = state.with(FACING_TO_PROPERTY_MAP.get(d), shouldConnect);
			}

		// See if it has enough connections
		Direction connectedDirection = null;
		for (Direction d : Iterate.directions) {
			if (isOpenAt(state, d)) {
				if (connectedDirection != null)
					return state;
				connectedDirection = d;
			}
		}

		// Add opposite end if only one connection
		if (connectedDirection != null)
			return state.with(FACING_TO_PROPERTY_MAP.get(connectedDirection.getOpposite()), true);

		// Use preferred
		return state.with(FACING_TO_PROPERTY_MAP.get(preferredDirection), true)
			.with(FACING_TO_PROPERTY_MAP.get(preferredDirection.getOpposite()), true);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false)
			: Fluids.EMPTY.getDefaultState();
	}

	@Override
	public Optional<ItemStack> removeBracket(IBlockReader world, BlockPos pos, boolean inOnReplacedContext) {
		BracketedTileEntityBehaviour behaviour =
			BracketedTileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (behaviour == null)
			return Optional.empty();
		BlockState bracket = behaviour.getBracket();
		behaviour.removeBracket(inOnReplacedContext);
		if (bracket == Blocks.AIR.getDefaultState())
			return Optional.empty();
		return Optional.of(new ItemStack(bracket.getBlock()));
	}
}
