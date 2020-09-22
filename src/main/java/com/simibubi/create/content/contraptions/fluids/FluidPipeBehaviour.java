package com.simibubi.create.content.contraptions.fluids;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.KineticDebugger;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;

public abstract class FluidPipeBehaviour extends TileEntityBehaviour {

	public static BehaviourType<FluidPipeBehaviour> TYPE = new BehaviourType<>();

	// Direction -> (inboundflows{}, outwardflows{})
	Map<Direction, Couple<PipeFlows>> allFlows;
	FluidStack fluid;
	Couple<FluidStack> collision;

	public FluidPipeBehaviour(SmartTileEntity te) {
		super(te);
		allFlows = new IdentityHashMap<>();
		fluid = FluidStack.EMPTY;
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	public void notifyNetwork() {
		FluidPropagator.propagateChangedPipe(this.getWorld(), tileEntity.getPos(), tileEntity.getBlockState());
	}

	public boolean canTransferToward(FluidStack fluid, BlockState state, Direction direction, boolean inbound) {
		return isConnectedTo(state, direction);
	}

	public abstract boolean isConnectedTo(BlockState state, Direction direction);

	public float getRimRadius(BlockState state, Direction direction) {
		return 1 / 4f + 1 / 64f;
	}

	public boolean hasStartedFlow(FluidNetworkFlow flow, Direction face, boolean inbound) {
		return allFlows.containsKey(face) && allFlows.get(face)
			.get(inbound)
			.hasFlow(flow);
	}

	public boolean hasCompletedFlow(Direction face, boolean inbound) {
		return allFlows.containsKey(face) && allFlows.get(face)
			.get(inbound)
			.isCompleted();
	}

	@Override
	public void write(CompoundNBT compound, boolean client) {
		compound.put("Fluid", fluid.writeToNBT(new CompoundNBT()));
		ListNBT flows = new ListNBT();
		for (Direction face : Iterate.directions)
			for (boolean inbound : Iterate.trueAndFalse) {
				LerpedFloat flowProgress = getFlowProgress(face, inbound);
				if (flowProgress == null)
					continue;
				CompoundNBT nbt = new CompoundNBT();
				NBTHelper.writeEnum(nbt, "Face", face);
				nbt.putBoolean("In", inbound);
				PipeFlows pipeFlows = allFlows.get(face)
					.get(inbound);
				Set<FluidNetworkFlow> participants = pipeFlows.participants;
				nbt.putBoolean("Silent", participants == null || participants.isEmpty());
				nbt.put("Progress", flowProgress.writeNBT());

				if (client)
					nbt.putFloat("Strength", pipeFlows.bestFlowStrength);

				flows.add(nbt);
			}
		compound.put("Flows", flows);
	}

	@Override
	public void read(CompoundNBT compound, boolean client) {
		fluid = FluidStack.loadFluidStackFromNBT(compound.getCompound("Fluid"));

		if (client) {
			for (Direction face : Iterate.directions)
				if (allFlows.containsKey(face))
					allFlows.get(face)
						.forEach(pf -> pf.progress = null);
		}

		NBTHelper.iterateCompoundList(compound.getList("Flows", NBT.TAG_COMPOUND), nbt -> {
			Direction face = NBTHelper.readEnum(nbt, "Face", Direction.class);
			boolean inbound = nbt.getBoolean("In");
			LerpedFloat progress = createFlowProgress(0);
			progress.readNBT(nbt.getCompound("Progress"), false);
			addFlow(null, face, inbound, nbt.getBoolean("Silent"));
			setFlowProgress(face, inbound, progress);
			if (client)
				setVisualFlowStrength(face, inbound, nbt.getFloat("Strength"));
		});

		if (!client)
			return;

		for (Direction face : Iterate.directions) {
			if (!allFlows.containsKey(face))
				return;
			Couple<PipeFlows> couple = allFlows.get(face);
			if (couple.get(true).progress == null && couple.get(false).progress == null)
				allFlows.remove(face);
			if (allFlows.isEmpty())
				clear();
		}
	}

	public void addFlow(@Nullable FluidNetworkFlow flow, Direction face, boolean inbound, boolean silent) {
		if (flow != null) {
			FluidStack fluid = flow.getFluidStack();
			if (!this.fluid.isEmpty() && !fluid.isFluidEqual(this.fluid)) {
				collision = Couple.create(this.fluid, fluid);
				return;
			}
			this.fluid = fluid;
		}

		if (!allFlows.containsKey(face)) {
			allFlows.put(face, Couple.create(PipeFlows::new));
			if (inbound && !silent)
				spawnSplashOnRim(face);
		}

		if (flow != null) {
			PipeFlows flows = allFlows.get(face)
				.get(inbound);
			flows.addFlow(flow);
			contentsChanged();
		}
	}

	public void removeFlow(FluidNetworkFlow flow, Direction face, boolean inbound) {
		if (!allFlows.containsKey(face))
			return;
		Couple<PipeFlows> couple = allFlows.get(face);
		couple.get(inbound)
			.removeFlow(flow);
		contentsChanged();
		if (!couple.get(true)
			.isActive()
			&& !couple.get(false)
				.isActive())
			allFlows.remove(face);
		if (allFlows.isEmpty())
			clear();
	}

	public void setVisualFlowStrength(Direction face, boolean inbound, float strength) {
		if (!allFlows.containsKey(face))
			return;
		allFlows.get(face)
			.get(inbound).bestFlowStrength = strength;
	}

	public void setFlowProgress(Direction face, boolean inbound, LerpedFloat progress) {
		if (!allFlows.containsKey(face))
			return;
		allFlows.get(face)
			.get(inbound).progress = progress;
	}

	public LerpedFloat getFlowProgress(Direction face, boolean inbound) {
		if (!allFlows.containsKey(face))
			return null;
		return allFlows.get(face)
			.get(inbound).progress;
	}

	public void skipFlow(Direction face, boolean inbound) {
		if (!allFlows.containsKey(face))
			return;
		Couple<PipeFlows> couple = allFlows.get(face);
		couple.get(inbound)
			.skip();
	}

	public void clear() {
		allFlows.clear();
		fluid = FluidStack.EMPTY;
		contentsChanged();
	}

	public void spawnParticles() {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> this::spawnParticlesInner);
	}

