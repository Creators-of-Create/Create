package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.outliner.Outline.OutlineParams;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.DistExecutor;

public class FluidPropagator {

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
		if (!isStraightPipe(state))
			return null;
		for (Direction d : Iterate.directions) {
			if (!pos.offset(d)
				.equals(neighborPos))
				continue;
			return d;
		}
		return null;
	}

	public static FluidPipeBehaviour getPipe(IBlockReader reader, BlockPos pos) {
		return TileEntityBehaviour.get(reader, pos, FluidPipeBehaviour.TYPE);
	}

	public static boolean isOpenEnd(IBlockReader reader, BlockPos pos, Direction side) {
		BlockPos connectedPos = pos.offset(side);
		BlockState connectedState = reader.getBlockState(connectedPos);
		FluidPipeBehaviour pipe = FluidPropagator.getPipe(reader, connectedPos);
		if (pipe != null && pipe.isConnectedTo(connectedState, side.getOpposite()))
			return false;
		if (PumpBlock.isPump(connectedState) && connectedState.get(PumpBlock.FACING)
			.getAxis() == side.getAxis())
			return false;
		if (Block.hasSolidSide(connectedState, reader, connectedPos, side.getOpposite()))
			return false;
		if (!(connectedState.getMaterial()
			.isReplaceable() && connectedState.getBlockHardness(reader, connectedPos) != -1)
			&& !connectedState.has(BlockStateProperties.WATERLOGGED))
			return false;
		return true;
	}

	public static void propagateChangedPipe(IWorld world, BlockPos pipePos, BlockState pipeState) {
		List<BlockPos> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();

		frontier.add(pipePos);

		// Visit all connected pumps to update their network
		while (!frontier.isEmpty()) {
			BlockPos currentPos = frontier.remove(0);
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);
			BlockState currentState = currentPos.equals(pipePos) ? pipeState : world.getBlockState(currentPos);
			FluidPipeBehaviour pipe = getPipe(world, currentPos);
			if (pipe == null)
				continue;
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
					PumpTileEntity pump = (PumpTileEntity) tileEntity;
					pump.updatePipesOnSide(direction.getOpposite());
					continue;
				}
				if (visited.contains(target))
					continue;
				FluidPipeBehaviour targetPipe = getPipe(world, target);
				if (targetPipe == null)
					continue;
				if (targetPipe.isConnectedTo(targetState, direction.getOpposite()))
					frontier.add(target);
			}
		}
	}

	public static List<Direction> getPipeConnections(BlockState state, FluidPipeBehaviour pipe) {
		List<Direction> list = new ArrayList<>();
		for (Direction d : Iterate.directions)
			if (pipe.isConnectedTo(state, d))
				list.add(d);
		return list;
	}

	public static int getPumpRange() {
		return AllConfigs.SERVER.fluids.mechanicalPumpRange.get();
	}

	public static OutlineParams showBlockFace(BlockFace face) {
		MutableObject<OutlineParams> params = new MutableObject<>(new OutlineParams());
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			Vec3d directionVec = new Vec3d(face.getFace()
				.getDirectionVec());
			Vec3d scaleVec = directionVec.scale(-.25f * face.getFace()
				.getAxisDirection()
				.getOffset());
			directionVec = directionVec.scale(.5f);
			params.setValue(CreateClient.outliner.showAABB(face,
				FluidPropagator.smallCenter.offset(directionVec.add(new Vec3d(face.getPos())))
					.grow(scaleVec.x, scaleVec.y, scaleVec.z)
					.grow(1 / 16f)));
		});
		return params.getValue();
	}

	static AxisAlignedBB smallCenter = new AxisAlignedBB(BlockPos.ZERO).shrink(.25);

	public static boolean hasFluidCapability(BlockState state, IBlockReader world, BlockPos pos, Direction blockFace) {
		if (!state.hasTileEntity())
			return false;
		TileEntity tileEntity = world.getTileEntity(pos);
		return tileEntity != null
			&& tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, blockFace.getOpposite())
				.isPresent();
	}

	public static boolean isStraightPipe(BlockState state) {
		if (state.getBlock() instanceof AxisPipeBlock)
			return true;
		if (!FluidPipeBlock.isPipe(state))
			return false;
		boolean axisFound = false;
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
				if (axisFound)
					return false;
				else
					axisFound = true;
		}
		return axisFound && connections == 2;
	}

}
