package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.InstancedTileRenderDispatcher;
import com.simibubi.create.foundation.render.instancing.InstanceKey;
import com.simibubi.create.foundation.render.instancing.InstancedModel;
import com.simibubi.create.foundation.render.instancing.InstancedTileRenderRegistry;
import com.simibubi.create.foundation.render.instancing.RotatingData;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import static com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer.KINETIC_TILE;

public class SingleRotatingInstance extends KineticTileInstance<KineticTileEntity> {
    public static void register(TileEntityType<? extends KineticTileEntity> type) {
        InstancedTileRenderRegistry.instance.register(type, SingleRotatingInstance::new);
    }

    protected InstanceKey<RotatingData> rotatingModelKey;

    public SingleRotatingInstance(InstancedTileRenderDispatcher modelManager, KineticTileEntity tile) {
        super(modelManager, tile);
    }

    @Override
    protected void init() {
        Direction.Axis axis = ((IRotate) tile.getBlockState().getBlock()).getRotationAxis(tile.getBlockState());
        rotatingModelKey = getModel().setupInstance(setupFunc(tile.getSpeed(), axis));
    }

    @Override
    public void onUpdate() {
        Direction.Axis axis = ((IRotate) tile.getBlockState().getBlock()).getRotationAxis(tile.getBlockState());
        updateRotation(rotatingModelKey, axis);
    }

    @Override
    public void updateLight() {
        rotatingModelKey.modifyInstance(this::relight);
    }

    @Override
    public void remove() {
        rotatingModelKey.delete();
        rotatingModelKey = null;
    }

    protected BlockState getRenderedBlockState() {
        return tile.getBlockState();
    }

    protected InstancedModel<RotatingData> getModel() {
        return rotatingMaterial().getModel(KINETIC_TILE, getRenderedBlockState());
    }
}
