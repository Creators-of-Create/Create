package com.simibubi.create.content.kinetics.mixer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.flwdata.RotatingInstance;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;

public class MixerInstance extends EncasedCogInstance implements DynamicVisual {

	private final RotatingInstance mixerHead;
	private final OrientedInstance mixerPole;
	private final MechanicalMixerBlockEntity mixer;

	public MixerInstance(VisualizationContext materialManager, MechanicalMixerBlockEntity blockEntity) {
		super(materialManager, blockEntity, false);
		this.mixer = blockEntity;

		mixerHead = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.MECHANICAL_MIXER_HEAD), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();

		mixerHead.setRotationAxis(Direction.Axis.Y);

		mixerPole = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_MIXER_POLE), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();


		float renderedHeadOffset = getRenderedHeadOffset();

		transformPole(renderedHeadOffset);
		transformHead(renderedHeadOffset);
	}

	@Override
	protected Instancer<RotatingInstance> getCogModel() {
		return materialManager.defaultSolid()
			.material(AllInstanceTypes.ROTATING)
			.getModel(AllPartialModels.SHAFTLESS_COGWHEEL, blockEntity.getBlockState());
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		float renderedHeadOffset = getRenderedHeadOffset();

		transformPole(renderedHeadOffset);
		transformHead(renderedHeadOffset);
	}

	private void transformHead(float renderedHeadOffset) {
		float speed = mixer.getRenderedHeadRotationSpeed(AnimationTickHolder.getPartialTicks());

		mixerHead.setPosition(getVisualPosition())
				.nudge(0, -renderedHeadOffset, 0)
				.setRotationalSpeed(speed * 2);
	}

	private void transformPole(float renderedHeadOffset) {
		mixerPole.setPosition(getVisualPosition())
				.nudgePosition(0, -renderedHeadOffset, 0);
	}

	private float getRenderedHeadOffset() {
		return mixer.getRenderedHeadOffset(AnimationTickHolder.getPartialTicks());
	}

	@Override
	public void updateLight() {
		super.updateLight();

		relight(pos.below(), mixerHead);
		relight(pos, mixerPole);
	}

	@Override
	protected void _delete() {
		super._delete();
		mixerHead.delete();
		mixerPole.delete();
	}
}
