package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class SidedAxisTunnelTileEntity extends KineticTileEntity {

	public SidedAxisTunnelTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
