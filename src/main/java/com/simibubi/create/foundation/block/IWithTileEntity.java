package com.simibubi.create.foundation.block;

import java.util.function.Consumer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface IWithTileEntity<T extends TileEntity> {

	default void withTileEntityDo(IBlockReader world, BlockPos pos, Consumer<T> action) {
		@SuppressWarnings("unchecked")
		T te = (T) world.getTileEntity(pos);
		if (te == null)
			return;
		action.accept(te);
	}
	
	default T getTileEntity(IBlockReader world, BlockPos pos) {
		@SuppressWarnings("unchecked")
		T te = (T) world.getTileEntity(pos);
		if (te == null)
			return null;
		return te;
	}
	
}
