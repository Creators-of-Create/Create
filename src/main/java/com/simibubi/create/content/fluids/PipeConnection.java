package com.simibubi.create.content.fluids;

import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.utility.BlockFace;
import net.createmod.catnip.utility.Couple;
import net.createmod.catnip.utility.Iterate;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.animation.LerpedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;

public class PipeConnection {

	public Direction side;

	// Layer I
	Couple<Float> pressure; // [inbound, outward]
	Optional<FlowSource> source;
	Optional<FlowSource> previousSource;

	// Layer II
	Optional<Flow> flow;
	boolean particleSplashNextTick;

	// Layer III
	Optional<FluidNetwork> network; // not serialized

	public PipeConnection(Direction side) {
		this.side = side;
		pressure = Couple.create(() -> 0f);
		flow = Optional.empty();
		previousSource = Optional.empty();
		source = Optional.empty();
		network = Optional.empty();
		particleSplashNextTick = false;
	}

	public FluidStack getProvidedFluid() {
		FluidStack empty = FluidStack.EMPTY;
		if (!hasFlow())
			return empty;
		Flow flow = this.flow.get();
		if (!flow.inbound)
			return empty;
		if (!flow.complete)
			return empty;
		return flow.fluid;
	}

	public boolean flipFlowsIfPressureReversed() {
		if (!hasFlow())
			return false;
		boolean singlePressure = comparePressure() != 0 && (getInboundPressure() == 0 || getOutwardPressure() == 0);
		Flow flow = this.flow.get();
		if (!singlePressure || comparePressure() < 0 == flow.inbound)
			return false;
		flow.inbound = !flow.inbound;
		if (!flow.complete)
			this.flow = Optional.empty();
		return true;
	}

	public void manageSource(Level world, BlockPos pos) {
		if (!source.isPresent() && !determineSource(world, pos))
			return;
		FlowSource flowSource = source.get();
		flowSource.manageSource(world);
	}

	public boolean manageFlows(Level world, BlockPos pos, FluidStack internalFluid,
		Predicate<FluidStack> extractionPredicate) {

		// Only keep network if still valid
		Optional<FluidNetwork> retainedNetwork = network;
		network = Optional.empty();

		// chunk border
		if (!source.isPresent() && !determineSource(world, pos))
			return false;
		FlowSource flowSource = source.get();

		if (!hasFlow()) {
			if (!hasPressure())
				return false;

			// Try starting a new flow
			boolean prioritizeInbound = comparePressure() < 0;
			for (boolean trueFalse : Iterate.trueAndFalse) {
				boolean inbound = prioritizeInbound == trueFalse;
				if (pressure.get(inbound) == 0)
					continue;
				if (tryStartingNewFlow(inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid))
					return true;
			}
			return false;
		}

		// Manage existing flow
		Flow flow = this.flow.get();
		FluidStack provided = flow.inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid;
		if (!hasPressure() || provided.isEmpty() || !provided.isFluidEqual(flow.fluid)) {
			this.flow = Optional.empty();
			return true;
		}

		// Overwrite existing flow
		if (flow.inbound != comparePressure() < 0) {
			boolean inbound = !flow.inbound;
			if (inbound && !provided.isEmpty() || !inbound && !internalFluid.isEmpty()) {
				FluidPropagator.resetAffectedFluidNetworks(world, pos, side);
				tryStartingNewFlow(inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid);
				return true;
			}
		}

		flowSource.whileFlowPresent(world, flow.inbound);

		if (!flowSource.isEndpoint())
			return false;
		if (!flow.inbound)
			return false;

		// Layer III
		network = retainedNetwork;
		if (!hasNetwork())
			network = Optional.of(new FluidNetwork(world, new BlockFace(pos, side), flowSource::provideHandler));
		network.get()
			.tick();

		return false;
	}

