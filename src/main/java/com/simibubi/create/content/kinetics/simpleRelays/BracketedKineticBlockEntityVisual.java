package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class BracketedKineticBlockEntityVisual extends SingleRotatingVisual<BracketedKineticBlockEntity> {

	protected RotatingInstance additionalShaft;

	public BracketedKineticBlockEntityVisual(VisualizationContext context, BracketedKineticBlockEntity blockEntity) {
		super(context, blockEntity);
	}

	@Override
	public void init(float pt) {
        if (ICogWheel.isLargeCog(blockEntity.getBlockState())) {
			// Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
            // mesh properly

            float speed = blockEntity.getSpeed();
            Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
            BlockPos pos = blockEntity.getBlockPos();
            float offset = BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos);
            var model = Models.partial(AllPartialModels.COGWHEEL_SHAFT, axis, BracketedKineticBlockEntityVisual::rotateToAxis);
            Instancer<RotatingInstance> half = instancerProvider.instancer(AllInstanceTypes.ROTATING, model, RenderStage.AFTER_BLOCK_ENTITIES);

            additionalShaft = setup(half.createInstance(), speed);
            additionalShaft.setRotationOffset(offset)
                .setChanged();
        }
		super.init(pt);
	}

	@Override
	protected Model model() {
		if (!ICogWheel.isLargeCog(blockEntity.getBlockState()))
			return super.model();

		Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
		return Models.partial(AllPartialModels.SHAFTLESS_LARGE_COGWHEEL, axis, BracketedKineticBlockEntityVisual::rotateToAxis);
	}

	private static void rotateToAxis(Direction.Axis axis, PoseStack ms) {
		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		TransformStack.of(ms)
				.center()
				.rotateToFace(facing)
				.rotate(Axis.XN.rotationDegrees(-90))
				.uncenter();
	}

	@Override
	public void update(float pt) {
		super.update(pt);
		if (additionalShaft != null) {
			updateRotation(additionalShaft);
			additionalShaft.setRotationOffset(BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos))
					.setChanged();
		}
	}

	@Override
	public void updateLight() {
		super.updateLight();
		if (additionalShaft != null)
			relight(pos, additionalShaft);
	}

	@Override
    protected void _delete() {
		super._delete();
		if (additionalShaft != null)
			additionalShaft.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		if (additionalShaft != null)
			consumer.accept(additionalShaft);
	}
}
