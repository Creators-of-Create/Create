package com.simibubi.create.content.logistics.block.redstone;

import java.util.List;

import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.InterpolatedChasingValue;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AnalogLeverTileEntity extends SmartTileEntity implements IHaveGoggleInformation {

	int state = 0;
	int lastChange;
	InterpolatedChasingValue clientState = new InterpolatedChasingValue().withSpeed(.2f);

	public AnalogLeverTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("State", state);
		compound.putInt("ChangeTimer", lastChange);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
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
		if (level.isClientSide)
			clientState.tick();
	}

	@Override
	public void initialize() {
		super.initialize();

	}

	private void updateOutput() {
		AnalogLeverBlock.updateNeighbors(getBlockState(), level, worldPosition);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
	}

	public void changeState(boolean back) {
		int prevState = state;
		state += back ? -1 : 1;
		state = Mth.clamp(state, 0, 15);
		if (prevState != state)
			lastChange = 15;
		sendData();
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(componentSpacing.plainCopy().append(Lang.translate("tooltip.analogStrength", this.state)));

		return true;
	}

	public int getState() {
		return state;
	}
}
