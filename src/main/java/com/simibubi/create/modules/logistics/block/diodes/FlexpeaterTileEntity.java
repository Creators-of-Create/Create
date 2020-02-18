package com.simibubi.create.modules.logistics.block.diodes;

import static com.simibubi.create.modules.logistics.block.diodes.FlexpeaterBlock.POWERING;
import static net.minecraft.block.RedstoneDiodeBlock.POWERED;

import java.util.List;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.base.TileEntityBehaviour;
import com.simibubi.create.foundation.behaviour.scrollvalue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;

public class FlexpeaterTileEntity extends SmartTileEntity {

	public int state;
	public boolean charging;
	ScrollValueBehaviour maxState;

	public FlexpeaterTileEntity() {
		this(AllTileEntities.FLEXPEATER.type);
	}

	protected FlexpeaterTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void addBehaviours(List<TileEntityBehaviour> behaviours) {
		maxState = new ScrollValueBehaviour(Lang.translate("generic.delay"), this, new FlexpeaterScrollSlot())
				.between(1, 60 * 20 * 30);
		maxState.withStepFunction(this::step);
		maxState.withFormatter(this::format);
		maxState.withUnit(this::getUnit);
		behaviours.add(maxState);
	}

	@Override
	public void read(CompoundNBT compound) {
		state = compound.getInt("State");
		charging = compound.getBoolean("Charging");
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("State", state);
		compound.putBoolean("Charging", charging);
		return super.write(compound);
	}

	private int step(int value, boolean positive) {
		if (!positive)
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

	private String getUnit(int value) {
		if (value < 20)
			return Lang.translate("generic.unit.ticks");
		if (value < 20 * 60)
			return Lang.translate("generic.unit.seconds");
		return Lang.translate("generic.unit.minutes");
	}

	@Override
	public void tick() {
		super.tick();
		boolean powered = getBlockState().get(POWERED);
		boolean powering = getBlockState().get(POWERING);
		boolean atMax = state >= maxState.getValue();
		boolean atMin = state <= 0;
		updateState(powered, powering, atMax, atMin);
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	protected void updateState(boolean powered, boolean powering, boolean atMax, boolean atMin) {
		if (!charging && powered)
			charging = true;

		if (charging && atMax) {
			if (!powering && !world.isRemote)
				world.setBlockState(pos, getBlockState().with(POWERING, true));
			if (!powered)
				charging = false;
			return;
		}

		if (!charging && atMin) {
			if (powering && !world.isRemote)
				world.setBlockState(pos, getBlockState().with(POWERING, false));
			return;
		}

		state += charging ? 1 : -1;
	}

}
