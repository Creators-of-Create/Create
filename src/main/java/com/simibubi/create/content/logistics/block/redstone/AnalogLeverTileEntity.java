package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.gui.widgets.InterpolatedChasingValue;
import com.simibubi.create.foundation.render.backend.instancing.IInstanceRendered;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;

public class AnalogLeverTileEntity extends SmartTileEntity implements IHaveGoggleInformation, IInstanceRendered {

	int state = 0;
	int lastChange;
	InterpolatedChasingValue clientState = new InterpolatedChasingValue().withSpeed(.2f);

	public AnalogLeverTileEntity(TileEntityType<? extends AnalogLeverTileEntity> type) {
		super(type);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putInt("State", state);
		compound.putInt("ChangeTimer", lastChange);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundNBT compound, boolean clientPacket) {
		state = compound.getInt("State");
		lastChange = compound.getInt("ChangeTimer");
		clientState.target(state);
		super.read(compound, clientPacket);
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

	@Override
	public void initialize() {
		super.initialize();

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

	@Override
	public boolean addToGoggleTooltip(List<String> tooltip, boolean isPlayerSneaking) {
		tooltip.add(spacing + Lang.translate("tooltip.analogStrength", this.state));

		return true;
	}

	public int getState() {
		return state;
	}
}
