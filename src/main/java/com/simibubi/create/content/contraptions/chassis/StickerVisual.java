package com.simibubi.create.content.contraptions.chassis;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AngleHelper;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class StickerVisual extends AbstractBlockEntityVisual<StickerBlockEntity> implements SimpleDynamicVisual {

	float lastOffset = Float.NaN;
	final Direction facing;
	final boolean fakeWorld;
	final int offset;

	private final TransformedInstance head;

	public StickerVisual(VisualizationContext context, StickerBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		head = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.STICKER_HEAD)).createInstance();

		fakeWorld = blockEntity.getLevel() != Minecraft.getInstance().level;
		facing = blockState.getValue(StickerBlock.FACING);
		offset = blockState.getValue(StickerBlock.EXTENDED) ? 1 : 0;

		animateHead(offset);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		float offset = blockEntity.piston.getValue(ctx.partialTick());

		if (fakeWorld)
			offset = this.offset;

		if (Mth.equal(offset, lastOffset))
			return;

		animateHead(offset);

		lastOffset = offset;
	}

	private void animateHead(float offset) {
		head.loadIdentity()
				.translate(getVisualPosition())
				.nudge(blockEntity.hashCode())
				.center()
				.rotateYDegrees(AngleHelper.horizontalAngle(facing))
				.rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
				.uncenter()
				.translate(0, (offset * offset) * 4 / 16f, 0)
				.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(head);
	}

	@Override
	protected void _delete() {
		head.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(head);
	}
}
