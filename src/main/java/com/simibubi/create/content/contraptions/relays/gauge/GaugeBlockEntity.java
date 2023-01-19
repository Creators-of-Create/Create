package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GaugeBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

	public float dialTarget;
	public float dialState;
	public float prevDialState;
	public int color;

	public GaugeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("Value", dialTarget);
		compound.putInt("Color", color);
		super.write(compound, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, boolean clientPacket) {
		dialTarget = compound.getFloat("Value");
		color = compound.getInt("Color");
		super.read(compound, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();
		prevDialState = dialState;
		dialState += (dialTarget - dialState) * .125f;
		if (dialState > 1 && level.random.nextFloat() < 1 / 2f)
			dialState -= (dialState - 1) * level.random.nextFloat();
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(componentSpacing.plainCopy().append(Lang.translateDirect("gui.gauge.info_header")));

		return true;
	}
}
