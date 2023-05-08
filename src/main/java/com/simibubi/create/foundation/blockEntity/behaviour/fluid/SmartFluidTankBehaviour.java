package com.simibubi.create.foundation.blockEntity.behaviour.fluid;

import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.foundation.blockEntity.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class SmartFluidTankBehaviour extends BlockEntityBehaviour {

	public static final BehaviourType<SmartFluidTankBehaviour>

	TYPE = new BehaviourType<>(), INPUT = new BehaviourType<>("Input"), OUTPUT = new BehaviourType<>("Output");

	private static final int SYNC_RATE = 8;

	protected int syncCooldown;
	protected boolean queuedSync;
	protected TankSegment[] tanks;
	protected LazyOptional<? extends IFluidHandler> capability;
	protected boolean extractionAllowed;
	protected boolean insertionAllowed;
	protected Runnable fluidUpdateCallback;

	private BehaviourType<SmartFluidTankBehaviour> behaviourType;

	public static SmartFluidTankBehaviour single(SmartBlockEntity be, int capacity) {
		return new SmartFluidTankBehaviour(TYPE, be, 1, capacity, false);
	}

	public SmartFluidTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type, SmartBlockEntity be, int tanks,
		int tankCapacity, boolean enforceVariety) {
		super(be);
		insertionAllowed = true;
		extractionAllowed = true;
		behaviourType = type;
		this.tanks = new TankSegment[tanks];
		IFluidHandler[] handlers = new IFluidHandler[tanks];
		for (int i = 0; i < tanks; i++) {
			TankSegment tankSegment = new TankSegment(tankCapacity);
			this.tanks[i] = tankSegment;
			handlers[i] = tankSegment.tank;
		}
		capability = LazyOptional.of(() -> new InternalFluidHandler(handlers, enforceVariety));
		fluidUpdateCallback = () -> {
		};
	}

	public SmartFluidTankBehaviour whenFluidUpdates(Runnable fluidUpdateCallback) {
		this.fluidUpdateCallback = fluidUpdateCallback;
		return this;
	}

	public SmartFluidTankBehaviour allowInsertion() {
		insertionAllowed = true;
		return this;
	}

	public SmartFluidTankBehaviour allowExtraction() {
		extractionAllowed = true;
		return this;
	}

	public SmartFluidTankBehaviour forbidInsertion() {
		insertionAllowed = false;
		return this;
	}

	public SmartFluidTankBehaviour forbidExtraction() {
		extractionAllowed = false;
		return this;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (getWorld().isClientSide)
			return;
		forEach(ts -> {
			ts.fluidLevel.forceNextSync();
			ts.onFluidStackChanged();
		});
	}

	@Override
	public void tick() {
		super.tick();

		if (syncCooldown > 0) {
			syncCooldown--;
			if (syncCooldown == 0 && queuedSync)
				updateFluids();
		}

		forEach(be -> {
			LerpedFloat fluidLevel = be.getFluidLevel();
			if (fluidLevel != null)
				fluidLevel.tickChaser();
		});
	}

	public void sendDataImmediately() {
		syncCooldown = 0;
		queuedSync = false;
		updateFluids();
	}

	public void sendDataLazily() {
		if (syncCooldown > 0) {
			queuedSync = true;
			return;
		}
		updateFluids();
		queuedSync = false;
		syncCooldown = SYNC_RATE;
	}

	protected void updateFluids() {
		fluidUpdateCallback.run();
		blockEntity.sendData();
		blockEntity.setChanged();
	}

	@Override
	public void unload() {
		super.unload();
		capability.invalidate();
	}

	public SmartFluidTank getPrimaryHandler() {
		return getPrimaryTank().tank;
	}

	public TankSegment getPrimaryTank() {
		return tanks[0];
	}

	public TankSegment[] getTanks() {
		return tanks;
	}

	public boolean isEmpty() {
		for (TankSegment tankSegment : tanks)
			if (!tankSegment.tank.isEmpty())
				return false;
		return true;
	}

	public void forEach(Consumer<TankSegment> action) {
		for (TankSegment tankSegment : tanks)
			action.accept(tankSegment);
	}

	public LazyOptional<? extends IFluidHandler> getCapability() {
		return capability;
	}

	@Override
	public void write(CompoundTag nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		ListTag tanksNBT = new ListTag();
		forEach(ts -> tanksNBT.add(ts.writeNBT()));
		nbt.put(getType().getName() + "Tanks", tanksNBT);
	}

	@Override
	public void read(CompoundTag nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);
		MutableInt index = new MutableInt(0);
		NBTHelper.iterateCompoundList(nbt.getList(getType().getName() + "Tanks", Tag.TAG_COMPOUND), c -> {
			if (index.intValue() >= tanks.length)
				return;
			tanks[index.intValue()].readNBT(c, clientPacket);
			index.increment();
		});
	}

	public class InternalFluidHandler extends CombinedTankWrapper {

		public InternalFluidHandler(IFluidHandler[] handlers, boolean enforceVariety) {
			super(handlers);
			if (enforceVariety)
				enforceVariety();
		}

		@Override
		public int fill(FluidStack resource, FluidAction action) {
			if (!insertionAllowed)
				return 0;
			return super.fill(resource, action);
		}

		public int forceFill(FluidStack resource, FluidAction action) {
			return super.fill(resource, action);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action) {
			if (!extractionAllowed)
				return FluidStack.EMPTY;
			return super.drain(resource, action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action) {
			if (!extractionAllowed)
				return FluidStack.EMPTY;
			return super.drain(maxDrain, action);
		}

	}

	public class TankSegment {

		protected SmartFluidTank tank;
		protected LerpedFloat fluidLevel;
		protected FluidStack renderedFluid;

		public TankSegment(int capacity) {
			tank = new SmartFluidTank(capacity, f -> onFluidStackChanged());
			fluidLevel = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .25, Chaser.EXP);
			renderedFluid = FluidStack.EMPTY;
		}

		public void onFluidStackChanged() {
			if (!blockEntity.hasLevel())
				return;
			fluidLevel.chase(tank.getFluidAmount() / (float) tank.getCapacity(), .25, Chaser.EXP);
			if (!getWorld().isClientSide)
				sendDataLazily();
			if (blockEntity.isVirtual() && !tank.getFluid()
				.isEmpty())
				renderedFluid = tank.getFluid();
		}

		public FluidStack getRenderedFluid() {
			return renderedFluid;
		}

		public LerpedFloat getFluidLevel() {
			return fluidLevel;
		}

		public float getTotalUnits(float partialTicks) {
			return fluidLevel.getValue(partialTicks) * tank.getCapacity();
		}

		public CompoundTag writeNBT() {
			CompoundTag compound = new CompoundTag();
			compound.put("TankContent", tank.writeToNBT(new CompoundTag()));
			compound.put("Level", fluidLevel.writeNBT());
			return compound;
		}

		public void readNBT(CompoundTag compound, boolean clientPacket) {
			tank.readFromNBT(compound.getCompound("TankContent"));
			fluidLevel.readNBT(compound.getCompound("Level"), clientPacket);
			if (!tank.getFluid()
				.isEmpty())
				renderedFluid = tank.getFluid();
		}

		public boolean isEmpty(float partialTicks) {
			FluidStack renderedFluid = getRenderedFluid();
			if (renderedFluid.isEmpty())
				return true;
			float units = getTotalUnits(partialTicks);
			if (units < 1)
				return true;
			return false;
		}

	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}
}
