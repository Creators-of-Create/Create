package com.simibubi.create.content.redstone.analogLever;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.transform.Rotate;
import com.jozufozu.flywheel.lib.transform.Translate;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;

public class AnalogLeverInstance extends AbstractBlockEntityVisual<AnalogLeverBlockEntity> implements DynamicVisual {

	protected final TransformedInstance handle;
	protected final TransformedInstance indicator;

	final float rX;
	final float rY;

	public AnalogLeverInstance(VisualizationContext materialManager, AnalogLeverBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		handle = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_HANDLE), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();
		indicator = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.ANALOG_LEVER_INDICATOR), RenderStage.AFTER_BLOCK_ENTITIES)
			.createInstance();

		AttachFace face = blockState.getValue(AnalogLeverBlock.FACE);
		rX = face == AttachFace.FLOOR ? 0 : face == AttachFace.WALL ? 90 : 180;
		rY = AngleHelper.horizontalAngle(blockState.getValue(AnalogLeverBlock.FACING));

		transform(indicator.loadIdentity());
		animateLever();
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		if (!blockEntity.clientState.settled())
			animateLever();
	}

	protected void animateLever() {
		float state = blockEntity.clientState.getValue(AnimationTickHolder.getPartialTicks());

		indicator.setColor(Color.mixColors(0x2C0300, 0xCD0000, state / 15f));

		float angle = (float) ((state / 15) * 90 / 180 * Math.PI);

		transform(handle.loadIdentity()).translate(1 / 2f, 1 / 16f, 1 / 2f)
			.rotate(angle, Direction.EAST)
			.translate(-1 / 2f, -1 / 16f, -1 / 2f);
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
