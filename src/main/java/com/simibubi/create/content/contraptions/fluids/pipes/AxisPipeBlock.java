package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
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
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import net.minecraft.block.AbstractBlock.Properties;

public class AxisPipeBlock extends RotatedPillarBlock implements IWrenchableWithBracket, IAxisPipe {

	public AxisPipeBlock(Properties p_i48339_1_) {
		super(p_i48339_1_);
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockTypeChanged = state.getBlock() != newState.getBlock();
		if (blockTypeChanged && !world.isClientSide)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state != newState && !isMoving)
			removeBracket(world, pos, true).ifPresent(stack -> Block.popResource(world, pos, stack));
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.removeBlockEntity(pos);
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult hit) {
		if (!AllBlocks.COPPER_CASING.isIn(player.getItemInHand(hand)))
			return ActionResultType.PASS;
		if (!world.isClientSide) {
			BlockState newState = AllBlocks.ENCASED_FLUID_PIPE.getDefaultState();
			for (Direction d : Iterate.directionsInAxis(getAxis(state)))
				newState = newState.setValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(d), true);
			FluidTransportBehaviour.cacheFlows(world, pos);
			world.setBlockAndUpdate(pos, newState);
			FluidTransportBehaviour.loadFlows(world, pos);
		}
		AllTriggers.triggerFor(AllTriggers.CASING_PIPE, player);
		return ActionResultType.SUCCESS;
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (world.isClientSide)
			return;
		if (state != oldState)
			world.getBlockTicks()
				.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
		PlayerEntity player) {
		return AllBlocks.FLUID_PIPE.asStack();
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugPacketSender.sendNeighborsUpdatePacket(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!isOpenAt(state, d))
			return;
		world.getBlockTicks()
			.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	public static boolean isOpenAt(BlockState state, Direction d) {
		return d.getAxis() == state.getValue(AXIS);
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.EIGHT_VOXEL_POLE.get(state.getValue(AXIS));
	}

	public BlockState toRegularPipe(IWorld world, BlockPos pos, BlockState state) {
		Direction side = Direction.get(AxisDirection.POSITIVE, state.getValue(AXIS));
		Map<Direction, BooleanProperty> facingToPropertyMap = FluidPipeBlock.PROPERTY_BY_DIRECTION;
		return AllBlocks.FLUID_PIPE.get()
			.updateBlockState(AllBlocks.FLUID_PIPE.getDefaultState()
				.setValue(facingToPropertyMap.get(side), true)
				.setValue(facingToPropertyMap.get(side.getOpposite()), true), side, null, world, pos);
	}

	@Override
	public Axis getAxis(BlockState state) {
		return state.getValue(AXIS);
	}

	@Override
	public Optional<ItemStack> removeBracket(IBlockReader world, BlockPos pos, boolean inOnReplacedContext) {
		BracketedTileEntityBehaviour behaviour = TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (behaviour == null)
			return Optional.empty();
		BlockState bracket = behaviour.getBracket();
		behaviour.removeBracket(inOnReplacedContext);
		if (bracket == Blocks.AIR.defaultBlockState())
			return Optional.empty();
		return Optional.of(new ItemStack(bracket.getBlock()));
	}

}
