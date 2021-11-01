package com.simibubi.create.content.contraptions.fluids.pipes;

import static net.minecraft.state.properties.BlockStateProperties.DOWN;
import staticnet.minecraft.world.level.block.state.properties.BlockStatePropertiess.EAST;
import static net.minecraft.state.properties.BlockStateProperties.NORTH;
import staticnet.minecraft.world.level.block.state.properties.BlockStatePropertiess.SOUTH;
import static net.minecraft.state.properties.BlockStateProperties.UP;
import staticnet.minecraft.world.level.block.state.properties.BlockStatePropertiess.WEST;

import java.util.Map;
import java.util.Random;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.contraptions.wrench.IWrenchable;
import com.simibubi.create.content.schematics.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.ItemRequirement;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class EncasedPipeBlock extends Block implements IWrenchable, ISpecialBlockItemRequirement {

	public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = PipeBlock.PROPERTY_BY_DIRECTION;

	public EncasedPipeBlock(Properties p_i48339_1_) {
		super(p_i48339_1_);
		registerDefaultState(defaultBlockState().setValue(NORTH, false)
			.setValue(SOUTH, false)
			.setValue(DOWN, false)
			.setValue(UP, false)
			.setValue(WEST, false)
			.setValue(EAST, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockTypeChanged = state.getBlock() != newState.getBlock();
		if (blockTypeChanged && !world.isClientSide)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.removeBlockEntity(pos);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!world.isClientSide && state != oldState)
			world.getBlockTicks()
				.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos,
		Player player) {
		return AllBlocks.FLUID_PIPE.asStack();
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugPackets.sendNeighborsUpdatePacket(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!state.getValue(FACING_TO_PROPERTY_MAP.get(d)))
			return;
		world.getBlockTicks()
			.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
		return AllTileEntities.ENCASED_FLUID_PIPE.create();
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();

		if (world.isClientSide)
			return InteractionResult.SUCCESS;

		context.getLevel()
			.levelEvent(2001, context.getClickedPos(), Block.getId(state));
		BlockState equivalentPipe = transferSixWayProperties(state, AllBlocks.FLUID_PIPE.getDefaultState());

		Direction firstFound = Direction.UP;
		for (Direction d : Iterate.directions)
			if (state.getValue(FACING_TO_PROPERTY_MAP.get(d))) {
				firstFound = d;
				break;
			}

		FluidTransportBehaviour.cacheFlows(world, pos);
		world.setBlockAndUpdate(pos, AllBlocks.FLUID_PIPE.get()
			.updateBlockState(equivalentPipe, firstFound, null, world, pos));
		FluidTransportBehaviour.loadFlows(world, pos);
		return InteractionResult.SUCCESS;
	}

	public static BlockState transferSixWayProperties(BlockState from, BlockState to) {
		for (Direction d : Iterate.directions) {
			BooleanProperty property = FACING_TO_PROPERTY_MAP.get(d);
			to = to.setValue(property, from.getValue(property));
		}
		return to;
	}

	@Override
	public ItemRequirement getRequiredItems(BlockState state, BlockEntity te) {
		return ItemRequirement.of(AllBlocks.FLUID_PIPE.getDefaultState(), te);
	}

}
