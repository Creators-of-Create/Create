package com.simibubi.create.content.equipment.toolbox;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.api.data.Iterate;
import net.minecraft.core.Direction;

public class ToolBoxVisual extends AbstractBlockEntityVisual<ToolboxBlockEntity> implements SimpleDynamicVisual {

	private final Direction facing;
	private final TransformedInstance lid;
	private final TransformedInstance[] drawers;

	private float lastLidAngle = Float.NaN;
	private float lastDrawerOffset = Float.NaN;

	public ToolBoxVisual(VisualizationContext context, ToolboxBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		facing = blockState.getValue(ToolboxBlock.FACING)
				.getOpposite();

		Instancer<TransformedInstance> drawerModel = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TOOLBOX_DRAWER));

		drawers = new TransformedInstance[]{drawerModel.createInstance(), drawerModel.createInstance()};
		lid = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TOOLBOX_LIDS.get(blockEntity.getColor())))
				.createInstance();

		animate(partialTick);
	}

	@Override
	protected void _delete() {
		lid.delete();

		for (var drawer : drawers) {
			drawer.delete();
		}
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		animate(ctx.partialTick());
	}

	private void animate(float partialTicks) {
		float lidAngle = blockEntity.lid.getValue(partialTicks);
		float drawerOffset = blockEntity.drawers.getValue(partialTicks);

		if (lidAngle != lastLidAngle) {
			lid.setIdentityTransform()
					.translate(getVisualPosition())
					.center()
					.rotateYDegrees(-facing.toYRot())
					.uncenter()
					.translate(0, 6 / 16f, 12 / 16f)
					.rotateXDegrees(135 * lidAngle)
					.translateBack(0, 6 / 16f, 12 / 16f)
					.setChanged();
		}

		if (drawerOffset != lastDrawerOffset) {
			for (int offset : Iterate.zeroAndOne) {
				drawers[offset].setIdentityTransform()
						.translate(getVisualPosition())
						.center()
						.rotateYDegrees(-facing.toYRot())
						.uncenter()
						.translate(0, offset * 1 / 8f, -drawerOffset * .175f * (2 - offset))
						.setChanged();
			}
		}

		lastLidAngle = lidAngle;
		lastDrawerOffset = drawerOffset;
	}

	@Override
	public void updateLight(float partialTick) {
		relight(drawers);
		relight(lid);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(lid);
		for (var drawer : drawers) {
			consumer.accept(drawer);
		}
	}
}
