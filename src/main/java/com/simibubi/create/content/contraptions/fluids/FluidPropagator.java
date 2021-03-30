package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.PipeConnection.Flow;
import com.simibubi.create.content.contraptions.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class FluidPropagator {

	public static void propagateChangedPipe(IWorld world, BlockPos pipePos, BlockState pipeState) {
		List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		Set<Pair<PumpTileEntity, Direction>> discoveredPumps = new HashSet<>();

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
				BlockPos target = currentPos.offset(direction);
				if (!world.isAreaLoaded(target, 0))
					continue;

				TileEntity tileEntity = world.getTileEntity(target);
				BlockState targetState = world.getBlockState(target);
				if (tileEntity instanceof PumpTileEntity) {
					if (!AllBlocks.MECHANICAL_PUMP.has(targetState) || targetState.get(PumpBlock.FACING)
						.getAxis() != direction.getAxis())
						continue;
					discoveredPumps.add(Pair.of((PumpTileEntity) tileEntity, direction.getOpposite()));
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

	public static void resetAffectedFluidNetworks(World world, BlockPos start, Direction side) {
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
				BlockPos target = pos.offset(d);
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

	public static Direction validateNeighbourChange(BlockState state, World world, BlockPos pos, Block otherBlock,
		BlockPos neighborPos, boolean isMoving) {
		if (world.isRemote)
			return null;
		if (otherBlock instanceof FluidPipeBlock)
			return null;
		if (otherBlock instanceof AxisPipeBlock)
			return null;
		if (otherBlock instanceof PumpBlock)
			return null;
		if (otherBlock instanceof FlowingFluidBlock)
			return null;
		if (getStraightPipeAxis(state) == null)
			return null;
		for (Direction d : Iterate.directions) {
			if (!pos.offset(d)
				.equals(neighborPos))
				continue;
			return d;
		}
		return null;
	}

	public static FluidTransportBehaviour getPipe(IBlockReader reader, BlockPos pos) {
		return TileEntityBehaviour.get(reader, pos, FluidTransportBehaviour.TYPE);
	}

	public static boolean isOpenEnd(IBlockReader reader, BlockPos pos, Direction side) {
		BlockPos connectedPos = pos.offset(side);
		BlockState connectedState = reader.getBlockState(connectedPos);
		FluidTransportBehaviour pipe = FluidPropagator.getPipe(reader, connectedPos);
		if (pipe != null && pipe.canHaveFlowToward(connectedState, side.getOpposite()))
			return false;
		if (PumpBlock.isPump(connectedState) && connectedState.get(PumpBlock.FACING)
			.getAxis() == side.getAxis())
			return false;
		if (connectedState.has(BlockStateProperties.HONEY_LEVEL))
			return true;
		if (Block.hasSolidSide(connectedState, reader, connectedPos, side.getOpposite()))
			return false;
		if (!(connectedState.getMaterial()
			.isReplaceable() && connectedState.getBlockHardness(reader, connectedPos) != -1)
			&& !connectedState.has(BlockStateProperties.WATERLOGGED))
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
		return AllConfigs.SERVER.fluids.mechanicalPumpRange.get();
	}

//	static AxisAlignedBB smallCenter = new AxisAlignedBB(BlockPos.ZERO).shrink(.25);
//	
//	@Deprecated 
//	public static OutlineParams showBlockFace(BlockFace face) {
//		MutableObject<OutlineParams> params = new MutableObject<>(new OutlineParams());
//		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
//			Vec3d directionVec = new Vec3d(face.getFace()
//				.getDirectionVec());
//			Vec3d scaleVec = directionVec.scale(-.25f * face.getFace()
//				.getAxisDirection()
//				.getOffset());
//			directionVec = directionVec.scale(.45f);
//			params.setValue(CreateClient.outliner.showAABB(face,
//				FluidPropagator.smallCenter.offset(directionVec.add(new Vec3d(face.getPos())))
//					.grow(scaleVec.x, scaleVec.y, scaleVec.z)
//					.grow(1 / 16f)));
//		});
//		return params.getValue()
//			.lineWidth(1 / 16f);
//	}

	public static boolean hasFluidCapability(IBlockReader world, BlockPos pos, Direction side) {
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity != null && tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)
			.isPresent();
	}

	@Nullable
	public static Axis getStraightPipeAxis(BlockState state) {
		if (state.getBlock() instanceof PumpBlock)
			return state.get(PumpBlock.FACING)
				.getAxis();
		if (state.getBlock() instanceof AxisPipeBlock)
			return state.get(AxisPipeBlock.AXIS);
		if (!FluidPipeBlock.isPipe(state))
			return null;
		Axis axisFound = null;
		int connections = 0;
		for (Axis axis : Iterate.axes) {
			Direction d1 = Direction.getFacingFromAxis(AxisDirection.NEGATIVE, axis);
			Direction d2 = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
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