	private boolean tryStartingNewFlow(boolean inbound, FluidStack providedFluid) {
		if (providedFluid.isEmpty())
			return false;
		Flow flow = new Flow(inbound, providedFluid);
		this.flow = Optional.of(flow);
		return true;
	}

	public boolean determineSource(Level world, BlockPos pos) {
		BlockPos relative = pos.relative(side);
		// cannot use world.isLoaded because it always returns true on client
		if (world.getChunk(relative.getX() >> 4, relative.getZ() >> 4, ChunkStatus.FULL, false) == null)
			return false;

		BlockFace location = new BlockFace(pos, side);
		if (FluidPropagator.isOpenEnd(world, pos, side)) {
			if (previousSource.orElse(null) instanceof OpenEndedPipe)
				source = previousSource;
			else
				source = Optional.of(new OpenEndedPipe(location));
			return true;
		}

		if (FluidPropagator.hasFluidCapability(world, location.getConnectedPos(), side.getOpposite())) {
			source = Optional.of(new FlowSource.FluidHandler(location));
			return true;
		}

		FluidTransportBehaviour behaviour =
			BlockEntityBehaviour.get(world, relative, FluidTransportBehaviour.TYPE);
		source = Optional.of(behaviour == null ? new FlowSource.Blocked(location) : new FlowSource.OtherPipe(location));
		return true;
	}

	public void tickFlowProgress(Level world, BlockPos pos) {
		if (!hasFlow())
			return;
		Flow flow = this.flow.get();
		if (flow.fluid.isEmpty())
			return;

		if (world.isClientSide) {
			if (!source.isPresent())
				determineSource(world, pos);

			spawnParticles(world, pos, flow.fluid);
			if (particleSplashNextTick)
				spawnSplashOnRim(world, pos, flow.fluid);
			particleSplashNextTick = false;
		}

		float flowSpeed = 1 / 32f + Mth.clamp(pressure.get(flow.inbound) / 128f, 0, 1) * 31 / 32f;
		flow.progress.setValue(Math.min(flow.progress.getValue() + flowSpeed, 1));
		if (flow.progress.getValue() >= 1)
			flow.complete = true;
	}

	public void serializeNBT(CompoundTag tag, boolean clientPacket) {
		CompoundTag connectionData = new CompoundTag();
		tag.put(side.getName(), connectionData);

		if (hasPressure()) {
			ListTag pressureData = new ListTag();
			pressureData.add(FloatTag.valueOf(getInboundPressure()));
			pressureData.add(FloatTag.valueOf(getOutwardPressure()));
			connectionData.put("Pressure", pressureData);
		}

		if (hasOpenEnd())
			connectionData.put("OpenEnd", ((OpenEndedPipe) source.get()).serializeNBT());

		if (hasFlow()) {
			CompoundTag flowData = new CompoundTag();
			Flow flow = this.flow.get();
			flow.fluid.writeToNBT(flowData);
			flowData.putBoolean("In", flow.inbound);
			if (!flow.complete)
				flowData.put("Progress", flow.progress.writeNBT());
			connectionData.put("Flow", flowData);
		}

	}

	private boolean hasOpenEnd() {
		return source.orElse(null) instanceof OpenEndedPipe;
	}

	public void deserializeNBT(CompoundTag tag, BlockPos blockEntityPos, boolean clientPacket) {
		CompoundTag connectionData = tag.getCompound(side.getName());

		if (connectionData.contains("Pressure")) {
			ListTag pressureData = connectionData.getList("Pressure", Tag.TAG_FLOAT);
			pressure = Couple.create(pressureData.getFloat(0), pressureData.getFloat(1));
		} else
			pressure.replace(f -> 0f);

		source = Optional.empty();
		if (connectionData.contains("OpenEnd"))
			source = Optional.of(OpenEndedPipe.fromNBT(connectionData.getCompound("OpenEnd"), blockEntityPos));

		if (connectionData.contains("Flow")) {
			CompoundTag flowData = connectionData.getCompound("Flow");
			FluidStack fluid = FluidStack.loadFluidStackFromNBT(flowData);
			boolean inbound = flowData.getBoolean("In");
			if (!flow.isPresent()) {
				flow = Optional.of(new Flow(inbound, fluid));
				if (clientPacket)
					particleSplashNextTick = true;
			}
			Flow flow = this.flow.get();

			flow.fluid = fluid;
			flow.inbound = inbound;
			flow.complete = !flowData.contains("Progress");

			if (!flow.complete)
				flow.progress.readNBT(flowData.getCompound("Progress"), clientPacket);
			else {
				if (flow.progress.getValue() == 0)
					flow.progress.startWithValue(1);
				flow.progress.setValue(1);
			}

		} else
			flow = Optional.empty();

	}

