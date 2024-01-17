package com.simibubi.create.content.contraptions.chassis;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class StickerVisual extends AbstractBlockEntityVisual<StickerBlockEntity> implements DynamicVisual {

	float lastOffset = Float.NaN;
	final Direction facing;
	final boolean fakeWorld;
	final int offset;

	private final TransformedInstance head;

	public StickerVisual(VisualizationContext context, StickerBlockEntity blockEntity) {
		super(context, blockEntity);

		head = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.STICKER_HEAD), RenderStage.AFTER_BLOCK_ENTITIES).createInstance();

		fakeWorld = blockEntity.getLevel() != Minecraft.getInstance().level;
		facing = blockState.getValue(StickerBlock.FACING);
		offset = blockState.getValue(StickerBlock.EXTENDED) ? 1 : 0;

		animateHead(offset);
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
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
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing) + 90)
				.uncenter()
				.translate(0, (offset * offset) * 4 / 16f, 0);
	}

	@Override
	public void updateLight() {
		relight(pos, head);
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
