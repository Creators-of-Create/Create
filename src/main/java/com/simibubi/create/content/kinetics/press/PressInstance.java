package com.simibubi.create.content.kinetics.press;

import org.joml.Quaternionf;

import com.jozufozu.flywheel.api.instance.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.oriented.OrientedInstance;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class PressInstance extends ShaftInstance<MechanicalPressBlockEntity> implements DynamicVisual {

	private final OrientedInstance pressHead;

	public PressInstance(VisualizationContext materialManager, MechanicalPressBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		pressHead = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_PRESS_HEAD), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();

		Quaternionf q = Axis.YP
			.rotationDegrees(AngleHelper.horizontalAngle(blockState.getValue(MechanicalPressBlock.HORIZONTAL_FACING)));

		pressHead.setRotation(q);

		transformModels();
	}

	@Override
	public void beginFrame() {
		transformModels();
	}

	private void transformModels() {
		float renderedHeadOffset = getRenderedHeadOffset(blockEntity);

		pressHead.setPosition(getVisualPosition())
			.nudge(0, -renderedHeadOffset, 0);
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
}
