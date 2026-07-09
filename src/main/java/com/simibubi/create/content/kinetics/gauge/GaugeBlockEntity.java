package com.simibubi.create.content.kinetics.gauge;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GaugeBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation {

	public float dialTarget;
	public float dialState;
	public float prevDialState;
	public int color;

	public GaugeBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	@Override
	public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		compound.putFloat("Value", dialTarget);
		compound.putInt("Color", color);
		super.write(compound, registries, clientPacket);
	}

	@Override
	protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
		dialTarget = compound.getFloatOr("Value", 0);
		color = compound.getIntOr("Color", 0);
		super.read(compound, registries, clientPacket);
	}

	@Override
	public void tick() {
		super.tick();
		prevDialState = dialState;
		dialState += (dialTarget - dialState) * .125f;
		if (dialState > 1 && level.getRandom().nextFloat() < 1 / 2f)
			dialState -= (dialState - 1) * level.getRandom().nextFloat();
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

		return true;
	}

}
