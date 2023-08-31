package com.simibubi.create.content.fluids.pump;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.BlockFace;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class PumpBlockEntity extends KineticBlockEntity {

	Couple<MutableBoolean> sidesToUpdate;
	boolean pressureUpdate;

	// Backcompat- flips any pump blockstate that loads with reversed=true
	boolean scheduleFlip;

	public PumpBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		sidesToUpdate = Couple.create(MutableBoolean::new);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		behaviours.add(new PumpFluidTransferBehaviour(this));
		registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
		registerAwardables(behaviours, AllAdvancements.PUMP);
	}

	@Override
	public void tick() {
		super.tick();

		if (level.isClientSide && !isVirtual())
			return;

		if (scheduleFlip) {
			level.setBlockAndUpdate(worldPosition,
				getBlockState().setValue(PumpBlock.FACING, getBlockState().getValue(PumpBlock.FACING)
					.getOpposite()));
			scheduleFlip = false;
		}

		sidesToUpdate.forEachWithContext((update, isFront) -> {
			if (update.isFalse())
				return;
			update.setFalse();
			distributePressureTo(isFront ? getFront() : getFront().getOpposite());
		});
	}

	@Override
	public void onSpeedChanged(float previousSpeed) {
		super.onSpeedChanged(previousSpeed);

		if (Math.abs(previousSpeed) == Math.abs(getSpeed()))
			return;
		if (speed != 0)
			award(AllAdvancements.PUMP);
		if (level.isClientSide && !isVirtual())
			return;

		updatePressureChange();
	}

	public void updatePressureChange() {
		pressureUpdate = false;
		BlockPos frontPos = worldPosition.relative(getFront());
		BlockPos backPos = worldPosition.relative(getFront().getOpposite());
		FluidPropagator.propagateChangedPipe(level, frontPos, level.getBlockState(frontPos));
		FluidPropagator.propagateChangedPipe(level, backPos, level.getBlockState(backPos));

		FluidTransportBehaviour behaviour = getBehaviour(FluidTransportBehaviour.TYPE);
		if (behaviour != null)
			behaviour.wipePressure();
		sidesToUpdate.forEach(MutableBoolean::setTrue);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		if (compound.getBoolean("Reversed"))
			scheduleFlip = true;
	}

	protected void distributePressureTo(Direction side) {
		if (getSpeed() == 0)
			return;

		BlockFace start = new BlockFace(worldPosition, side);
		boolean pull = isPullingOnSide(isFront(side));
		Set<BlockFace> targets = new HashSet<>();
		Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();

		if (!pull)
			FluidPropagator.resetAffectedFluidNetworks(level, worldPosition, side.getOpposite());

		if (!hasReachedValidEndpoint(level, start, pull)) {

			pipeGraph.computeIfAbsent(worldPosition, $ -> Pair.of(0, new IdentityHashMap<>()))
				.getSecond()
				.put(side, pull);
			pipeGraph.computeIfAbsent(start.getConnectedPos(), $ -> Pair.of(1, new IdentityHashMap<>()))
				.getSecond()
				.put(side.getOpposite(), !pull);

			List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
			Set<BlockPos> visited = new HashSet<>();
			int maxDistance = FluidPropagator.getPumpRange();
			frontier.add(Pair.of(1, start.getConnectedPos()));

			while (!frontier.isEmpty()) {
				Pair<Integer, BlockPos> entry = frontier.remove(0);
				int distance = entry.getFirst();
				BlockPos currentPos = entry.getSecond();

				if (!level.isLoaded(currentPos))
					continue;
				if (visited.contains(currentPos))
					continue;
				visited.add(currentPos);
				BlockState currentState = level.getBlockState(currentPos);
				FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, currentPos);
				if (pipe == null)
					continue;

				for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
					BlockFace blockFace = new BlockFace(currentPos, face);
					BlockPos connectedPos = blockFace.getConnectedPos();

					if (!level.isLoaded(connectedPos))
						continue;
					if (blockFace.isEquivalent(start))
						continue;
					if (hasReachedValidEndpoint(level, blockFace, pull)) {
						pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
							.getSecond()
							.put(face, pull);
						targets.add(blockFace);
						continue;
					}

					FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, connectedPos);
					if (pipeBehaviour == null)
						continue;
					if (pipeBehaviour instanceof PumpFluidTransferBehaviour)
						continue;
					if (visited.contains(connectedPos))
						continue;
					if (distance + 1 >= maxDistance) {
						pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
							.getSecond()
							.put(face, pull);
						targets.add(blockFace);
						continue;
					}

					pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
						.getSecond()
						.put(face, pull);
					pipeGraph.computeIfAbsent(connectedPos, $ -> Pair.of(distance + 1, new IdentityHashMap<>()))
						.getSecond()
						.put(face.getOpposite(), !pull);
					frontier.add(Pair.of(distance + 1, connectedPos));
				}
			}
		}

		// DFS
		Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
		searchForEndpointRecursively(pipeGraph, targets, validFaces,
			new BlockFace(start.getPos(), start.getOppositeFace()), pull);

		float pressure = Math.abs(getSpeed());
		for (Set<BlockFace> set : validFaces.values()) {
			int parallelBranches = Math.max(1, set.size() - 1);
			for (BlockFace face : set) {
				BlockPos pipePos = face.getPos();
				Direction pipeSide = face.getFace();

				if (pipePos.equals(worldPosition))
					continue;

				boolean inbound = pipeGraph.get(pipePos)
					.getSecond()
					.get(pipeSide);
				FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, pipePos);
				if (pipeBehaviour == null)
					continue;

				pipeBehaviour.addPressure(pipeSide, inbound, pressure / parallelBranches);
			}
		}

	}

	protected boolean searchForEndpointRecursively(Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph,
		Set<BlockFace> targets, Map<Integer, Set<BlockFace>> validFaces, BlockFace currentFace, boolean pull) {
		BlockPos currentPos = currentFace.getPos();
		if (!pipeGraph.containsKey(currentPos))
			return false;
		Pair<Integer, Map<Direction, Boolean>> pair = pipeGraph.get(currentPos);
		int distance = pair.getFirst();

		boolean atLeastOneBranchSuccessful = false;
		for (Direction nextFacing : Iterate.directions) {
			if (nextFacing == currentFace.getFace())
				continue;
			Map<Direction, Boolean> map = pair.getSecond();
			if (!map.containsKey(nextFacing))
				continue;

			BlockFace localTarget = new BlockFace(currentPos, nextFacing);
			if (targets.contains(localTarget)) {
				validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
					.add(localTarget);
				atLeastOneBranchSuccessful = true;
				continue;
			}

			if (map.get(nextFacing) != pull)
				continue;
			if (!searchForEndpointRecursively(pipeGraph, targets, validFaces,
				new BlockFace(currentPos.relative(nextFacing), nextFacing.getOpposite()), pull))
				continue;

			validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
				.add(localTarget);
			atLeastOneBranchSuccessful = true;
		}

		if (atLeastOneBranchSuccessful)
			validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
				.add(currentFace);

		return atLeastOneBranchSuccessful;
	}

	private boolean hasReachedValidEndpoint(LevelAccessor world, BlockFace blockFace, boolean pull) {
		BlockPos connectedPos = blockFace.getConnectedPos();
		BlockState connectedState = world.getBlockState(connectedPos);
		BlockEntity blockEntity = world.getBlockEntity(connectedPos);
		Direction face = blockFace.getFace();

		// facing a pump
		if (PumpBlock.isPump(connectedState) && connectedState.getValue(PumpBlock.FACING)
			.getAxis() == face.getAxis() && blockEntity instanceof PumpBlockEntity) {
			PumpBlockEntity pumpBE = (PumpBlockEntity) blockEntity;
			return pumpBE.isPullingOnSide(pumpBE.isFront(blockFace.getOppositeFace())) != pull;
		}

		// other pipe, no endpoint
		FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, connectedPos);
		if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace()))
			return false;

		// fluid handler endpoint
		if (blockEntity != null) {
			LazyOptional<IFluidHandler> capability =
				blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite());
			if (capability.isPresent())
				return true;
		}

		// open endpoint
		return FluidPropagator.isOpenEnd(world, blockFace.getPos(), face);
	}

	public void updatePipesOnSide(Direction side) {
		if (!isSideAccessible(side))
			return;
		updatePipeNetwork(isFront(side));
		getBehaviour(FluidTransportBehaviour.TYPE).wipePressure();
	}

	protected boolean isFront(Direction side) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof PumpBlock))
			return false;
		Direction front = blockState.getValue(PumpBlock.FACING);
		boolean isFront = side == front;
		return isFront;
	}

	@Nullable
	protected Direction getFront() {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof PumpBlock))
			return null;
		return blockState.getValue(PumpBlock.FACING);
	}

	protected void updatePipeNetwork(boolean front) {
		sidesToUpdate.get(front)
			.setTrue();
	}

	public boolean isSideAccessible(Direction side) {
		BlockState blockState = getBlockState();
		if (!(blockState.getBlock() instanceof PumpBlock))
			return false;
		return blockState.getValue(PumpBlock.FACING)
			.getAxis() == side.getAxis();
	}

	public boolean isPullingOnSide(boolean front) {
		return !front;
	}

	class PumpFluidTransferBehaviour extends FluidTransportBehaviour {

		public PumpFluidTransferBehaviour(SmartBlockEntity be) {
			super(be);
		}

		@Override
		public void tick() {
			super.tick();
			for (Entry<Direction, PipeConnection> entry : interfaces.entrySet()) {
				boolean pull = isPullingOnSide(isFront(entry.getKey()));
				Couple<Float> pressure = entry.getValue().getPressure();
				pressure.set(pull, Math.abs(getSpeed()));
				pressure.set(!pull, 0f);
			}
		}

		@Override
		public boolean canHaveFlowToward(BlockState state, Direction direction) {
			return isSideAccessible(direction);
		}

		@Override
		public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
			Direction direction) {
			AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
			if (attachment == AttachmentTypes.RIM)
				return AttachmentTypes.NONE;
			return attachment;
		}

	}
}
