package com.simibubi.create.content.contraptions.relays.encased;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SplitShaftTileEntity extends DirectionalShaftHalvesTileEntity {

	public SplitShaftTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	public abstract float getRotationSpeedModifier(Direction face);
	
}
