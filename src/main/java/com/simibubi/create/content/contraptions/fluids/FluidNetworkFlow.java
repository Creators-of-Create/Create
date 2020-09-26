package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.fluids.FluidStack;

class FluidNetworkFlow {

	@FunctionalInterface
	static interface PipeFlowConsumer {
		void accept(FluidPipeBehaviour pipe, Direction face, boolean inbound);
	}

	/**
	 * 
	 */
	private final FluidNetwork activePipeNetwork;
	FluidNetworkEndpoint source;
	FluidStack fluidStack;
	Set<BlockFace> flowPointers;

	Set<FluidNetworkEndpoint> outputEndpoints;
	boolean pumpReached;

	boolean pulling;
	float speed;

	public FluidNetworkFlow(FluidNetwork activePipeNetwork, FluidNetworkEndpoint source, IWorld world,
		boolean pulling) {
		this.activePipeNetwork = activePipeNetwork;
		this.source = source;
		this.pulling = pulling;
		flowPointers = new HashSet<>();
		outputEndpoints = new HashSet<>();
		fluidStack = FluidStack.EMPTY;
		tick(world, 0);
	}

	void resetFlow(IWorld world) {
		fluidStack = FluidStack.EMPTY;
		flowPointers.clear();
		outputEndpoints.clear();
		pumpReached = false;
		forEachPipeFlow(world, (pipe, face, inbound) -> pipe.removeFlow(this, face, inbound));
	}

	void addToSkippedConnections(IWorld world) {
		forEachPipeFlow(world, (pipe, face, inbound) -> {
			if (!pipe.getFluid().isFluidEqual(fluidStack))
				return;
			BlockFace blockFace = new BlockFace(pipe.getPos(), face);
			this.activePipeNetwork.previousFlow.put(blockFace, pipe.getFluid());
		});
	}

	void forEachPipeFlow(IWorld world, FluidNetworkFlow.PipeFlowConsumer consumer) {
		Set<BlockFace> flowPointers = new HashSet<>();
		flowPointers.add(getSource());

		// Update all branches of this flow, and create new ones if necessary
		while (!flowPointers.isEmpty()) {
			List<BlockFace> toAdd = new ArrayList<>();
			for (Iterator<BlockFace> iterator = flowPointers.iterator(); iterator.hasNext();) {
				BlockFace flowPointer = iterator.next();
				BlockPos currentPos = flowPointer.getPos();
				FluidPipeBehaviour pipe = getPipeInTree(world, currentPos);
				if (pipe == null) {
					iterator.remove();
					continue;
				}
				Map<Direction, Boolean> directions = this.activePipeNetwork.pipeGraph.get(currentPos)
					.getSecond();
				for (Entry<Direction, Boolean> entry : directions.entrySet()) {
					boolean inbound = entry.getValue() != pulling;
					Direction face = entry.getKey();
					if (inbound && face != flowPointer.getFace())
						continue;
					consumer.accept(pipe, face, inbound);
					if (inbound)
						continue;
					toAdd.add(new BlockFace(currentPos.offset(face), face.getOpposite()));
				}
				iterator.remove();
			}
			flowPointers.addAll(toAdd);
		}
	}

