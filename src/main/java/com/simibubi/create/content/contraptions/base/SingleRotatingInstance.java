package com.simibubi.create.content.contraptions.base;

import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.backend.material.MaterialManager;

import net.minecraft.world.level.block.state.BlockState;

public class SingleRotatingInstance extends KineticTileInstance<KineticTileEntity> {

    protected final RotatingData rotatingModel;

    public SingleRotatingInstance(MaterialManager modelManager, KineticTileEntity tile) {
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

    protected Instancer<RotatingData> getModel() {
        return getRotatingMaterial().getModel(getRenderedBlockState());
    }
}
