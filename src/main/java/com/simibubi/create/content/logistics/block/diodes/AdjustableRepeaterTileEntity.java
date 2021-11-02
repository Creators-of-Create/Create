package com.simibubi.create.content.logistics.block.diodes;

import static com.simibubi.create.content.logistics.block.diodes.AdjustableRepeaterBlock.POWERING;
import static net.minecraft.block.RedstoneDiodeBlock.POWERED;

import java.util.List;

import com.jozufozu.flywheel.backend.instancing.IInstanceRendered;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AdjustableRepeaterTileEntity extends SmartTileEntity implements IInstanceRendered {

	public int state;
	public boolean charging;
	ScrollValueBehaviour maxState;

	public AdjustableRepeaterTileEntity(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		maxState = new ScrollValueBehaviour(Lang.translate("generic.delay"), this, new AdjustableRepeaterScrollSlot())
				.between(1, 60 * 20 * 30);
		maxState.withStepFunction(this::step);
		maxState.withFormatter(this::format);
		maxState.withUnit(this::getUnit);
		maxState.withCallback(this::onMaxDelayChanged);

		behaviours.add(maxState);
	}

	private void onMaxDelayChanged(int newMax) {
		state = Mth.clamp(state, 0, newMax);
		sendData();
	}

	@Override
	protected void fromTag(BlockState blockState, CompoundTag compound, boolean clientPacket) {
		state = compound.getInt("State");
		charging = compound.getBoolean("Charging");
		super.fromTag(blockState, compound, clientPacket);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("State", state);
		compound.putBoolean("Charging", charging);
		super.write(compound, clientPacket);
	}

	private int step(StepContext context) {
		int value = context.currentValue;
		if (!context.forward)
			value--;

		if (value < 20)
			return 1;
		if (value < 20 * 60)
			return 20;
		return 20 * 60;
	}

	private String format(int value) {
		if (value < 20)
			return value + "t";
		if (value < 20 * 60)
			return (value / 20) + "s";
		return (value / 20 / 60) + "m";
	}

	private Component getUnit(int value) {
		if (value < 20)
			return Lang.translate("generic.unit.ticks");
		if (value < 20 * 60)
			return Lang.translate("generic.unit.seconds");
		return Lang.translate("generic.unit.minutes");
	}

	@Override
	public void tick() {
		super.tick();
		boolean powered = getBlockState().getValue(POWERED);
		boolean powering = getBlockState().getValue(POWERING);
		boolean atMax = state >= maxState.getValue();
		boolean atMin = state <= 0;
		updateState(powered, powering, atMax, atMin);
	}

	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (!charging && powered)
			charging = true;

		if (charging && atMax) {
			if (!powering && !level.isClientSide)
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, true));
			if (!powered)
				charging = false;
			return;
		}

		if (!charging && atMin) {
			if (powering && !level.isClientSide)
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(POWERING, false));
			return;
		}

		state += charging ? 1 : -1;
	}
}
