package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.network.chat.Component;

public class GaugeTileEntity extends KineticTileEntity implements IHaveGoggleInformation {

	public float dialTarget;
	public float dialState;
	public float prevDialState;
	public int color;

	public GaugeTileEntity(BlockPos pos, BlockState state, BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public void write(CompoundTag compound, boolean clientPacket) {
		compound.putFloat("Value", dialTarget);
		compound.putInt("Color", color);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundTag compound, boolean clientPacket) {
		dialTarget = compound.getFloat("Value");
		color = compound.getInt("Color");
		super.fromTag(state, compound, clientPacket);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		super.tick(level, pos, state, blockEntity);
		prevDialState = dialState;
		dialState += (dialTarget - dialState) * .125f;
		if (dialState > 1 && level.random.nextFloat() < 1 / 2f)
			dialState -= (dialState - 1) * level.random.nextFloat();
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.gauge.info_header")));

		return true;
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}
}
