package com.simibubi.create.foundation.render.instancing;

import net.minecraft.tileentity.TileEntity;

public interface IInstancedTileEntityRenderer<T extends TileEntity> {

    void addInstanceData(InstanceContext<T> te);
}
