package com.simibubi.create.foundation.utility.render.instancing;

import net.minecraft.tileentity.TileEntity;

import java.util.function.Supplier;

public abstract class InstancedTileEntityRenderer<T extends TileEntity, D extends InstanceData> {

    public abstract D getInstanceData(T te);

    public abstract InstanceBuffer<D> getModel(T te);
}
