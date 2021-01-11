package com.simibubi.create.foundation.utility.render.instancing;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.render.FastContraptionRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IInstancedTileEntityRenderer<T extends TileEntity> {

    void addInstanceData(InstanceContext<T> te);
}