	/**
	 * @return zero if outward == inbound <br>
	 *         positive if outward {@literal >} inbound <br>
	 *         negative if outward {@literal <} inbound
	 */
	public float comparePressure() {
		return getOutwardPressure() - getInboundPressure();
	}

	public void wipePressure() {
		this.pressure.replace(f -> 0f);
		if (this.source.isPresent())
			this.previousSource = this.source;
		this.source = Optional.empty();
		resetNetwork();
	}

	public FluidStack provideOutboundFlow() {
		if (!hasFlow())
			return FluidStack.EMPTY;
		Flow flow = this.flow.get();
		if (!flow.complete || flow.inbound)
			return FluidStack.EMPTY;
		return flow.fluid;
	}

	public void addPressure(boolean inbound, float pressure) {
		this.pressure = this.pressure.mapWithContext((f, in) -> in == inbound ? f + pressure : f);
	}

	public Couple<Float> getPressure() {
		return pressure;
	}

	public boolean hasPressure() {
		return getInboundPressure() != 0 || getOutwardPressure() != 0;
	}

	private float getOutwardPressure() {
		return pressure.getSecond();
	}

	private float getInboundPressure() {
		return pressure.getFirst();
	}

	public boolean hasFlow() {
		return flow.isPresent();
	}

	public boolean hasNetwork() {
		return network.isPresent();
	}

	public void resetNetwork() {
		network.ifPresent(FluidNetwork::reset);
	}

	public class Flow {

		public boolean complete;
		public boolean inbound;
		public LerpedFloat progress;
		public FluidStack fluid;

		public Flow(boolean inbound, FluidStack fluid) {
			this.inbound = inbound;
			this.fluid = fluid;
			this.progress = LerpedFloat.linear()
				.startWithValue(0);
			this.complete = false;
		}

	}

	public static final int MAX_PARTICLE_RENDER_DISTANCE = 20;
	public static final int SPLASH_PARTICLE_AMOUNT = 1;
	public static final float IDLE_PARTICLE_SPAWN_CHANCE = 1 / 1000f;
	public static final float RIM_RADIUS = 1 / 4f + 1 / 64f;
	public static final Random r = new Random();

