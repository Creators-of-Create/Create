package com.simibubi.create.content.trains.observer;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TrackObserverVisual extends AbstractBlockEntityVisual<TrackObserverBlockEntity> implements SimpleTickableVisual {
	private final TransformedInstance overlay;
	private BlockPos oldTargetPos;

	public TrackObserverVisual(VisualizationContext ctx, TrackObserverBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		overlay = ctx.instancerProvider()
			.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.TRACK_OBSERVER_OVERLAY))
			.createInstance();

		setupVisual();
	}

	@Override
	public void tick(Context context) {
		setupVisual();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(overlay);
	}

	@Override
	protected void _delete() {
		overlay.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		consumer.accept(overlay);
	}

	private void setupVisual() {
		TrackTargetingBehaviour<TrackObserver> target = blockEntity.edgePoint;
		BlockPos targetPosition = target.getGlobalPosition();
		Level level = blockEntity.getLevel();
		BlockState trackState = level.getBlockState(targetPosition);
		Block block = trackState.getBlock();

		if (!(block instanceof ITrackBlock trackBlock)) {
			overlay.setZeroTransform()
				.setChanged();
			return;
		}

		if (!targetPosition.equals(oldTargetPos)) {
			oldTargetPos = targetPosition;

			overlay.setIdentityTransform()
				.translate(targetPosition.subtract(renderOrigin()));

			RenderedTrackOverlayType type = RenderedTrackOverlayType.OBSERVER;
			trackBlock.prepareTrackOverlay(overlay, level, targetPosition, trackState, target.getTargetBezier(),
				target.getTargetDirection(), type);

			overlay.setChanged();
		}
	}
}
