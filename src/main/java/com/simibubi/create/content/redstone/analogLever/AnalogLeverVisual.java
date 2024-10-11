package com.simibubi.create.content.redstone.analogLever;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Color;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.Rotate;
import dev.engine_room.flywheel.lib.transform.Translate;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class AnalogLeverVisual extends AbstractBlockEntityVisual<AnalogLeverBlockEntity> implements SimpleDynamicVisual {

	protected final TransformedInstance handle;
	protected final TransformedInstance indicator;

	final float rX;
	final float rY;

	public AnalogLeverVisual(VisualizationContext context, AnalogLeverBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		handle = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_HANDLE))
			.createInstance();
		indicator = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_INDICATOR))
			.createInstance();

		AttachFace face = blockState.getValue(AnalogLeverBlock.FACE);
		rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
		rY = AngleHelper.horizontalAngle(blockState.getValue(AnalogLeverBlock.FACING));

		transform(indicator.setIdentityTransform());

		animateLever(partialTick);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		if (!blockEntity.clientState.settled())
			animateLever(ctx.partialTick());
	}

	protected void animateLever(float pt) {
		float state = blockEntity.clientState.getValue(pt);

		indicator.colorRgb(Color.mixColors(0x2C0300, 0xCD0000, state / 15f));
		indicator.setChanged();

		float angle = (float) ((state / 15) * 90 / 180 * Math.PI);

		transform(handle.setIdentityTransform()).translate(1 / 2f, 1 / 16f, 1 / 2f)
			.rotate(angle, Direction.EAST)
			.translate(-1 / 2f, -1 / 16f, -1 / 2f)
			.setChanged();
	}

	@Override
	protected void _delete() {
		handle.delete();
		indicator.delete();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(handle, indicator);
	}

	private <T extends Translate<T> & Rotate<T>> T transform(T msr) {
		return msr.translate(getVisualPosition())
			.center()
			.rotate((float) (rY / 180 * Math.PI), Direction.UP)
			.rotate((float) (rX / 180 * Math.PI), Direction.EAST)
			.uncenter();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(handle);
		consumer.accept(indicator);
	}
}
