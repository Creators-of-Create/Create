package com.simibubi.create.foundation.block;

import java.util.function.Consumer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface IWithTileEntity<T extends TileEntity> {

	default void withTileEntityDo(IWorld world, BlockPos pos, Consumer<T> action) {
		@SuppressWarnings("unchecked")
		T te = (T) world.getTileEntity(pos);
		if (te == null)
			return;
		action.accept(te);
	}
	
}
