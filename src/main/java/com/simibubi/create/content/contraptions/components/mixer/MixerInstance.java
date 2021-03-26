package com.simibubi.create.content.contraptions.components.mixer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.ShaftlessCogInstance;
import com.simibubi.create.foundation.render.backend.instancing.IDynamicInstance;
import com.simibubi.create.foundation.render.backend.instancing.InstanceKey;
import com.simibubi.create.foundation.render.backend.instancing.InstancedTileRenderer;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelData;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.MatrixStacker;
import net.minecraft.util.Direction;

public class MixerInstance extends ShaftlessCogInstance implements IDynamicInstance {

    private final InstanceKey<RotatingData> mixerHead;
    private final InstanceKey<ModelData> mixerPole;

    public MixerInstance(InstancedTileRenderer<?> dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        mixerHead = getRotatingMaterial().getModel(AllBlockPartials.MECHANICAL_MIXER_HEAD, blockState)
                                      .createInstance();

        mixerHead.getInstance()
                 .setRotationAxis(Direction.Axis.Y);

        mixerPole = getTransformMaterial()
                                .getModel(AllBlockPartials.MECHANICAL_MIXER_POLE, blockState)
                                .createInstance();


        MechanicalMixerTileEntity mixer = (MechanicalMixerTileEntity) tile;
        float renderedHeadOffset = getRenderedHeadOffset(mixer);

        transformPole(renderedHeadOffset);
        transformHead(mixer, renderedHeadOffset);
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
                 .setPosition(getInstancePosition())
                 .nudge(0, -renderedHeadOffset, 0)
                 .setRotationalSpeed(speed * 2);
    }

    private void transformPole(float renderedHeadOffset) {
        MatrixStack ms = new MatrixStack();

        MatrixStacker msr = MatrixStacker.of(ms);
        msr.translate(getInstancePosition());
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
