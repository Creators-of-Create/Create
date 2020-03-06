package com.simibubi.create.modules.contraptions.relays.encased;

import com.simibubi.create.modules.contraptions.base.KineticTileEntity;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class DirectionalShaftHalvesTileEntity extends KineticTileEntity {
	
	public DirectionalShaftHalvesTileEntity(TileEntityType<?> typeIn) {
		super(typeIn);
	}

	public Direction getSourceFacing() {
		BlockPos source = getSource().subtract(getPos());
		return Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
	}

}
