package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.List;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;

public class GaugeTileEntity extends KineticTileEntity implements IHaveGoggleInformation {

	public float dialTarget;
	public float dialState;
	public float prevDialState;
	public int color;

	public GaugeTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void write(CompoundNBT compound, boolean clientPacket) {
		compound.putFloat("Value", dialTarget);
		compound.putInt("Color", color);
		super.write(compound, clientPacket);
	}

	@Override
	protected void fromTag(BlockState state, CompoundNBT compound, boolean clientPacket) {
		dialTarget = compound.getFloat("Value");
		color = compound.getInt("Color");
		super.fromTag(state, compound, clientPacket);
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
	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		tooltip.add(componentSpacing.plainCopy().append(Lang.translate("gui.gauge.info_header")));

		return true;
	}

	@Override
	public boolean shouldRenderNormally() {
		return true;
	}
}
