package com.jozufozu.flywheel.backend.instancing;

import net.minecraft.tileentity.TileEntity;

@FunctionalInterface
public interface ITileInstanceFactory<T extends TileEntity> {
	TileEntityInstance<? super T> create(MaterialManager<?> manager, T te);
}
