package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public class AdjustablePulleyTileEntity extends KineticTileEntity {

	int signal;
	boolean signalChanged;

	public AdjustablePulleyTileEntity(TileEntityType<? extends AdjustablePulleyTileEntity> type) {
		super(type);
		signal = 0;
		setLazyTickRate(40);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("Signal", signal);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		signal = compound.getInt("Signal");
		super.read(compound);
	}

	public float getModifier() {
		return getModifierForSignal(signal);
	}

	public void neighborChanged() {
		if (!hasWorld())
			return;
		int power = world.getRedstonePowerFromNeighbors(pos);
		if (power != signal) 
			signalChanged = true;
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		neighborChanged();
	}

	@Override
	public void tick() {
		super.tick();
		if (signalChanged) {
			signalChanged = false;
			analogSignalChanged(world.getRedstonePowerFromNeighbors(pos));
		}
	}

	protected void analogSignalChanged(int newSignal) {
		detachKinetics();
		removeSource();
		signal = newSignal;
		attachKinetics();
	}

	protected float getModifierForSignal(int newPower) {
		if (newPower == 0)
			return 1;
		return 1 + ((newPower + 1) / 16f);
	}

}
