package com.simibubi.create.content.contraptions.fluids.actors;

import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

// FIXME: Quite similar to FluidTankTileEntity, create a behaviour

public class SpoutTileEntity extends SmartTileEntity {

	protected FluidTank tank;
	protected LazyOptional<IFluidHandler> capability;
	protected LerpedFloat fluidLevel;
	protected FluidStack renderedFluid;

	public static final int FILLING_TIME = 20;
	protected int processingTicks;

	private static final int SYNC_RATE = 8;
	protected int syncCooldown;
	protected boolean queuedSync;

	protected boolean sendSplash;
	protected BeltProcessingBehaviour beltProcessing;

	public SpoutTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		tank = new SmartFluidTank(1000, this::onFluidStackChanged);
		capability = LazyOptional.of(() -> tank);
		fluidLevel = LerpedFloat.linear()
			.startWithValue(0)
			.chase(0, .25, Chaser.EXP);
		renderedFluid = FluidStack.EMPTY;
		processingTicks = -1;
	}

	protected void onFluidStackChanged(FluidStack newFluidStack) {
		if (!hasWorld())
			return;
		fluidLevel.chase(tank.getFluidAmount() / (float) tank.getCapacity(), .25, Chaser.EXP);
		if (!world.isRemote) {
			markDirty();
			sendData();
		}
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!world.isRemote) {
			fluidLevel.forceNextSync();
			onFluidStackChanged(tank.getFluid());
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().expand(0, -2, 0);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
			.whileItemHeld(this::whenItemHeld);
		behaviours.add(beltProcessing);
	}

	protected ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (!FillingBySpout.canItemBeFilled(transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		if (FillingBySpout.getRequiredAmountForItem(transported.stack, tank.getFluid()) == -1)
			return PASS;
		return HOLD;
	}

	protected ProcessingResult whenItemHeld(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (processingTicks > 0)
			return HOLD;
		if (!FillingBySpout.canItemBeFilled(transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		FluidStack fluid = tank.getFluid();
		int requiredAmountForItem = FillingBySpout.getRequiredAmountForItem(transported.stack, fluid.copy());
		if (requiredAmountForItem == -1)
			return PASS;
		if (requiredAmountForItem > fluid.getAmount())
			return HOLD;

		if (processingTicks == -1) {
			processingTicks = FILLING_TIME;
			markDirty();
			sendData();
			return HOLD;
		}

		// Process finished

		processingTicks = -1;
		ItemStack out = FillingBySpout.fillItem(requiredAmountForItem, transported.stack, fluid);
		if (!out.isEmpty()) {
			List<TransportedItemStack> outList = new ArrayList<>();
			TransportedItemStack similar = transported.copy();
			similar.stack = out;
			// FIXME: original stack keeps waiting
			if (!transported.stack.isEmpty())
				outList.add(transported.copy());
			outList.add(similar);
			handler.handleProcessingOnItem(transported, outList);
		}
		tank.setFluid(fluid);
		sendSplash = true;
		markDirty();
		sendData();
		return PASS;
	}

	@Override
	public void remove() {
		capability.invalidate();
		super.remove();
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.put("TankContent", tank.writeToNBT(new CompoundNBT()));
		compound.put("Level", fluidLevel.writeNBT());
		compound.putInt("ProcessingTicks", processingTicks);
		if (sendSplash && clientPacket) {
			compound.putBoolean("Splash", true);
			sendSplash = false;
		}
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		tank.readFromNBT(compound.getCompound("TankContent"));
		fluidLevel.readNBT(compound.getCompound("Level"), clientPacket);
		processingTicks = compound.getInt("ProcessingTicks");

		if (!clientPacket)
			return;
		if (compound.contains("Splash"))
			spawnSplash(renderedFluid);
		if (!tank.getFluid()
			.isEmpty())
			renderedFluid = tank.getFluid();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != Direction.DOWN)
			return capability.cast();
		return super.getCapability(cap, side);
	}

	public Pair<FluidStack, LerpedFloat> getFluid() {
		return Pair.of(renderedFluid, fluidLevel);
	}

	public void sendDataImmediately() {
		syncCooldown = 0;
		queuedSync = false;
		sendData();
	}

	@Override
	public void tick() {
		super.tick();
		if (processingTicks > 0)
			processingTicks--;
		if (processingTicks >= 0 && world.isRemote)
			spawnProcessingParticles(renderedFluid);
		if (syncCooldown > 0) {
			syncCooldown--;
			if (syncCooldown == 0 && queuedSync)
				sendData();
		}
		if (fluidLevel != null)
			fluidLevel.tickChaser();
	}

	@Override
	public void sendData() {
		if (syncCooldown > 0) {
			queuedSync = true;
			return;
		}
		super.sendData();
		queuedSync = false;
		syncCooldown = SYNC_RATE;
	}

	protected void spawnProcessingParticles(FluidStack fluid) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		vec = vec.subtract(0, 8 / 16f, 0);
		IParticleData particle = new BlockParticleData(ParticleTypes.BLOCK, fluid.getFluid()
			.getDefaultState()
			.getBlockState());
		world.addOptionalParticle(particle, vec.x, vec.y, vec.z, 0, -.5f, 0);
	}

	protected static int SPLASH_PARTICLE_COUNT = 20;

	protected void spawnSplash(FluidStack fluid) {
		Vec3d vec = VecHelper.getCenterOf(pos);
		vec = vec.subtract(0, 2 - 5 / 16f, 0);
		IParticleData particle = new BlockParticleData(ParticleTypes.BLOCK, fluid.getFluid()
			.getDefaultState()
			.getBlockState());
		for (int i = 0; i < SPLASH_PARTICLE_COUNT; i++) {
			Vec3d m = VecHelper.offsetRandomly(Vec3d.ZERO, world.rand, 0.25f);
			m = new Vec3d(m.x, Math.abs(m.y), m.z);
			world.addOptionalParticle(particle, vec.x, vec.y, vec.z, m.x, m.y, m.z);
		}
	}

}
