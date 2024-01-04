package com.simibubi.create.content.kinetics.gauge;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public abstract class GaugeInstance extends ShaftInstance<GaugeBlockEntity> implements DynamicVisual {

    protected final ArrayList<DialFace> faces;

    protected PoseStack ms;

    protected GaugeInstance(VisualizationContext materialManager, GaugeBlockEntity blockEntity) {
        super(materialManager, blockEntity);

        faces = new ArrayList<>(2);

        GaugeBlock gaugeBlock = (GaugeBlock) blockState.getBlock();

        Instancer<TransformedInstance> dialModel = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GAUGE_DIAL), RenderStage.AFTER_BLOCK_ENTITIES);
        Instancer<TransformedInstance> headModel = getHeadModel();

        ms = new PoseStack();
        var msr = TransformStack.of(ms);
        msr.translate(getVisualPosition());

        float progress = Mth.lerp(AnimationTickHolder.getPartialTicks(), blockEntity.prevDialState, blockEntity.dialState);

        for (Direction facing : Iterate.directions) {
            if (!gaugeBlock.shouldRenderHeadOnFace(level, pos, blockState, facing))
                continue;

            DialFace face = makeFace(facing, dialModel, headModel);

            faces.add(face);

            face.setupTransform(msr, progress);
        }
    }

    private DialFace makeFace(Direction face, Instancer<TransformedInstance> dialModel, Instancer<TransformedInstance> headModel) {
        return new DialFace(face, dialModel.createInstance(), headModel.createInstance());
    }

    @Override
    public void beginFrame(VisualFrameContext ctx) {
        if (Mth.equal(blockEntity.prevDialState, blockEntity.dialState))
            return;

        float progress = Mth.lerp(ctx.partialTick(), blockEntity.prevDialState, blockEntity.dialState);

        var msr = TransformStack.of(ms);

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
    protected void _delete() {
        super._delete();

        faces.forEach(DialFace::delete);
    }

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
        for (DialFace face : faces) {
            face.forEach(consumer);
        }
    }

	protected abstract Instancer<TransformedInstance> getHeadModel();

    protected class DialFace extends Couple<TransformedInstance> {

        Direction face;

        public DialFace(Direction face, TransformedInstance first, TransformedInstance second) {
            super(first, second);
            this.face = face;
        }

        private void setupTransform(TransformStack<?> msr, float progress) {
            float dialPivot = 5.75f / 16;

            msr.pushPose();
            rotateToFace(msr);

            getSecond().setTransform(ms);

            msr.translate(0, dialPivot, dialPivot)
               .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
               .translate(0, -dialPivot, -dialPivot);

            getFirst().setTransform(ms);

            msr.popPose();
        }

        private void updateTransform(TransformStack<?> msr, float progress) {
            float dialPivot = 5.75f / 16;

            msr.pushPose();

            rotateToFace(msr)
                    .translate(0, dialPivot, dialPivot)
                    .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
                    .translate(0, -dialPivot, -dialPivot);

            getFirst().setTransform(ms);

            msr.popPose();
        }

        protected TransformStack<?> rotateToFace(TransformStack<?> msr) {
            return msr.center()
                      .rotate((float) ((-face.toYRot() - 90) / 180 * Math.PI), Direction.UP)
                      .uncenter();
        }

        private void delete() {
            getFirst().delete();
            getSecond().delete();
        }
    }

    public static class Speed extends GaugeInstance {
        public Speed(VisualizationContext materialManager, GaugeBlockEntity blockEntity) {
            super(materialManager, blockEntity);
        }

        @Override
        protected Instancer<TransformedInstance> getHeadModel() {
            return instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GAUGE_HEAD_SPEED), RenderStage.AFTER_BLOCK_ENTITIES);
        }
    }

    public static class Stress extends GaugeInstance {
        public Stress(VisualizationContext materialManager, GaugeBlockEntity blockEntity) {
            super(materialManager, blockEntity);
        }

        @Override
        protected Instancer<TransformedInstance> getHeadModel() {
            return instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GAUGE_HEAD_STRESS), RenderStage.AFTER_BLOCK_ENTITIES);
        }
    }
}
