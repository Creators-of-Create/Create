package com.simibubi.create.content.contraptions.components.press;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;

public class PressInstance extends ShaftInstance implements IDynamicInstance {

    private final InstanceKey<ModelData> pressHead;

    public PressInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        pressHead = modelManager.getMaterial(RenderMaterials.MODELS)
                                .getModel(AllBlockPartials.MECHANICAL_PRESS_HEAD, blockState)
                                .createInstance();

        updateLight();
        transformModels((MechanicalPressTileEntity) tile);
    }

    @Override
    public void beginFrame() {
        MechanicalPressTileEntity press = (MechanicalPressTileEntity) tile;
        if (!press.running)
            return;

        transformModels(press);
    }

    private void transformModels(MechanicalPressTileEntity press) {
        float renderedHeadOffset = getRenderedHeadOffset(press);

        MatrixStack ms = new MatrixStack();

        MatrixStacker msr = MatrixStacker.of(ms);
        msr.translate(getFloatingPos());
        msr.translate(0, -renderedHeadOffset, 0);

        pressHead.getInstance()
                 .setTransformNoCopy(ms);
    }

    private float getRenderedHeadOffset(MechanicalPressTileEntity press) {
        return press.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
    }

    @Override
    public void updateLight() {
        super.updateLight();

        relight(pos, pressHead.getInstance());
    }

    @Override
    public void remove() {
        super.remove();
        pressHead.delete();
    }
}
