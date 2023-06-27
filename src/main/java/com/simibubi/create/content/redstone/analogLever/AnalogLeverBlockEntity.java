package com.simibubi.create.content.redstone.analogLever;

import java.util.List;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.utility.animation.LerpedFloat.Chaser;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AnalogLeverBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	int state = 0;
	int lastChange;
	LerpedFloat clientState;

	public AnalogLeverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		clientState = LerpedFloat.linear();
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
		clientState.chase(state, 0.2f, Chaser.EXP);
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
			clientState.tickChaser();
	}

	@Override
	public void initialize() {
		super.initialize();

	}

	private void updateOutput() {
		AnalogLeverBlock.updateNeighbors(getBlockState(), level, worldPosition);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
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
		tooltip.add(componentSpacing.plainCopy().append(Lang.translateDirect("tooltip.analogStrength", this.state)));

		return true;
	}

	public int getState() {
		return state;
	}
}
