package com.simibubi.create.foundation.render.instancing;

import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import net.minecraft.tileentity.TileEntity;

@FunctionalInterface
public interface IRendererFactory<T extends TileEntity> {
    TileEntityInstance<? super T> create(InstancedTileRenderDispatcher manager, T te);
}
