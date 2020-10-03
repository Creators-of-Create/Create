package com.simibubi.create.content.contraptions.fluids;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidNetwork {

	BlockFace pumpLocation;
	Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph;
	List<FluidNetworkFlow> flows;
	Set<FluidNetworkEndpoint> targets;
	Set<BlockFace> rangeEndpoints;
	Map<BlockFace, FluidStack> previousFlow;

	boolean connectToPumps;
	int waitForUnloadedNetwork;

	public FluidNetwork() {
		pipeGraph = new HashMap<>();
		flows = new ArrayList<>();
		targets = new HashSet<>();
		rangeEndpoints = new HashSet<>();
		previousFlow = new HashMap<>();
	}

	public boolean hasEndpoints() {
		for (FluidNetworkFlow pipeFlow : flows)
			if (pipeFlow.hasValidTargets())
				return true;
		return false;
	}

	public Collection<FluidNetworkEndpoint> getEndpoints(boolean pulling) {
		if (!pulling) {
			for (FluidNetworkFlow pipeFlow : flows)
				return pipeFlow.outputEndpoints;
			return Collections.emptySet();
		}

		List<FluidNetworkEndpoint> list = new ArrayList<>();
		for (FluidNetworkFlow pipeFlow : flows) {
			if (!pipeFlow.hasValidTargets())
				continue;
			list.add(pipeFlow.source);
		}
		return list;
	}

	public void tick(IWorld world, PumpTileEntity pumpTE) {
		if (connectToPumps) {
			connectToOtherFNs(world, pumpTE);
			connectToPumps = false;
		}
	}

	public void tickFlows(IWorld world, PumpTileEntity pumpTE, boolean pulling, float speed) {
		if (connectToPumps)
			return;
		initFlows(pumpTE, pulling);
		previousFlow.clear();
		flows.forEach(ep -> ep.tick(world, speed));
	}

	private void initFlows(PumpTileEntity pumpTE, boolean pulling) {
		if (targets.isEmpty())
			return;
		if (!flows.isEmpty())
			return;
		World world = pumpTE.getWorld();
		if (pulling) {
			targets.forEach(ne -> flows.add(new FluidNetworkFlow(this, ne, world, pulling)));
		} else {
			PumpEndpoint pumpEndpoint = new PumpEndpoint(pumpLocation.getOpposite(), pumpTE);
			flows.add(new FluidNetworkFlow(this, pumpEndpoint, world, pulling));
		}
	}

	public void connectToOtherFNs(IWorld world, PumpTileEntity pump) {
		List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
		Set<BlockPos> visited = new HashSet<>();
		int maxDistance = FluidPropagator.getPumpRange() * 2;
		frontier.add(Pair.of(-1, pumpLocation.getPos()));

		while (!frontier.isEmpty()) {
			Pair<Integer, BlockPos> entry = frontier.remove(0);
			int distance = entry.getFirst();
			BlockPos currentPos = entry.getSecond();

			if (!world.isAreaLoaded(currentPos, 0))
				continue;
			if (visited.contains(currentPos))
				continue;
			visited.add(currentPos);

			List<Direction> connections;
			if (currentPos.equals(pumpLocation.getPos())) {
				connections = ImmutableList.of(pumpLocation.getFace());
			} else {
				BlockState currentState = world.getBlockState(currentPos);
				FluidPipeBehaviour pipe = FluidPropagator.getPipe(world, currentPos);
				if (pipe == null)
					continue;
				connections = FluidPropagator.getPipeConnections(currentState, pipe);
			}

			for (Direction face : connections) {
				BlockFace blockFace = new BlockFace(currentPos, face);
				BlockPos connectedPos = blockFace.getConnectedPos();
				BlockState connectedState = world.getBlockState(connectedPos);

				if (connectedPos.equals(pumpLocation.getPos()))
					continue;
				if (!world.isAreaLoaded(connectedPos, 0))
					continue;
				if (PumpBlock.isPump(connectedState) && connectedState.get(PumpBlock.FACING)
					.getAxis() == face.getAxis()) {
					TileEntity tileEntity = world.getTileEntity(connectedPos);
					if (tileEntity instanceof PumpTileEntity) {
						PumpTileEntity otherPump = (PumpTileEntity) tileEntity;
						if (otherPump.networks == null)
							continue;

						otherPump.networks.forEach(fn -> {
							int nearest = Integer.MAX_VALUE;
							BlockFace argNearest = null;
							for (BlockFace pumpEndpoint : fn.rangeEndpoints) {
								if (pumpEndpoint.isEquivalent(pumpLocation)) {
									argNearest = pumpEndpoint;
									break;
								}
								Pair<Integer, Map<Direction, Boolean>> pair =
									pipeGraph.get(pumpEndpoint.getConnectedPos());
								if (pair == null)
									continue;
								Integer distanceFromPump = pair.getFirst();
								Map<Direction, Boolean> pipeConnections = pair.getSecond();

								if (!pipeConnections.containsKey(pumpEndpoint.getOppositeFace()))
									continue;
								if (nearest <= distanceFromPump)
									continue;
								nearest = distanceFromPump;
								argNearest = pumpEndpoint;

							}
							if (argNearest != null) {
								InterPumpEndpoint endpoint = new InterPumpEndpoint(world, argNearest.getOpposite(),
									pump, otherPump, pumpLocation, fn.pumpLocation);
								targets.add(endpoint);
								fn.targets.add(endpoint.opposite(world));
							}
						});

					}
					continue;
				}
				if (visited.contains(connectedPos))
					continue;
				if (distance > maxDistance)
					continue;
				FluidPipeBehaviour targetPipe = FluidPropagator.getPipe(world, connectedPos);
				if (targetPipe == null)
					continue;
				if (targetPipe.isConnectedTo(connectedState, face.getOpposite()))
					frontier.add(Pair.of(distance + 1, connectedPos));
			}
		}

	}

	public void assemble(IWorld world, PumpTileEntity pumpTE, BlockFace pumpLocation) {
		Map<BlockFace, OpenEndedPipe> openEnds = pumpTE.getOpenEnds(pumpLocation.getFace());
		openEnds.values()
			.forEach(OpenEndedPipe::markStale);

		this.pumpLocation = pumpLocation;
		if (!collectEndpoint(world, pumpLocation, openEnds, 0)) {

			List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
			Set<BlockPos> visited = new HashSet<>();
			int maxDistance = FluidPropagator.getPumpRange();
			frontier.add(Pair.of(0, pumpLocation.getConnectedPos()));

			while (!frontier.isEmpty()) {
				Pair<Integer, BlockPos> entry = frontier.remove(0);
				int distance = entry.getFirst();
				BlockPos currentPos = entry.getSecond();

				if (!world.isAreaLoaded(currentPos, 0))
					continue;
				if (visited.contains(currentPos))
					continue;
				visited.add(currentPos);
				BlockState currentState = world.getBlockState(currentPos);
				FluidPipeBehaviour pipe = FluidPropagator.getPipe(world, currentPos);
				if (pipe == null)
					continue;

				for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
					if (!pipe.canTransferToward(FluidStack.EMPTY, world.getBlockState(currentPos), face, false))
						continue;

					BlockFace blockFace = new BlockFace(currentPos, face);
					BlockPos connectedPos = blockFace.getConnectedPos();

					if (connectedPos.equals(pumpLocation.getPos())) {
						addEntry(blockFace.getPos(), blockFace.getFace(), true, distance);
						continue;
					}
					if (!world.isAreaLoaded(connectedPos, 0))
						continue;
					if (collectEndpoint(world, blockFace, openEnds, distance))
						continue;
					FluidPipeBehaviour pipeBehaviour = FluidPropagator.getPipe(world, connectedPos);
					if (pipeBehaviour == null)
						continue;
					if (visited.contains(connectedPos))
						continue;
					if (distance + 1 >= maxDistance) {
						rangeEndpoints.add(blockFace);
						addEntry(currentPos, face, false, distance);
						FluidPropagator.showBlockFace(blockFace)
							.lineWidth(1 / 8f)
							.colored(0xff0000);
						continue;
					}

					addConnection(connectedPos, currentPos, face.getOpposite(), distance);
					frontier.add(Pair.of(distance + 1, connectedPos));
				}
			}
		}

		Set<BlockFace> staleEnds = new HashSet<>();
		openEnds.entrySet()
			.forEach(e -> {
				if (e.getValue()
					.isStale())
					staleEnds.add(e.getKey());
			});
		staleEnds.forEach(openEnds::remove);

		connectToPumps = true;
	}

	private FluidNetworkEndpoint reuseOrCreateOpenEnd(IWorld world, Map<BlockFace, OpenEndedPipe> openEnds,
		BlockFace toCreate) {
		OpenEndedPipe openEndedPipe = null;
		if (openEnds.containsKey(toCreate)) {
			openEndedPipe = openEnds.get(toCreate);
			openEndedPipe.unmarkStale();
		} else {
			openEndedPipe = new OpenEndedPipe(toCreate);
			openEnds.put(toCreate, openEndedPipe);
		}
		return new FluidNetworkEndpoint(world, toCreate, openEndedPipe.getCapability());

	}

	private boolean collectEndpoint(IWorld world, BlockFace blockFace, Map<BlockFace, OpenEndedPipe> openEnds,
		int distance) {
		BlockPos connectedPos = blockFace.getConnectedPos();
		BlockState connectedState = world.getBlockState(connectedPos);

		// other pipe, no endpoint
		FluidPipeBehaviour pipe = FluidPropagator.getPipe(world, connectedPos);
		if (pipe != null && pipe.isConnectedTo(connectedState, blockFace.getOppositeFace()))
			return false;
		TileEntity tileEntity = world.getTileEntity(connectedPos);

		// fluid handler endpoint
		Direction face = blockFace.getFace();
		if (tileEntity != null) {
			LazyOptional<IFluidHandler> capability =
				tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
			if (capability.isPresent()) {
				targets.add(new FluidNetworkEndpoint(world, blockFace, capability));
				addEntry(blockFace.getPos(), face, false, distance);
				FluidPropagator.showBlockFace(blockFace)
					.colored(0x00b7c2)
					.lineWidth(1 / 8f);
				return true;
			}
		}

		// open endpoint
		if (PumpBlock.isPump(connectedState) && connectedState.get(PumpBlock.FACING)
			.getAxis() == face.getAxis()) {
			rangeEndpoints.add(blockFace);
			addEntry(blockFace.getPos(), face, false, distance);
			return true;
		}
		if (!FluidPropagator.isOpenEnd(world, blockFace.getPos(), face))
			return false;

		targets.add(reuseOrCreateOpenEnd(world, openEnds, blockFace));
		addEntry(blockFace.getPos(), face, false, distance);
		FluidPropagator.showBlockFace(blockFace)
			.colored(0xb700c2)
			.lineWidth(1 / 8f);
		return true;
	}

	private void addConnection(BlockPos from, BlockPos to, Direction direction, int distance) {
		addEntry(from, direction, true, distance);
		addEntry(to, direction.getOpposite(), false, distance + 1);
	}

	private void addEntry(BlockPos pos, Direction direction, boolean outbound, int distance) {
		if (!pipeGraph.containsKey(pos))
			pipeGraph.put(pos, Pair.of(distance, new HashMap<>()));
		pipeGraph.get(pos)
			.getSecond()
			.put(direction, outbound);
	}

	public void reAssemble(IWorld world, PumpTileEntity pumpTE, BlockFace pumpLocation) {
		rangeEndpoints.clear();
		targets.clear();
		pipeGraph.clear();
		assemble(world, pumpTE, pumpLocation);
	}

	public void remove(IWorld world) {
		clearFlows(world, false);
	}

	public void clearFlows(IWorld world, boolean saveState) {
		for (FluidNetworkFlow networkFlow : flows) {
			if (!networkFlow.getFluidStack()
				.isEmpty())
				networkFlow.addToSkippedConnections(world);
			networkFlow.resetFlow(world);
		}
		flows.clear();
	}

}
