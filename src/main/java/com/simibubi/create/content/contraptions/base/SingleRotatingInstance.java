package com.simibubi.create.content.contraptions.base;

import com.simibubi.create.foundation.render.backend.instancing.InstancedModel;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;

import net.minecraft.block.BlockState;

public class SingleRotatingInstance extends KineticTileInstance<KineticTileEntity> {

    protected final RotatingData rotatingModel;

    public SingleRotatingInstance(InstancedTileRenderer<?> modelManager, KineticTileEntity tile) {
        super(modelManager, tile);

        rotatingModel = setup(getModel().createInstance());
    }

    @Override
    public void update() {
        updateRotation(rotatingModel);
    }

    @Override
    public void updateLight() {
        relight(pos, rotatingModel);
    }

    @Override
    public void remove() {
        rotatingModel.delete();
    }

    protected BlockState getRenderedBlockState() {
        return blockState;
    }

    protected InstancedModel<RotatingData> getModel() {
        return getRotatingMaterial().getModel(getRenderedBlockState());
    }
}
