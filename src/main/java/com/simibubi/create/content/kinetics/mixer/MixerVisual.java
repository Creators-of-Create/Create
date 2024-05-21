package com.simibubi.create.content.kinetics.mixer;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import net.minecraft.core.Direction;

public class MixerVisual extends EncasedCogVisual implements SimpleDynamicVisual {

	private final RotatingInstance mixerHead;
	private final OrientedInstance mixerPole;
	private final MechanicalMixerBlockEntity mixer;

	public MixerVisual(VisualizationContext context, MechanicalMixerBlockEntity blockEntity) {
		super(context, blockEntity, false);
		this.mixer = blockEntity;

		mixerHead = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.MECHANICAL_MIXER_HEAD))
			.createInstance();

		mixerHead.setRotationAxis(Direction.Axis.Y);

		mixerPole = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_MIXER_POLE))
				.createInstance();
	}

	@Override
	public void init(float pt) {
		super.init(pt);

		animate(pt);
	}

	@Override
	protected Model getCogModel() {
		return Models.partial(AllPartialModels.SHAFTLESS_COGWHEEL);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		animate(ctx.partialTick());
	}

	private void animate(float pt) {
		float renderedHeadOffset = mixer.getRenderedHeadOffset(pt);

		transformPole(renderedHeadOffset);
		transformHead(renderedHeadOffset, pt);
	}

	private void transformHead(float renderedHeadOffset, float pt) {
		float speed = mixer.getRenderedHeadRotationSpeed(pt);

		mixerHead.setPosition(getVisualPosition())
				.nudge(0, -renderedHeadOffset, 0)
				.setRotationalSpeed(speed * 2)
				.setChanged();
	}

	private void transformPole(float renderedHeadOffset) {
		mixerPole.setPosition(getVisualPosition())
				.nudgePosition(0, -renderedHeadOffset, 0)
				.setChanged();
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

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(mixerHead);
		consumer.accept(mixerPole);
	}
}
