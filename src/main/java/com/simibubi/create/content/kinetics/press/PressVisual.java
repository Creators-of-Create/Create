package com.simibubi.create.content.kinetics.press;

import java.util.function.Consumer;

import org.joml.Quaternionf;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.utility.AngleHelper;

public class PressVisual extends ShaftVisual<MechanicalPressBlockEntity> implements SimpleDynamicVisual {

	private final OrientedInstance pressHead;

	public PressVisual(VisualizationContext context, MechanicalPressBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		pressHead = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_PRESS_HEAD))
				.createInstance();

		Quaternionf q = Axis.YP
			.rotationDegrees(AngleHelper.horizontalAngle(blockState.getValue(MechanicalPressBlock.HORIZONTAL_FACING)));

		pressHead.setRotation(q);

		transformModels(partialTick);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		transformModels(ctx.partialTick());
	}

	private void transformModels(float pt) {
		float renderedHeadOffset = getRenderedHeadOffset(pt);

		pressHead.setPosition(getVisualPosition())
			.nudgePosition(0, -renderedHeadOffset, 0)
			.setChanged();
	}

	private float getRenderedHeadOffset(float pt) {
		PressingBehaviour pressingBehaviour = blockEntity.getPressingBehaviour();
		return pressingBehaviour.getRenderedHeadOffset(pt)
			* pressingBehaviour.mode.headOffset;
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);

		relight(pos, pressHead);
	}

	@Override
    protected void _delete() {
		super._delete();
		pressHead.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(pressHead);
	}
}
