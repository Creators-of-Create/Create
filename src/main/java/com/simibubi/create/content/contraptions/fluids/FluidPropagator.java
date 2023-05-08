package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.fluids.PipeConnection.Flow;
import com.simibubi.create.content.contraptions.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.VanillaFluidTargets;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidPropagator {

	public static CreateAdvancement[] getSharedTriggers() {
		return new CreateAdvancement[] { AllAdvancements.WATER_SUPPLY, AllAdvancements.CROSS_STREAMS,
			AllAdvancements.HONEY_DRAIN };
	}

	public static void propagateChangedPipe(LevelAccessor world, BlockPos pipePos, BlockState pipeState) {
		List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		Set<Pair<PumpBlockEntity, Direction>> discoveredPumps = new HashSet<>();

		frontier.add(Pair.of(0, pipePos));

		// Visit all connected pumps to update their network
		while (!frontier.isEmpty()) {
			Pair<Integer, BlockPos> pair = frontier.remove(0);
			BlockPos currentPos = pair.getSecond();
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);
			BlockState currentState = currentPos.equals(pipePos) ? pipeState : world.getBlockState(currentPos);
			FluidTransportBehaviour pipe = getPipe(world, currentPos);
			if (pipe == null)
				continue;
			pipe.wipePressure();

			for (Direction direction : getPipeConnections(currentState, pipe)) {
				BlockPos target = currentPos.relative(direction);
				if (world instanceof Level l && !l.isLoaded(target))
					continue;

				BlockEntity blockEntity = world.getBlockEntity(target);
				BlockState targetState = world.getBlockState(target);
				if (blockEntity instanceof PumpBlockEntity) {
					if (!AllBlocks.MECHANICAL_PUMP.has(targetState) || targetState.getValue(PumpBlock.FACING)
						.getAxis() != direction.getAxis())
						continue;
					discoveredPumps.add(Pair.of((PumpBlockEntity) blockEntity, direction.getOpposite()));
					continue;
				}
				if (visited.contains(target))
					continue;
				FluidTransportBehaviour targetPipe = getPipe(world, target);
				if (targetPipe == null)
					continue;
				Integer distance = pair.getFirst();
				if (distance >= getPumpRange() && !targetPipe.hasAnyPressure())
					continue;
				if (targetPipe.canHaveFlowToward(targetState, direction.getOpposite()))
					frontier.add(Pair.of(distance + 1, target));
			}
		}

		discoveredPumps.forEach(pair -> pair.getFirst()
			.updatePipesOnSide(pair.getSecond()));
	}

	public static void resetAffectedFluidNetworks(Level world, BlockPos start, Direction side) {
		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		frontier.add(start);

		while (!frontier.isEmpty()) {
			BlockPos pos = frontier.remove(0);
			if (visited.contains(pos))
				continue;
			visited.add(pos);
			FluidTransportBehaviour pipe = getPipe(world, pos);
			if (pipe == null)
				continue;

			for (Direction d : Iterate.directions) {
				if (pos.equals(start) && d != side)
					continue;
				BlockPos target = pos.relative(d);
				if (visited.contains(target))
					continue;

				PipeConnection connection = pipe.getConnection(d);
				if (connection == null)
					continue;
				if (!connection.hasFlow())
					continue;

				Flow flow = connection.flow.get();
				if (!flow.inbound)
					continue;

				connection.resetNetwork();
				frontier.add(target);
			}
		}
	}

	public static Direction validateNeighbourChange(BlockState state, Level world, BlockPos pos, Block otherBlock,
		BlockPos neighborPos, boolean isMoving) {
		if (world.isClientSide)
			return null;
		// calling getblockstate() as otherBlock param seems to contain the block which
		// was replaced
		otherBlock = world.getBlockState(neighborPos)
			.getBlock();
		if (otherBlock instanceof FluidPipeBlock)
			return null;
		if (otherBlock instanceof AxisPipeBlock)
			return null;
		if (otherBlock instanceof PumpBlock)
			return null;
		if (otherBlock instanceof LiquidBlock)
			return null;
		if (getStraightPipeAxis(state) == null && !AllBlocks.ENCASED_FLUID_PIPE.has(state))
			return null;
		for (Direction d : Iterate.directions) {
			if (!pos.relative(d)
				.equals(neighborPos))
				continue;
			return d;
		}
		return null;
	}

	public static FluidTransportBehaviour getPipe(BlockGetter reader, BlockPos pos) {
		return BlockEntityBehaviour.get(reader, pos, FluidTransportBehaviour.TYPE);
	}

	public static boolean isOpenEnd(BlockGetter reader, BlockPos pos, Direction side) {
		BlockPos connectedPos = pos.relative(side);
		BlockState connectedState = reader.getBlockState(connectedPos);
		FluidTransportBehaviour pipe = FluidPropagator.getPipe(reader, connectedPos);
		if (pipe != null && pipe.canHaveFlowToward(connectedState, side.getOpposite()))
			return false;
		if (PumpBlock.isPump(connectedState) && connectedState.getValue(PumpBlock.FACING)
			.getAxis() == side.getAxis())
			return false;
		if (VanillaFluidTargets.shouldPipesConnectTo(connectedState))
			return true;
		if (BlockHelper.hasBlockSolidSide(connectedState, reader, connectedPos, side.getOpposite())
			&& !AllBlockTags.FAN_TRANSPARENT.matches(connectedState))
			return false;
		if (hasFluidCapability(reader, connectedPos, side.getOpposite()))
			return false;
		if (!(connectedState.getMaterial()
			.isReplaceable() && connectedState.getDestroySpeed(reader, connectedPos) != -1)
			&& !connectedState.hasProperty(BlockStateProperties.WATERLOGGED))
			return false;
		return true;
	}

	public static List<Direction> getPipeConnections(BlockState state, FluidTransportBehaviour pipe) {
		List<Direction> list = new ArrayList<>();
		for (Direction d : Iterate.directions)
			if (pipe.canHaveFlowToward(state, d))
				list.add(d);
		return list;
	}

	public static int getPumpRange() {
		return AllConfigs.server().fluids.mechanicalPumpRange.get();
	}

	public static boolean hasFluidCapability(BlockGetter world, BlockPos pos, Direction side) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null)
			return false;
		LazyOptional<IFluidHandler> capability =
			blockEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
		return capability.isPresent();
	}

	@Nullable
	public static Axis getStraightPipeAxis(BlockState state) {
		if (state.getBlock() instanceof PumpBlock)
			return state.getValue(PumpBlock.FACING)
				.getAxis();
		if (state.getBlock() instanceof AxisPipeBlock)
			return state.getValue(AxisPipeBlock.AXIS);
		if (!FluidPipeBlock.isPipe(state))
			return null;
		Axis axisFound = null;
		int connections = 0;
		for (Axis axis : Iterate.axes) {
			Direction d1 = Direction.get(AxisDirection.NEGATIVE, axis);
			Direction d2 = Direction.get(AxisDirection.POSITIVE, axis);
			boolean openAt1 = FluidPipeBlock.isOpenAt(state, d1);
			boolean openAt2 = FluidPipeBlock.isOpenAt(state, d2);
			if (openAt1)
				connections++;
			if (openAt2)
				connections++;
			if (openAt1 && openAt2)
				if (axisFound != null)
					return null;
				else
					axisFound = axis;
		}
		return connections == 2 ? axisFound : null;
	}

}
