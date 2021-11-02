package com.simibubi.create.content.contraptions.relays.encased;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class SplitShaftTileEntity extends DirectionalShaftHalvesTileEntity {

	public SplitShaftTileEntity(BlockEntityType<?> typeIn) {
		super(typeIn);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
