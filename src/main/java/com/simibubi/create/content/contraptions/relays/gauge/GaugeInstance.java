package com.simibubi.create.content.contraptions.relays.gauge;

import java.util.ArrayList;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.relays.encased.ShaftInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public abstract class GaugeInstance extends ShaftInstance implements DynamicInstance {

    protected final ArrayList<DialFace> faces;

    protected PoseStack ms;

    protected GaugeInstance(MaterialManager dispatcher, KineticTileEntity tile) {
        super(dispatcher, tile);

        faces = new ArrayList<>(2);

        GaugeTileEntity gaugeTile = (GaugeTileEntity) tile;
        GaugeBlock gaugeBlock = (GaugeBlock) blockState.getBlock();

        Instancer<ModelData> dialModel = getTransformMaterial().getModel(AllBlockPartials.GAUGE_DIAL, blockState);
        Instancer<ModelData> headModel = getHeadModel();

        ms = new PoseStack();
        TransformStack msr = TransformStack.cast(ms);
        msr.translate(getInstancePosition());

        float progress = Mth.lerp(AnimationTickHolder.getPartialTicks(), gaugeTile.prevDialState, gaugeTile.dialState);

        for (Direction facing : Iterate.directions) {
            if (!gaugeBlock.shouldRenderHeadOnFace(world, pos, blockState, facing))
                continue;

            DialFace face = makeFace(facing, dialModel, headModel);

            faces.add(face);

            face.setupTransform(msr, progress);
        }
    }

    private DialFace makeFace(Direction face, Instancer<ModelData> dialModel, Instancer<ModelData> headModel) {
        return new DialFace(face, dialModel.createInstance(), headModel.createInstance());
    }

    @Override
    public void beginFrame() {
        GaugeTileEntity gaugeTile = (GaugeTileEntity) blockEntity;

        if (Mth.equal(gaugeTile.prevDialState, gaugeTile.dialState))
            return;

        float progress = Mth.lerp(AnimationTickHolder.getPartialTicks(), gaugeTile.prevDialState, gaugeTile.dialState);

        TransformStack msr = TransformStack.cast(ms);

        for (DialFace faceEntry : faces) {
            faceEntry.updateTransform(msr, progress);
        }
    }

    @Override
    public void updateLight() {
        super.updateLight();

        relight(pos, faces.stream()
                          .flatMap(Couple::stream));
    }

    @Override
    public void remove() {
        super.remove();

        faces.forEach(DialFace::delete);
    }

    protected abstract Instancer<ModelData> getHeadModel();

    private class DialFace extends Couple<ModelData> {

        Direction face;

        public DialFace(Direction face, ModelData first, ModelData second) {
            super(first, second);
            this.face = face;
        }

        private void setupTransform(TransformStack msr, float progress) {
            float dialPivot = 5.75f / 16;

            msr.pushPose();
            rotateToFace(msr);

            getSecond().setTransform(ms);

            msr.translate(0, dialPivot, dialPivot)
               .rotate(Direction.EAST, (float) (Math.PI / 2 * -progress))
               .translate(0, -dialPivot, -dialPivot);

            getFirst().setTransform(ms);

            msr.popPose();
        }

        private void updateTransform(TransformStack msr, float progress) {
            float dialPivot = 5.75f / 16;

            msr.pushPose();

            rotateToFace(msr)
                    .translate(0, dialPivot, dialPivot)
                    .rotate(Direction.EAST, (float) (Math.PI / 2 * -progress))
                    .translate(0, -dialPivot, -dialPivot);

            getFirst().setTransform(ms);

            msr.popPose();
        }

        protected TransformStack rotateToFace(TransformStack msr) {
            return msr.centre()
                      .rotate(Direction.UP, (float) ((-face.toYRot() - 90) / 180 * Math.PI))
                      .unCentre();
        }

        private void delete() {
            getFirst().delete();
            getSecond().delete();
        }
    }

    public static class Speed extends GaugeInstance {
        public Speed(MaterialManager dispatcher, KineticTileEntity tile) {
            super(dispatcher, tile);
        }

        @Override
        protected Instancer<ModelData> getHeadModel() {
            return getTransformMaterial().getModel(AllBlockPartials.GAUGE_HEAD_SPEED, blockState);
        }
    }

    public static class Stress extends GaugeInstance {
        public Stress(MaterialManager dispatcher, KineticTileEntity tile) {
            super(dispatcher, tile);
        }

        @Override
        protected Instancer<ModelData> getHeadModel() {
            return getTransformMaterial().getModel(AllBlockPartials.GAUGE_HEAD_STRESS, blockState);
        }
    }
}
