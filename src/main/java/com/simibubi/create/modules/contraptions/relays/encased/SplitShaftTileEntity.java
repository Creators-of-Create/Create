package com.simibubi.create.modules.contraptions.relays.encased;

import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class SplitShaftTileEntity extends KineticTileEntity {

	public SplitShaftTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
