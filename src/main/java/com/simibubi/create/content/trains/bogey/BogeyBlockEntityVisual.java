package com.simibubi.create.content.trains.bogey;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.trains.bogey.BogeySizes.BogeySize;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class BogeyBlockEntityVisual extends AbstractBlockEntityVisual<AbstractBogeyBlockEntity> implements SimpleDynamicVisual {
	private final PoseStack poseStack = new PoseStack();

	@Nullable
	private final BogeySize bogeySize;
	private BogeyStyle lastStyle;
	@Nullable
	private BogeyVisual bogey;

	public BogeyBlockEntityVisual(VisualizationContext ctx, AbstractBogeyBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		lastStyle = blockEntity.getStyle();

		if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> block)) {
			bogeySize = null;
			return;
		}

		bogeySize = block.getSize();

		BlockPos visualPos = getVisualPosition();
		poseStack.translate(visualPos.getX(), visualPos.getY(), visualPos.getZ());
		poseStack.translate(.5f, .5f, .5f);
		if (blockState.getValue(AbstractBogeyBlock.AXIS) == Direction.Axis.X)
			poseStack.mulPose(Axis.YP.rotationDegrees(90));
		poseStack.translate(0, -1.5 - 1 / 128f, 0);

		bogey = lastStyle.createVisual(bogeySize, visualizationContext, partialTick, false);

		updateBogey(partialTick);
	}

	@Override
	public void beginFrame(Context context) {
		if (bogeySize == null) {
			return;
		}

		BogeyStyle style = blockEntity.getStyle();
		if (style != lastStyle) {
			if (bogey != null) {
				bogey.delete();
				bogey = null;
			}
			lastStyle = style;
			bogey = lastStyle.createVisual(bogeySize, visualizationContext, context.partialTick(), false);
			updateLight(context.partialTick());
		}
		
		updateBogey(context.partialTick());
	}

	private void updateBogey(float partialTick) {
		if (bogey == null) {
			return;
		}

		CompoundTag bogeyData = blockEntity.getBogeyData();
		float angle = blockEntity.getVirtualAngle(partialTick);
		bogey.update(bogeyData, angle, poseStack);
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		if (bogey != null) {
			bogey.collectCrumblingInstances(consumer);
		}
	}

	@Override
	public void updateLight(float partialTick) {
		if (bogey != null) {
			bogey.updateLight(computePackedLight());
		}
	}

	@Override
	protected void _delete() {
		if (bogey != null) {
			bogey.delete();
		}
	}
}