	public void spawnSplashOnRim(Direction face) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> spawnSplashOnRimInner(face));
	}

	public static final int MAX_PARTICLE_RENDER_DISTANCE = 20;
	public static final int SPLASH_PARTICLE_AMOUNT = 1;
	public static final float IDLE_PARTICLE_SPAWN_CHANCE = 1 / 800f;
	public static final Random r = new Random();

	@OnlyIn(Dist.CLIENT)
	private void spawnParticlesInner() {
		if (!isRenderEntityWithinDistance())
			return;
		if (fluid.isEmpty())
			return;

		World world = Minecraft.getInstance().world;
		BlockPos pos = tileEntity.getPos();
		BlockState state = world.getBlockState(pos);

		for (Direction face : Iterate.directions) {
			boolean open = FluidPropagator.isOpenEnd(world, pos, face);
			if (isConnectedTo(state, face)) {
				if (open) {
					spawnPouringLiquid(world, state, fluid, face, 1);
					continue;
				}
				if (r.nextFloat() < IDLE_PARTICLE_SPAWN_CHANCE)
					spawnRimParticles(world, state, fluid, face, 1);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnSplashOnRimInner(Direction face) {
		if (!isRenderEntityWithinDistance())
			return;
		if (fluid.isEmpty())
			return;
		World world = Minecraft.getInstance().world;
		BlockPos pos = tileEntity.getPos();
		BlockState state = world.getBlockState(pos);
		spawnRimParticles(world, state, fluid, face, SPLASH_PARTICLE_AMOUNT);
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnRimParticles(World world, BlockState state, FluidStack fluid, Direction side, int amount) {
		BlockPos pos = tileEntity.getPos();
		if (FluidPropagator.isOpenEnd(world, pos, side)) {
			spawnPouringLiquid(world, state, fluid, side, amount);
			return;
		}

		IParticleData particle = null;
		if (FluidHelper.isWater(fluid.getFluid()))
			particle = ParticleTypes.DRIPPING_WATER;
		if (FluidHelper.isLava(fluid.getFluid()))
			particle = ParticleTypes.DRIPPING_LAVA;
		// TODO: Generic drip particle type for forge fluids

		if (particle == null)
			return;

		float rimRadius = getRimRadius(state, side);
		Vector3d directionVec = Vector3d.of(side.getDirectionVec());

		for (int i = 0; i < amount; i++) {
			Vector3d vec = VecHelper.offsetRandomly(Vector3d.ZERO, r, 1)
				.normalize();
			vec = VecHelper.clampComponentWise(vec, rimRadius)
				.mul(VecHelper.axisAlingedPlaneOf(directionVec))
				.add(directionVec.scale(.45 + r.nextFloat() / 16f));
			Vector3d m = vec;
			vec = vec.add(VecHelper.getCenterOf(pos));

			world.addOptionalParticle(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private void spawnPouringLiquid(World world, BlockState state, FluidStack fluid, Direction side, int amount) {
		IParticleData particle = new BlockParticleData(ParticleTypes.BLOCK, fluid.getFluid()
			.getDefaultState()
			.getBlockState());
		float rimRadius = getRimRadius(state, side);
		Vector3d directionVec = Vector3d.of(side.getDirectionVec());

		Couple<PipeFlows> couple = allFlows.get(side);
		if (couple == null)
			return;
		couple.forEachWithContext((flow, inbound) -> {
			if (flow.progress == null)
				return;
			for (int i = 0; i < amount; i++) {
				Vector3d vec = VecHelper.offsetRandomly(Vector3d.ZERO, r, rimRadius);
				vec = vec.mul(VecHelper.axisAlingedPlaneOf(directionVec))
					.add(directionVec.scale(.5 + r.nextFloat() / 4f));
				Vector3d m = vec;
				Vector3d centerOf = VecHelper.getCenterOf(tileEntity.getPos());
				vec = vec.add(centerOf);
				if (inbound) {
					vec = vec.add(m);
					m = centerOf.add(directionVec.scale(.5))
						.subtract(vec)
						.scale(3);
				}
				world.addOptionalParticle(particle, vec.x, vec.y - 1 / 16f, vec.z, m.x, m.y, m.z);
			}
		});

	}

	@OnlyIn(Dist.CLIENT)
	private boolean isRenderEntityWithinDistance() {
		Entity renderViewEntity = Minecraft.getInstance()
			.getRenderViewEntity();
		if (renderViewEntity == null)
			return false;
		Vector3d center = VecHelper.getCenterOf(tileEntity.getPos());
		if (renderViewEntity.getPositionVec()
			.distanceTo(center) > MAX_PARTICLE_RENDER_DISTANCE)
			return false;
		return true;
	}

	static AxisAlignedBB smallCenter = new AxisAlignedBB(BlockPos.ZERO).shrink(.25);

	@Override
	public void tick() {
		super.tick();
		boolean isRemote = getWorld().isRemote;

		allFlows.values()
			.forEach(c -> c.forEach(pf -> pf.tick(isRemote)));

		if (isRemote) {
			clientTick();
			return;
		}

		if (collision != null) {
			FluidReactions.handlePipeFlowCollision(getWorld(), tileEntity.getPos(), collision.getFirst(),
				collision.getSecond());
			collision = null;
			return;
		}
	}

	public Pair<Boolean, LerpedFloat> getStrogestFlow(Direction side) {
		Couple<PipeFlows> couple = allFlows.get(side);
		if (couple == null)
			return null;

		PipeFlows in = couple.get(true);
		PipeFlows out = couple.get(false);
		Couple<LerpedFloat> progress = couple.map(pf -> pf.progress);
		boolean inboundStronger = false;

		if (in.isCompleted() != out.isCompleted()) {
			inboundStronger = in.isCompleted();
		} else if ((progress.get(true) == null) != (progress.get(false) == null)) {
			inboundStronger = progress.get(true) != null;
		} else {
			if (progress.get(true) != null)
				inboundStronger = in.bestFlowStrength > out.bestFlowStrength;
		}

		return Pair.of(inboundStronger, progress.get(inboundStronger));
	}

	private void clientTick() {
		spawnParticles();

		if (!KineticDebugger.isActive())
			return;
		if (fluid.isEmpty())
			return;
		for (Entry<Direction, Couple<PipeFlows>> entry : allFlows.entrySet()) {
			Direction face = entry.getKey();
			Vector3d directionVec = Vector3d.of(face.getDirectionVec());
			float size = 1 / 4f;
			boolean extended = !isConnectedTo(tileEntity.getBlockState(), face.getOpposite());
			float length = extended ? .75f : .5f;

			entry.getValue()
				.forEachWithContext((flow, inbound) -> {
					if (flow.progress == null)
						return;
					float value = flow.progress.getValue();
					Vector3d start = directionVec.scale(inbound ? .5 : .5f - length);
					Vector3d offset = directionVec.scale(length * (inbound ? -1 : 1))
						.scale(value);

					Vector3d scale = new Vector3d(1, 1, 1).subtract(directionVec.scale(face.getAxisDirection()
						.getOffset()))
						.scale(size);
					AxisAlignedBB bb =
						new AxisAlignedBB(start, start.add(offset)).offset(VecHelper.getCenterOf(tileEntity.getPos()))
							.grow(scale.x, scale.y, scale.z);

					int color = 0x7fdbda;
					if (!fluid.isEmpty()) {
						Fluid fluid2 = fluid.getFluid();
						if (fluid2 == Fluids.WATER)
							color = 0x1D4D9B;
						if (fluid2 == Fluids.LAVA)
							color = 0xFF773D;
					}

					CreateClient.outliner.chaseAABB(Pair.of(this, face), bb)
						.withFaceTexture(AllSpecialTextures.CUTOUT_CHECKERED)
						.colored(color)
						.lineWidth(1 / 16f);
				});
		}
	}

	private void contentsChanged() {
		tileEntity.markDirty();
		tileEntity.sendData();
	}

	private LerpedFloat createFlowProgress(double speed) {
		return LerpedFloat.linear()
			.startWithValue(0)
			.chase(1, speed, Chaser.LINEAR);
	}

	public FluidStack getFluid() {
		return fluid;
	}

	class PipeFlows {
		LerpedFloat progress;
		Set<FluidNetworkFlow> participants;
		float bestFlowStrength;

		void addFlow(FluidNetworkFlow flow) {
			if (participants == null)
				participants = new HashSet<>();
			participants.add(flow);

			if (progress == null) {
				progress = createFlowProgress(flow.getSpeed());
			}
		}

		boolean hasFlow(FluidNetworkFlow flow) {
			return participants != null && participants.contains(flow);
		}

		void tick(boolean onClient) {
			if (progress == null)
				return;
			if (!onClient) {
				if (participants == null)
					return;
				bestFlowStrength = 0;
				for (FluidNetworkFlow networkFlow : participants)
					bestFlowStrength = Math.max(bestFlowStrength, networkFlow.getSpeed());
				if (isCompleted())
					return;
				if (progress.updateChaseSpeed(bestFlowStrength))
					contentsChanged();
			}
			progress.tickChaser();
		}

		void skip() {
			progress = LerpedFloat.linear()
				.startWithValue(1);
		}

		void removeFlow(FluidNetworkFlow flow) {
			if (participants == null)
				return;
			participants.remove(flow);
		}

		boolean isActive() {
			return participants != null && !participants.isEmpty();
		}

		boolean isCompleted() {
			return progress != null && progress.getValue() == 1;
		}

	}

}
