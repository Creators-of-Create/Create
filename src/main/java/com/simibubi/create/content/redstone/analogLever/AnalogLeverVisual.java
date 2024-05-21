package com.simibubi.create.content.redstone.analogLever;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.transform.Rotate;
import com.jozufozu.flywheel.lib.transform.Translate;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class AnalogLeverVisual extends AbstractBlockEntityVisual<AnalogLeverBlockEntity> implements SimpleDynamicVisual {

	protected final TransformedInstance handle;
	protected final TransformedInstance indicator;

	final float rX;
	final float rY;

	public AnalogLeverVisual(VisualizationContext context, AnalogLeverBlockEntity blockEntity) {
		super(context, blockEntity);

		handle = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_HANDLE))
			.createInstance();
		indicator = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_INDICATOR))
			.createInstance();

		AttachFace face = blockState.getValue(AnalogLeverBlock.FACE);
		rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
		rY = AngleHelper.horizontalAngle(blockState.getValue(AnalogLeverBlock.FACING));
	}

	@Override
	public void init(float partialTick) {
		super.init(partialTick);

		transform(indicator.loadIdentity());

		animateLever(partialTick);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		if (!blockEntity.clientState.settled())
			animateLever(ctx.partialTick());
	}

	protected void animateLever(float pt) {
		float state = blockEntity.clientState.getValue(pt);

		indicator.setColor(Color.mixColors(0x2C0300, 0xCD0000, state / 15f));
		indicator.setChanged();

		float angle = (float) ((state / 15) * 90 / 180 * Math.PI);

		transform(handle.loadIdentity()).translate(1 / 2f, 1 / 16f, 1 / 2f)
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
	public void updateLight() {
		relight(pos, handle, indicator);
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
