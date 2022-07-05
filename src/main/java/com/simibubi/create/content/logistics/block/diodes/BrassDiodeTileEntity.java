package com.simibubi.create.content.logistics.block.diodes;

import static com.simibubi.create.content.logistics.block.diodes.BrassDiodeBlock.POWERING;

import java.util.List;

import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueBehaviour.StepContext;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BrassDiodeTileEntity extends SmartTileEntity {

	protected int state;
	ScrollValueBehaviour maxState;

	public BrassDiodeTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		maxState = new ScrollValueBehaviour(Lang.translateDirect("generic.delay"), this, new BrassDiodeScrollSlot())
			.between(2, 60 * 20 * 30);
		maxState.withStepFunction(this::step);
		maxState.withFormatter(this::format);
		maxState.withUnit(this::getUnit);
		maxState.withCallback(this::onMaxDelayChanged);
		behaviours.add(maxState);
	}

	public float getProgress() {
		int max = Math.max(2, maxState.getValue());
		return Mth.clamp(state, 0, max) / (float) max;
	}

	public boolean isIdle() {
		return state == 0;
	}

	@Override
	public void tick() {
		super.tick();
		boolean powered = getBlockState().getValue(DiodeBlock.POWERED);
		boolean powering = getBlockState().getValue(POWERING);
		boolean atMax = state >= maxState.getValue();
		boolean atMin = state <= 0;
		updateState(powered, powering, atMax, atMin);
	}

	protected abstract void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin);

	private void onMaxDelayChanged(int newMax) {
		state = Mth.clamp(state, 0, newMax);
		sendData();
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		state = compound.getInt("State");
		super.read(compound, clientPacket);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putInt("State", state);
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
			return Lang.translateDirect("generic.unit.ticks");
		if (value < 20 * 60)
			return Lang.translateDirect("generic.unit.seconds");
		return Lang.translateDirect("generic.unit.minutes");
	}

}
