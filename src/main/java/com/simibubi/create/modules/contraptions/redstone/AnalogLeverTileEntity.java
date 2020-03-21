package com.simibubi.create.modules.contraptions.redstone;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

public class AnalogLeverTileEntity extends SmartTileEntity {

	int state = 0;
	int lastChange;
	InterpolatedChasingValue clientState = new InterpolatedChasingValue().withSpeed(.2f);

	public AnalogLeverTileEntity() {
		super(AllTileEntities.ANALOG_LEVER.type);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("State", state);
		compound.putInt("ChangeTimer", lastChange);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		state = compound.getInt("State");
		lastChange = compound.getInt("ChangeTimer");
		clientState.target(state);
		super.read(compound);
	}

	@Override
	public void tick() {
		super.tick();
		if (lastChange > 0) {
			lastChange--;
			if (lastChange == 0)
				updateOutput();
		}
		if (world.isRemote)
			clientState.tick();
	}

	private void updateOutput() {
		AnalogLeverBlock.updateNeighbors(getBlockState(), world, pos);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	public void changeState(boolean back) {
		int prevState = state;
		state += back ? -1 : 1;
		state = MathHelper.clamp(state, 0, 15);
		if (prevState != state)
			lastChange = 15;
		sendData();
	}

	public int getState() {
		return state;
	}
}
