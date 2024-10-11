package com.simibubi.create.content.kinetics.mixer;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.OrientedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;

public class MixerVisual extends EncasedCogVisual implements SimpleDynamicVisual {

	private final RotatingInstance mixerHead;
	private final OrientedInstance mixerPole;
	private final MechanicalMixerBlockEntity mixer;

	public MixerVisual(VisualizationContext context, MechanicalMixerBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, false, partialTick);
		this.mixer = blockEntity;

		mixerHead = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.MECHANICAL_MIXER_HEAD))
			.createInstance();

		mixerHead.setRotationAxis(Direction.Axis.Y);

		mixerPole = instancerProvider.instancer(InstanceTypes.ORIENTED, Models.partial(AllPartialModels.MECHANICAL_MIXER_POLE))
				.createInstance();

		animate(partialTick);
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
		mixerPole.position(getVisualPosition())
				.translatePosition(0, -renderedHeadOffset, 0)
				.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);

		relight(pos.below(), mixerHead);
		relight(mixerPole);
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
