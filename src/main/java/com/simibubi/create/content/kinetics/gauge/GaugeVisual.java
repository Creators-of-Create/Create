package com.simibubi.create.content.kinetics.gauge;

import java.util.ArrayList;
import java.util.function.Consumer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public abstract class GaugeVisual extends ShaftVisual<GaugeBlockEntity> implements SimpleDynamicVisual {

    protected final ArrayList<DialFace> faces = new ArrayList<>(2);

    protected final PoseStack ms = new PoseStack();

    protected GaugeVisual(VisualizationContext context, GaugeBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);
		init();
    }

	public void init() {
		GaugeBlock gaugeBlock = (GaugeBlock) blockState.getBlock();

		Instancer<TransformedInstance> dialModel = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GAUGE_DIAL));
		Instancer<TransformedInstance> headModel = getHeadModel();

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
    public void beginFrame(DynamicVisual.Context ctx) {
        if (Mth.equal(blockEntity.prevDialState, blockEntity.dialState))
            return;

        float progress = Mth.lerp(ctx.partialTick(), blockEntity.prevDialState, blockEntity.dialState);

        var msr = TransformStack.of(ms);

        for (DialFace faceEntry : faces) {
            faceEntry.updateTransform(msr, progress);
        }
    }

    @Override
    public void updateLight(float partialTick) {
        super.updateLight(partialTick);

        relight(pos, faces.stream()
				.flatMap(Couple::stream).toArray(FlatLit[]::new));
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

            getSecond().setTransform(ms).setChanged();

            msr.translate(0, dialPivot, dialPivot)
               .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
               .translate(0, -dialPivot, -dialPivot);

            getFirst().setTransform(ms).setChanged();

            msr.popPose();
        }

        private void updateTransform(TransformStack<?> msr, float progress) {
            float dialPivot = 5.75f / 16;

            msr.pushPose();

            rotateToFace(msr)
                    .translate(0, dialPivot, dialPivot)
                    .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
                    .translate(0, -dialPivot, -dialPivot);

            getFirst().setTransform(ms).setChanged();

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

    public static class Speed extends GaugeVisual {
        public Speed(VisualizationContext context, GaugeBlockEntity blockEntity, float partialTick) {
            super(context, blockEntity, partialTick);
        }

        @Override
        protected Instancer<TransformedInstance> getHeadModel() {
            return instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GAUGE_HEAD_SPEED));
        }
    }

    public static class Stress extends GaugeVisual {
        public Stress(VisualizationContext context, GaugeBlockEntity blockEntity, float partialTick) {
            super(context, blockEntity, partialTick);
        }

        @Override
        protected Instancer<TransformedInstance> getHeadModel() {
            return instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GAUGE_HEAD_STRESS));
        }
    }
}
