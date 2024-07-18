package com.simibubi.create.content.kinetics.simpleRelays;

import java.util.function.Consumer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.TransformStack;
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

	public BracketedKineticBlockEntityVisual(VisualizationContext context, BracketedKineticBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		init(partialTick);
	}

	public void init(float partialTick) {
        if (ICogWheel.isLargeCog(blockEntity.getBlockState())) {
			// Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
            // mesh properly

            float speed = blockEntity.getSpeed();
            Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockEntity);
            BlockPos pos = blockEntity.getBlockPos();
            float offset = BracketedKineticBlockEntityRenderer.getShaftAngleOffset(axis, pos);
            var model = Models.partial(AllPartialModels.COGWHEEL_SHAFT, axis, BracketedKineticBlockEntityVisual::rotateToAxis);
            Instancer<RotatingInstance> half = instancerProvider.instancer(AllInstanceTypes.ROTATING, model);

            additionalShaft = setup(half.createInstance(), speed);
            additionalShaft.setRotationOffset(offset)
                .setChanged();
        }
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
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);
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
