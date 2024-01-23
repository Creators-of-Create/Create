package com.simibubi.create.content.kinetics.press;

import java.util.function.Consumer;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class PressVisual extends ShaftVisual<MechanicalPressBlockEntity> implements DynamicVisual {

	private final OrientedInstance pressHead;

	public PressVisual(VisualizationContext context, MechanicalPressBlockEntity blockEntity) {
		super(context, blockEntity);

		pressHead = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_PRESS_HEAD), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();

		Quaternionf q = Axis.YP
			.rotationDegrees(AngleHelper.horizontalAngle(blockState.getValue(MechanicalPressBlock.HORIZONTAL_FACING)));

		pressHead.setRotation(q);

		transformModels();
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		transformModels();
	}

	private void transformModels() {
		float renderedHeadOffset = getRenderedHeadOffset(blockEntity);

		pressHead.setPosition(getVisualPosition())
			.nudgePosition(0, -renderedHeadOffset, 0)
			.setChanged();
	}

	private float getRenderedHeadOffset(MechanicalPressBlockEntity press) {
		PressingBehaviour pressingBehaviour = press.getPressingBehaviour();
		return pressingBehaviour.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks())
			* pressingBehaviour.mode.headOffset;
	}

	@Override
	public void updateLight() {
		super.updateLight();

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
