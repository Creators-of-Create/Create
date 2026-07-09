package com.simibubi.create.content.logistics.packager;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class PackagerVisual<T extends PackagerBlockEntity> extends AbstractBlockEntityVisual<T> implements SimpleDynamicVisual {
	public final TransformedInstance hatch;
	public final TransformedInstance tray;

	public float lastTrayOffset = Float.NaN;
	public PartialModel lastHatchPartial;


	public PackagerVisual(VisualizationContext ctx, T blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		lastHatchPartial = PackagerRenderer.getHatchModel(blockEntity);
		hatch = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(lastHatchPartial))
			.createInstance();

		tray = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(PackagerRenderer.getTrayModel(blockState)))
			.createInstance();

		Direction facing = blockState.getValue(PackagerBlock.FACING)
			.getOpposite();

		var lowerCorner = Vec3.atLowerCornerOf(facing.getUnitVec3i());
		hatch.setIdentityTransform()
			.translate(getVisualPosition())
			.translate(lowerCorner
				.scale(.49999f))
			.rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXCenteredDegrees(AngleHelper.verticalAngle(facing))
			.setChanged();

		// TODO: I think we need proper ItemVisuals to handle rendering the boxes in here

		animate(partialTick);
	}

	@Override
	public void beginFrame(Context ctx) {
		animate(ctx.partialTick());
	}

	public void animate(float partialTick) {
		var hatchPartial = PackagerRenderer.getHatchModel(blockEntity);

		if (hatchPartial != this.lastHatchPartial) {
			instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(hatchPartial))
				.stealInstance(hatch);

			this.lastHatchPartial = hatchPartial;
		}

		float trayOffset = blockEntity.getTrayOffset(partialTick);

		if (trayOffset != lastTrayOffset) {
			Direction facing = blockState.getValue(PackagerBlock.FACING)
				.getOpposite();

			var lowerCorner = Vec3.atLowerCornerOf(facing.getUnitVec3i());

			tray.setIdentityTransform()
				.translate(getVisualPosition())
				.translate(lowerCorner.scale(trayOffset))
				.rotateYCenteredDegrees(facing.toYRot())
				.setChanged();

			lastTrayOffset = trayOffset;
		}
	}

	@Override
	public void updateLight(float partialTick) {
		relight(hatch, tray);
	}

	@Override
	protected void _delete() {
		hatch.delete();
		tray.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {

	}
}
