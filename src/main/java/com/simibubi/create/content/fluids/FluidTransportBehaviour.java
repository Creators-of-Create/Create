package com.simibubi.create.content.fluids;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidTransportBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<FluidTransportBehaviour> TYPE = new BehaviourType<>();

	public enum UpdatePhase {
		WAIT_FOR_PUMPS, // Do not run Layer II logic while pumps could still be distributing pressure
		FLIP_FLOWS, // Do not cut any flows until all pipes had a chance to reverse them
		IDLE; // Operate normally
	}

	public Map<Direction, PipeConnection> interfaces;
	public UpdatePhase phase;

	public FluidTransportBehaviour(SmartBlockEntity be) {
		super(be);
		phase = UpdatePhase.WAIT_FOR_PUMPS;
	}

	public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
		return true;
	}

	public abstract boolean canHaveFlowToward(BlockState state, Direction direction);

	@Override
	public void initialize() {
		super.initialize();
		createConnectionData();
	}

	@Override
	public void tick() {
		super.tick();
		Level world = getWorld();
		BlockPos pos = getPos();
		boolean onServer = !world.isClientSide || blockEntity.isVirtual();

		if (interfaces == null)
			return;
		Collection<PipeConnection> connections = interfaces.values();

		// Do not provide a lone pipe connection with its own flow input
		PipeConnection singleSource = null;

//		if (onClient) {
//			connections.forEach(connection -> {
//				connection.visualizeFlow(pos);
//				connection.visualizePressure(pos);
//			});
//		}

		if (phase == UpdatePhase.WAIT_FOR_PUMPS) {
			phase = UpdatePhase.FLIP_FLOWS;
			return;
		}

		if (onServer) {
			boolean sendUpdate = false;
			for (PipeConnection connection : connections) {
				sendUpdate |= connection.flipFlowsIfPressureReversed();
				connection.manageSource(world, pos);
			}
			if (sendUpdate)
				blockEntity.notifyUpdate();
		}

		if (phase == UpdatePhase.FLIP_FLOWS) {
			phase = UpdatePhase.IDLE;
			return;
		}

		if (onServer) {
			FluidStack availableFlow = FluidStack.EMPTY;
			FluidStack collidingFlow = FluidStack.EMPTY;

			for (PipeConnection connection : connections) {
				FluidStack fluidInFlow = connection.getProvidedFluid();
				if (fluidInFlow.isEmpty())
					continue;
				if (availableFlow.isEmpty()) {
					singleSource = connection;
					availableFlow = fluidInFlow;
					continue;
				}
				if (availableFlow.isFluidEqual(fluidInFlow)) {
					singleSource = null;
					availableFlow = fluidInFlow;
					continue;
				}
				collidingFlow = fluidInFlow;
				break;
			}

			if (!collidingFlow.isEmpty()) {
				FluidReactions.handlePipeFlowCollision(world, pos, availableFlow, collidingFlow);
				return;
			}

			boolean sendUpdate = false;
			for (PipeConnection connection : connections) {
				FluidStack internalFluid = singleSource != connection ? availableFlow : FluidStack.EMPTY;
				Predicate<FluidStack> extractionPredicate =
					extracted -> canPullFluidFrom(extracted, blockEntity.getBlockState(), connection.side);
				sendUpdate |= connection.manageFlows(world, pos, internalFluid, extractionPredicate);
			}

			if (sendUpdate)
				blockEntity.notifyUpdate();
		}

		for (PipeConnection connection : connections)
			connection.tickFlowProgress(world, pos);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);
		if (interfaces == null)
			interfaces = new IdentityHashMap<>();
		for (Direction face : Iterate.directions)
			if (nbt.contains(face.getName()))
				interfaces.computeIfAbsent(face, d -> new PipeConnection(d));

		// Invalid data (missing/outdated). Defer init to runtime
		if (interfaces.isEmpty()) {
			interfaces = null;
			return;
		}

		interfaces.values()
			.forEach(connection -> connection.deserializeNBT(nbt, blockEntity.getBlockPos(), clientPacket));
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		if (clientPacket)
			createConnectionData();
		if (interfaces == null)
			return;

		interfaces.values()
			.forEach(connection -> connection.serializeNBT(nbt, clientPacket));
	}

	public FluidStack getProvidedOutwardFluid(Direction side) {
		createConnectionData();
		if (!interfaces.containsKey(side))
			return FluidStack.EMPTY;
		return interfaces.get(side)
			.provideOutboundFlow();
	}

	@Nullable
	public PipeConnection getConnection(Direction side) {
		createConnectionData();
		return interfaces.get(side);
	}

	public boolean hasAnyPressure() {
		createConnectionData();
		for (PipeConnection pipeConnection : interfaces.values())
			if (pipeConnection.hasPressure())
				return true;
		return false;
	}

	@Nullable
	public PipeConnection.Flow getFlow(Direction side) {
		createConnectionData();
		if (!interfaces.containsKey(side))
			return null;
		return interfaces.get(side).flow.orElse(null);
	}

	public void addPressure(Direction side, boolean inbound, float pressure) {
		createConnectionData();
		if (!interfaces.containsKey(side))
			return;
		interfaces.get(side)
			.addPressure(inbound, pressure);
		blockEntity.sendData();
	}

	public void wipePressure() {
		if (interfaces != null)
			for (Direction d : Iterate.directions) {
				if (!canHaveFlowToward(blockEntity.getBlockState(), d))
					interfaces.remove(d);
				else
					interfaces.computeIfAbsent(d, PipeConnection::new);
			}
		phase = UpdatePhase.WAIT_FOR_PUMPS;
		createConnectionData();
		interfaces.values()
			.forEach(PipeConnection::wipePressure);
		blockEntity.sendData();
	}

	private void createConnectionData() {
		if (interfaces != null)
			return;
		interfaces = new IdentityHashMap<>();
		for (Direction d : Iterate.directions)
			if (canHaveFlowToward(blockEntity.getBlockState(), d))
				interfaces.put(d, new PipeConnection(d));
	}

	public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
		Direction direction) {
		if (!canHaveFlowToward(state, direction))
			return AttachmentTypes.NONE;

		BlockPos offsetPos = pos.relative(direction);
		BlockState facingState = world.getBlockState(offsetPos);

		if (facingState.getBlock() instanceof PumpBlock
			&& facingState.getValue(PumpBlock.FACING) == direction.getOpposite())
			return AttachmentTypes.NONE;

		if (AllBlocks.ENCASED_FLUID_PIPE.has(facingState)
			&& facingState.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(direction.getOpposite())))
			return AttachmentTypes.RIM;

		if (FluidPropagator.hasFluidCapability(world, offsetPos, direction.getOpposite())
			&& !AllBlocks.HOSE_PULLEY.has(facingState))
			return AttachmentTypes.DRAIN;

		return AttachmentTypes.RIM;
	}

	public enum AttachmentTypes {
		NONE,
		CONNECTION(ComponentPartials.CONNECTION),
		RIM(ComponentPartials.RIM_CONNECTOR, ComponentPartials.RIM),
		PARTIAL_RIM(ComponentPartials.RIM),
		DRAIN(ComponentPartials.RIM_CONNECTOR, ComponentPartials.DRAIN),
		PARTIAL_DRAIN(ComponentPartials.DRAIN);

		public final ComponentPartials[] partials;

		AttachmentTypes(ComponentPartials... partials) {
			this.partials = partials;
		}

		public AttachmentTypes withoutConnector() {
			if (this == AttachmentTypes.RIM)
				return AttachmentTypes.PARTIAL_RIM;
			if (this == AttachmentTypes.DRAIN)
				return AttachmentTypes.PARTIAL_DRAIN;
			return this;
		}

		public enum ComponentPartials {
			CONNECTION, RIM_CONNECTOR, RIM, DRAIN;
		}
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	// for switching TEs, but retaining flows

	public static final WorldAttached<Map<BlockPos, Map<Direction, PipeConnection>>> interfaceTransfer =
		new WorldAttached<>($ -> new HashMap<>());

	public static void cacheFlows(LevelAccessor world, BlockPos pos) {
		FluidTransportBehaviour pipe = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		if (pipe != null)
			interfaceTransfer.get(world)
				.put(pos, pipe.interfaces);
	}

	public static void loadFlows(LevelAccessor world, BlockPos pos) {
		FluidTransportBehaviour newPipe = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		if (newPipe != null)
			newPipe.interfaces = interfaceTransfer.get(world)
				.remove(pos);
	}

}
