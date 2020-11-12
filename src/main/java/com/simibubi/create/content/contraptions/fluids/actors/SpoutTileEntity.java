package com.simibubi.create.content.contraptions.fluids.actors;

import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.HOLD;
import static com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult.PASS;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.BeltProcessingBehaviour.ProcessingResult;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.fluid.SmartFluidTankBehaviour;
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

public class SpoutTileEntity extends SmartTileEntity {

	public static final int FILLING_TIME = 20;

	protected BeltProcessingBehaviour beltProcessing;
	protected int processingTicks;
	protected boolean sendSplash;

	SmartFluidTankBehaviour tank;

	public SpoutTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
		processingTicks = -1;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return super.getRenderBoundingBox().expand(0, -2, 0);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		tank = SmartFluidTankBehaviour.single(this, 1000);
		behaviours.add(tank);

		beltProcessing = new BeltProcessingBehaviour(this).whenItemEnters(this::onItemReceived)
			.whileItemHeld(this::whenItemHeld);
		behaviours.add(beltProcessing);

	}

	protected ProcessingResult onItemReceived(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (!FillingBySpout.canItemBeFilled(world, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		if (FillingBySpout.getRequiredAmountForItem(world, transported.stack, getCurrentFluidInTank()) == -1)
			return PASS;
		return HOLD;
	}

	protected ProcessingResult whenItemHeld(TransportedItemStack transported,
		TransportedItemStackHandlerBehaviour handler) {
		if (processingTicks != -1 && processingTicks != 5)
			return HOLD;
		if (!FillingBySpout.canItemBeFilled(world, transported.stack))
			return PASS;
		if (tank.isEmpty())
			return HOLD;
		FluidStack fluid = getCurrentFluidInTank();
		int requiredAmountForItem = FillingBySpout.getRequiredAmountForItem(world, transported.stack, fluid.copy());
		if (requiredAmountForItem == -1)
			return PASS;
		if (requiredAmountForItem > fluid.getAmount())
			return HOLD;

		if (processingTicks == -1) {
			processingTicks = FILLING_TIME;
			notifyUpdate();
			return HOLD;
		}

		// Process finished
		ItemStack out = FillingBySpout.fillItem(world, requiredAmountForItem, transported.stack, fluid);
		if (!out.isEmpty()) {
			List<TransportedItemStack> outList = new ArrayList<>();
			TransportedItemStack held = null;
			TransportedItemStack result = transported.copy();
			result.stack = out;
			if (!transported.stack.isEmpty())
				held = transported.copy();
			outList.add(result);
			handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(outList, held));
		}

		tank.getPrimaryHandler()
			.setFluid(fluid);
		sendSplash = true;
		notifyUpdate();
		return PASS;
	}

	private FluidStack getCurrentFluidInTank() {
		return tank.getPrimaryHandler()
			.getFluid();
	}

	@Override
	protected void write(CompoundNBT compound, boolean clientPacket) {
		super.write(compound, clientPacket);

		compound.putInt("ProcessingTicks", processingTicks);
		if (sendSplash && clientPacket) {
			compound.putBoolean("Splash", true);
			sendSplash = false;
		}
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		processingTicks = compound.getInt("ProcessingTicks");
		if (!clientPacket)
			return;
		if (compound.contains("Splash"))
			spawnSplash(tank.getPrimaryTank()
				.getRenderedFluid());
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != Direction.DOWN)
			return tank.getCapability()
				.cast();
		return super.getCapability(cap, side);
	}

	public void tick() {
		super.tick();
		if (processingTicks >= 0)
			processingTicks--;
		if (processingTicks >= 8 && world.isRemote)
			spawnProcessingParticles(tank.getPrimaryTank()
				.getRenderedFluid());
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
