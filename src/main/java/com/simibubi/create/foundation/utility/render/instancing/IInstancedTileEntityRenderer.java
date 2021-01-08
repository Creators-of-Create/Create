package com.simibubi.create.foundation.utility.render.instancing;

import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

import java.util.function.Supplier;

public interface IInstancedTileEntityRenderer<T extends TileEntity> {

    void addInstanceData(T te);
}