	void tick(IWorld world, float speed) {
		boolean skipping = speed == 0;
		Map<BlockFace, FluidStack> previousFlow = this.activePipeNetwork.previousFlow;
		if (skipping && previousFlow.isEmpty())
			return;

		this.speed = speed;
		FluidStack provideFluid = source.provideFluid();
		if (!fluidStack.isEmpty() && !fluidStack.isFluidEqual(provideFluid)) {
			resetFlow(world);
			return;
		}

		fluidStack = provideFluid;

		// There is currently no unfinished flow being followed
		if (flowPointers.isEmpty()) {

			// The fluid source has run out -> reset
			if (fluidStack.isEmpty()) {
				if (hasValidTargets())
					resetFlow(world);
				return;
			}

			// Keep the flows if all is well
			if (hasValidTargets())
				return;

			// Start a new flow from or towards the pump
			BlockFace source = getSource();
			if (tryConnectTo(world, source.getOpposite()))
				return;
			flowPointers.add(source);
		}

		boolean skipped = false;
		Set<BlockFace> pausedPointers = new HashSet<>();

		do {
			skipped = false;
			List<BlockFace> toAdd = null;

			// Update all branches of this flow, and create new ones if necessary
			for (Iterator<BlockFace> iterator = flowPointers.iterator(); iterator.hasNext();) {
				BlockFace flowPointer = iterator.next();
				BlockPos currentPos = flowPointer.getPos();

				if (pausedPointers.contains(flowPointer))
					continue;

				FluidPipeBehaviour pipe = getPipeInTree(world, currentPos);
				if (pipe == null) {
					iterator.remove();
					continue;
				}

				Map<Direction, Boolean> directions = this.activePipeNetwork.pipeGraph.get(currentPos)
					.getSecond();
				boolean inboundComplete = false;
				boolean allFlowsComplete = true;
				BlockState state = world.getBlockState(currentPos);

				// First loop only inbound flows of a pipe to see if they have reached the
				// center
				for (boolean inboundPass : Iterate.trueAndFalse) {
					if (!inboundPass && !inboundComplete)
						break;

					// For all connections of the pipe tree of the pump
					for (Entry<Direction, Boolean> entry : directions.entrySet()) {
						Boolean awayFromPump = entry.getValue();
						Direction direction = entry.getKey();
						boolean inbound = awayFromPump != pulling;

						if (inboundPass && direction != flowPointer.getFace())
							continue;
						if (!inboundPass && inbound)
							continue;
						if (!pipe.canTransferToward(fluidStack, state, direction, inbound))
							continue;

						BlockFace blockface = new BlockFace(currentPos, direction);

						if (!pipe.hasStartedFlow(this, direction, inbound))
							pipe.addFlow(this, direction, inbound, false);
						if (skipping && canSkip(previousFlow, blockface)) {
							pipe.skipFlow(direction, inbound);
							FluidPropagator.showBlockFace(blockface)
								.colored(0x0)
								.lineWidth(1 / 8f);
							skipped = true;
						}

						if (!pipe.hasCompletedFlow(direction, inbound)) {
							allFlowsComplete = false;
							continue;
						}

						if (inboundPass) {
							inboundComplete = true;
							continue;
						}

						// Outward pass, check if any target was reached
						tryConnectTo(world, blockface);
					}
				}

				if (!allFlowsComplete && !skipping)
					continue;

				// Create a new flow branch at each outward pipe connection
				for (Entry<Direction, Boolean> entry : directions.entrySet()) {
					if (entry.getValue() != pulling)
						continue;
					Direction face = entry.getKey();
					if (!pipe.canTransferToward(fluidStack, state, face, false))
						continue;
					BlockFace addedBlockFace = new BlockFace(currentPos.offset(face), face.getOpposite());
					if (skipping && !canSkip(previousFlow, addedBlockFace)) {
						allFlowsComplete = false;
						continue;
					}
					if (toAdd == null)
						toAdd = new ArrayList<>();
					toAdd.add(addedBlockFace);
				}

				if (!allFlowsComplete && skipping) {
					pausedPointers.add(flowPointer);
					continue;
				}

				iterator.remove();

			} // End of branch loop

			if (toAdd != null)
				flowPointers.addAll(toAdd);

		} while (skipping && skipped);
	}

	private boolean canSkip(Map<BlockFace, FluidStack> previousFlow, BlockFace blockface) {
		return previousFlow.containsKey(blockface) && previousFlow.get(blockface)
			.isFluidEqual(fluidStack);
	}

	private boolean tryConnectTo(IWorld world, BlockFace blockface) {
		// Pulling flow, target is the pump
		if (pulling) {
			if (!this.activePipeNetwork.pumpLocation.getOpposite()
				.equals(blockface))
				return false;
			pumpReached = true;
			TileEntity targetTE = world.getTileEntity(this.activePipeNetwork.pumpLocation.getPos());
			if (targetTE instanceof PumpTileEntity)
				((PumpTileEntity) targetTE).setProvidedFluid(fluidStack);
			FluidPropagator.showBlockFace(this.activePipeNetwork.pumpLocation)
				.colored(0x799351)
				.lineWidth(1 / 8f);
			return true;
		}

		// Pushing flow, targets are the endpoints
		for (FluidNetworkEndpoint networkEndpoint : this.activePipeNetwork.targets) {
			if (!networkEndpoint.location.isEquivalent(blockface))
				continue;
			outputEndpoints.add(networkEndpoint);
			FluidPropagator.showBlockFace(blockface)
				.colored(0x799351)
				.lineWidth(1 / 8f);
			return !(networkEndpoint instanceof InterPumpEndpoint);
		}

		return false;
	}

	private BlockFace getSource() {
		return pulling ? source.location : this.activePipeNetwork.pumpLocation.getOpposite();
	}

	private FluidPipeBehaviour getPipeInTree(IWorld world, BlockPos currentPos) {
		if (!world.isAreaLoaded(currentPos, 0))
			return null;
		if (!this.activePipeNetwork.pipeGraph.containsKey(currentPos))
			return null;
		return TileEntityBehaviour.get(world, currentPos, FluidPipeBehaviour.TYPE);
	}

	boolean hasValidTargets() {
		return pumpReached || !outputEndpoints.isEmpty();
	}

	public float getSpeed() {
		return speed;
	}

	public FluidStack getFluidStack() {
		return fluidStack;
	}
}