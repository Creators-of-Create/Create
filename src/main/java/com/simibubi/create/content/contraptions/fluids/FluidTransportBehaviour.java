package com.simibubi.create.content.contraptions.fluids;

import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.WorldAttached;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public abstract class FluidTransportBehaviour extends TileEntityBehaviour {

	public static BehaviourType<FluidTransportBehaviour> TYPE = new BehaviourType<>();

	public enum UpdatePhase {
		WAIT_FOR_PUMPS, // Do not run Layer II logic while pumps could still be distributing pressure
		FLIP_FLOWS, // Do not cut any flows until all pipes had a chance to reverse them
		IDLE; // Operate normally
	}

	public Map<Direction, PipeConnection> interfaces;
	public UpdatePhase phase;

	public FluidTransportBehaviour(SmartTileEntity te) {
		super(te);
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
		World world = getWorld();
		BlockPos pos = getPos();
		boolean onServer = !world.isClientSide || tileEntity.isVirtual();

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
				tileEntity.notifyUpdate();
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
					extracted -> canPullFluidFrom(extracted, tileEntity.getBlockState(), connection.side);
				sendUpdate |= connection.manageFlows(world, pos, internalFluid, extractionPredicate);
			}

			if (sendUpdate)
				tileEntity.notifyUpdate();
		}

		for (PipeConnection connection : connections)
			connection.tickFlowProgress(world, pos);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
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
			.forEach(connection -> connection.deserializeNBT(nbt, tileEntity.getBlockPos(), clientPacket));
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
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
		tileEntity.sendData();
	}

	public void wipePressure() {
		if (interfaces != null)
			for (Direction d : Iterate.directions) {
				if (!canHaveFlowToward(tileEntity.getBlockState(), d))
					interfaces.remove(d);
				else
					interfaces.computeIfAbsent(d, PipeConnection::new);
			}
		phase = UpdatePhase.WAIT_FOR_PUMPS;
		createConnectionData();
		interfaces.values()
			.forEach(PipeConnection::wipePressure);
		tileEntity.sendData();
	}

	private void createConnectionData() {
		if (interfaces != null)
			return;
		interfaces = new IdentityHashMap<>();
		for (Direction d : Iterate.directions)
			if (canHaveFlowToward(tileEntity.getBlockState(), d))
				interfaces.put(d, new PipeConnection(d));
	}

	public AttachmentTypes getRenderedRimAttachment(IBlockDisplayReader world, BlockPos pos, BlockState state,
		Direction direction) {
		if (!canHaveFlowToward(state, direction))
			return AttachmentTypes.NONE;

		BlockPos offsetPos = pos.relative(direction);
		BlockState facingState = world.getBlockState(offsetPos);

		if (facingState.getBlock() instanceof PumpBlock && facingState.getValue(PumpBlock.FACING)
			.getAxis() == direction.getAxis())
			return AttachmentTypes.NONE;

		if (AllBlocks.ENCASED_FLUID_PIPE.has(facingState)
			&& facingState.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(direction.getOpposite())))
			return AttachmentTypes.NONE;

		if (FluidPropagator.hasFluidCapability(world, offsetPos, direction.getOpposite())
			&& !AllBlocks.HOSE_PULLEY.has(facingState))
			return AttachmentTypes.DRAIN;

		return AttachmentTypes.RIM;
	}

	public static enum AttachmentTypes {
		NONE, RIM, DRAIN;

		public boolean hasModel() {
			return this != NONE;
		}
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	// for switching TEs, but retaining flows

	public static final WorldAttached<Map<BlockPos, Map<Direction, PipeConnection>>> interfaceTransfer =
		new WorldAttached<>($ -> new HashMap<>());

	public static void cacheFlows(IWorld world, BlockPos pos) {
		FluidTransportBehaviour pipe = TileEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		if (pipe != null)
			interfaceTransfer.get(world)
				.put(pos, pipe.interfaces);
	}

	public static void loadFlows(IWorld world, BlockPos pos) {
		FluidTransportBehaviour newPipe = TileEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
		if (newPipe != null)
			newPipe.interfaces = interfaceTransfer.get(world)
				.remove(pos);
	}

}
