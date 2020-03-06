package com.simibubi.create.modules.contraptions.relays.encased;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class SplitShaftTileEntity extends DirectionalShaftHalvesTileEntity {

	public SplitShaftTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
