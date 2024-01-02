package com.simibubi.create.content.contraptions.chassis;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class StickerInstance extends AbstractBlockEntityVisual<StickerBlockEntity> implements DynamicVisual {

	float lastOffset = Float.NaN;
	final Direction facing;
	final boolean fakeWorld;
	final int offset;

	private final TransformedInstance head;

	public StickerInstance(VisualizationContext materialManager, StickerBlockEntity blockEntity) {
		super(materialManager, blockEntity);

		head = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.STICKER_HEAD), RenderStage.AFTER_BLOCK_ENTITIES).createInstance();

		fakeWorld = blockEntity.getLevel() != Minecraft.getInstance().level;
		facing = blockState.getValue(StickerBlock.FACING);
		offset = blockState.getValue(StickerBlock.EXTENDED) ? 1 : 0;

		animateHead(offset);
	}

	@Override
	public void beginFrame() {
		float offset = blockEntity.piston.getValue(AnimationTickHolder.getPartialTicks());

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
}
