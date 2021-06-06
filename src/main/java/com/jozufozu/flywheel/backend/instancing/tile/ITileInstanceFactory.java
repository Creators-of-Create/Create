package com.jozufozu.flywheel.backend.instancing.tile;

import com.jozufozu.flywheel.backend.instancing.MaterialManager;

import net.minecraft.tileentity.TileEntity;

@FunctionalInterface
public interface ITileInstanceFactory<T extends TileEntity> {
	TileEntityInstance<? super T> create(MaterialManager<?> manager, T te);
}
