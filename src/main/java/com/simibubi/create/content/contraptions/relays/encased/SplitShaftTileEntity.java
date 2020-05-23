package com.simibubi.create.content.contraptions.relays.encased;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class SplitShaftTileEntity extends DirectionalShaftHalvesTileEntity {

	public SplitShaftTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
