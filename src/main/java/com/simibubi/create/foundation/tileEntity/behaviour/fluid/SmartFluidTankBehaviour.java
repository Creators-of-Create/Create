package com.simibubi.create.foundation.tileEntity.behaviour.fluid;

import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class SmartFluidTankBehaviour extends TileEntityBehaviour {

	public static BehaviourType<SmartFluidTankBehaviour>

	TYPE = new BehaviourType<>(), INPUT = new BehaviourType<>("Input"), OUTPUT = new BehaviourType<>("Output");

	private static final int SYNC_RATE = 8;

	protected int syncCooldown;
	protected boolean queuedSync;
	protected TankSegment[] tanks;
	protected LazyOptional<? extends IFluidHandler> capability;
	protected boolean extractionAllowed;
	protected boolean insertionAllowed;

	private BehaviourType<SmartFluidTankBehaviour> behaviourType;

	public static SmartFluidTankBehaviour single(SmartTileEntity te, int capacity) {
		return new SmartFluidTankBehaviour(TYPE, te, 1, capacity, false);
	}

	public SmartFluidTankBehaviour(BehaviourType<SmartFluidTankBehaviour> type, SmartTileEntity te, int tanks,
		int tankCapacity, boolean enforceVariety) {
		super(te);
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
		if (getWorld().isRemote)
			return;
		foreach(ts -> {
			ts.fluidLevel.forceNextSync();
			ts.onFluidStackChanged(ts.tank.getFluid());
		});
	}

	@Override
	public void tick() {
		super.tick();

		if (syncCooldown > 0) {
			syncCooldown--;
			if (syncCooldown == 0 && queuedSync)
				tileEntity.sendData();
		}

		foreach(te -> {
			LerpedFloat fluidLevel = te.getFluidLevel();
			if (fluidLevel != null)
				fluidLevel.tickChaser();
		});
	}

	public void sendDataImmediately() {
		syncCooldown = 0;
		queuedSync = false;
		tileEntity.sendData();
	}

	public void sendDataLazily() {
		if (syncCooldown > 0) {
			queuedSync = true;
			return;
		}
		tileEntity.sendData();
		queuedSync = false;
		syncCooldown = SYNC_RATE;
	}

	@Override
	public void remove() {
		super.remove();
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

	public void foreach(Consumer<TankSegment> action) {
		for (TankSegment tankSegment : tanks)
			action.accept(tankSegment);
	}

	public LazyOptional<? extends IFluidHandler> getCapability() {
		return capability;
	}

	@Override
	public void write(CompoundNBT nbt, boolean clientPacket) {
		super.write(nbt, clientPacket);
		ListNBT tanksNBT = new ListNBT();
		foreach(ts -> tanksNBT.add(ts.writeNBT()));
		nbt.put(getType().getName() + "Tanks", tanksNBT);
	}

	@Override
	public void read(CompoundNBT nbt, boolean clientPacket) {
		super.read(nbt, clientPacket);
		MutableInt index = new MutableInt(0);
		NBTHelper.iterateCompoundList(nbt.getList(getType().getName() + "Tanks", NBT.TAG_COMPOUND), c -> {
			if (index.intValue() >= tanks.length)
				return;
			tanks[index.intValue()].readNBT(c, clientPacket);
			index.increment();
		});
	}
	
	class InternalFluidHandler extends CombinedTankWrapper {
		
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
			tank = new SmartFluidTank(1000, f -> onFluidStackChanged(f));
			fluidLevel = LerpedFloat.linear()
				.startWithValue(0)
				.chase(0, .25, Chaser.EXP);
			renderedFluid = FluidStack.EMPTY;
		}

		protected void onFluidStackChanged(FluidStack newFluidStack) {
			if (!tileEntity.hasWorld())
				return;
			fluidLevel.chase(tank.getFluidAmount() / (float) tank.getCapacity(), .25, Chaser.EXP);
			if (!getWorld().isRemote)
				sendDataLazily();
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

		public CompoundNBT writeNBT() {
			CompoundNBT compound = new CompoundNBT();
			compound.put("TankContent", tank.writeToNBT(new CompoundNBT()));
			compound.put("Level", fluidLevel.writeNBT());
			return compound;
		}

		public void readNBT(CompoundNBT compound, boolean clientPacket) {
			tank.readFromNBT(compound.getCompound("TankContent"));
			fluidLevel.readNBT(compound.getCompound("Level"), clientPacket);
			if (!tank.getFluid()
				.isEmpty())
				renderedFluid = tank.getFluid();
		}

	}

	@Override
	public BehaviourType<?> getType() {
		return behaviourType;
	}
}
