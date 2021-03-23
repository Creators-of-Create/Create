package com.simibubi.create.content.contraptions.components.mixer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.ShaftlessCogInstance;
import com.simibubi.create.foundation.render.backend.RenderMaterials;
import com.simibubi.create.foundation.render.backend.instancing.*;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.util.Direction;

public class MixerInstance extends ShaftlessCogInstance implements IDynamicInstance {

    private final InstanceKey<RotatingData> mixerHead;
    private final InstanceKey<ModelData> mixerPole;

    public MixerInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        mixerHead = rotatingMaterial().getModel(AllBlockPartials.MECHANICAL_MIXER_HEAD, blockState)
                                      .createInstance();

        mixerHead.getInstance()
                 .setRotationAxis(Direction.Axis.Y);

        mixerPole = modelManager.getMaterial(RenderMaterials.TRANSFORMED)
                                .getModel(AllBlockPartials.MECHANICAL_MIXER_POLE, blockState)
                                .createInstance();


        MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tile;
        float renderedHeadOffset = getRenderedHeadOffset(mixer);

        transformPole(renderedHeadOffset);
        transformHead(mixer, renderedHeadOffset);
        updateLight();
    }

    @Override
    public void beginFrame() {
        MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tile;

        float renderedHeadOffset = getRenderedHeadOffset(mixer);

        if (mixer.running) {
            transformPole(renderedHeadOffset);
        }

        transformHead(mixer, renderedHeadOffset);
    }

    private void transformHead(MechanicalMixerTileEntity mixer, float renderedHeadOffset) {
        float speed = mixer.getRenderedHeadRotationSpeed(AnimationTickHolder.getPartialTicks());

        mixerHead.getInstance()
                 .setPosition(pos)
                 .nudge(0, -renderedHeadOffset, 0)
                 .setRotationalSpeed(speed * 2);
    }

    private void transformPole(float renderedHeadOffset) {
        MatrixStack ms = new MatrixStack();

        MatrixStacker msr = MatrixStacker.of(ms);
        msr.translate(getFloatingPos());
        msr.translate(0, -renderedHeadOffset, 0);

        mixerPole.getInstance().setTransform(ms);
    }

    private float getRenderedHeadOffset(MechanicalMixerTileEntity mixer) {
        return mixer.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
    }

    @Override
    public void updateLight() {
        super.updateLight();

        relight(pos.down(), mixerHead.getInstance());

        relight(pos, mixerPole.getInstance());
    }

    @Override
    public void remove() {
        super.remove();
        mixerHead.delete();
        mixerPole.delete();
    }
}