	public void spawnSplashOnRim(Level world, BlockPos pos, FluidStack fluid) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> spawnSplashOnRimInner(world, pos, fluid));
	}

	public void spawnParticles(Level world, BlockPos pos, FluidStack fluid) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> spawnParticlesInner(world, pos, fluid));
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnParticlesInner(Level world, BlockPos pos, FluidStack fluid) {
		if (world == Minecraft.getInstance().level)
			if (!isRenderEntityWithinDistance(pos))
				return;
		if (hasOpenEnd())
			spawnPouringLiquid(world, pos, fluid, 1);
		else if (r.nextFloat() < IDLE_PARTICLE_SPAWN_CHANCE)
			spawnRimParticles(world, pos, fluid, 1);
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnSplashOnRimInner(Level world, BlockPos pos, FluidStack fluid) {
		if (world == Minecraft.getInstance().level)
			if (!isRenderEntityWithinDistance(pos))
				return;
		spawnRimParticles(world, pos, fluid, SPLASH_PARTICLE_AMOUNT);
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnRimParticles(Level world, BlockPos pos, FluidStack fluid, int amount) {
		if (hasOpenEnd()) {
			spawnPouringLiquid(world, pos, fluid, amount);
			return;
		}

		ParticleOptions particle = FluidFX.getDrippingParticle(fluid);
		FluidFX.spawnRimParticles(world, pos, side, amount, particle, RIM_RADIUS);
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnPouringLiquid(Level world, BlockPos pos, FluidStack fluid, int amount) {
		ParticleOptions particle = FluidFX.getFluidParticle(fluid);
		Vec3 directionVec = Vec3.atLowerCornerOf(side.getNormal());
		if (!hasFlow())
			return;
		Flow flow = this.flow.get();
		FluidFX.spawnPouringLiquid(world, pos, amount, particle, RIM_RADIUS, directionVec, flow.inbound);
	}

	@OnlyIn(Dist.CLIENT)
	public static boolean isRenderEntityWithinDistance(BlockPos pos) {
		Entity renderViewEntity = Minecraft.getInstance()
			.getCameraEntity();
		if (renderViewEntity == null)
			return false;
		Vec3 center = VecHelper.getCenterOf(pos);
		if (renderViewEntity.position()
			.distanceTo(center) > MAX_PARTICLE_RENDER_DISTANCE)
			return false;
		return true;
	}

//	void visualizePressure(BlockPos pos) {
//		if (!hasPressure())
//			return;
//
//		pressure.forEachWithContext((pressure, inbound) -> {
//			if (inbound)
//				return;
//
//			Vector3d directionVec = new Vector3d(side.getDirectionVec());
//			Vector3d scaleVec = directionVec.scale(-.25f * side.getAxisDirection()
//				.getOffset());
//			directionVec = directionVec.scale(inbound ? .35f : .45f);
//			CreateClient.outliner.chaseAABB("pressure" + pos.toShortString() + side.getName() + String.valueOf(inbound),
//				FluidPropagator.smallCenter.offset(directionVec.add(new Vector3d(pos)))
//					.grow(scaleVec.x, scaleVec.y, scaleVec.z)
//					.expand(0, pressure / 64f, 0)
//					.grow(1 / 64f));
//		});
//	}
//
//	void visualizeFlow(BlockPos pos) {
//		if (!hasFlow())
//			return;
//
//		Vector3d directionVec = new Vector3d(side.getDirectionVec());
//		float size = 1 / 4f;
//		float length = .5f;
//		Flow flow = this.flow.get();
//		boolean inbound = flow.inbound;
//		FluidStack fluid = flow.fluid;
//
//		if (flow.progress == null)
//			return;
//		float value = flow.progress.getValue();
//		Vector3d start = directionVec.scale(inbound ? .5 : .5f - length);
//		Vector3d offset = directionVec.scale(length * (inbound ? -1 : 1))
//			.scale(value);
//
//		Vector3d scale = new Vector3d(1, 1, 1).subtract(directionVec.scale(side.getAxisDirection()
//			.getOffset()))
//			.scale(size);
//		AABB bb = new AABB(start, start.add(offset)).offset(VecHelper.getCenterOf(pos))
//			.grow(scale.x, scale.y, scale.z);
//
//		int color = 0x7fdbda;
//		if (!fluid.isEmpty()) {
//			Fluid fluid2 = fluid.getFluid();
//			if (fluid2 == Fluids.WATER)
//				color = 0x1D4D9B;
//			else if (fluid2 == Fluids.LAVA)
//				color = 0xFF773D;
//			else
//				color = fluid2.getAttributes()
//					.getColor(fluid);
//		}
//
//		CreateClient.outliner.chaseAABB(this, bb)
//			.withFaceTexture(AllSpecialTextures.SELECTION)
//			.colored(color)
//			.lineWidth(0);
//	}

}
