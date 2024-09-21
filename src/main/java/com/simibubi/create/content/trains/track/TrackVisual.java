package com.simibubi.create.content.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.trains.track.BezierConnection.GirderAngles;
import com.simibubi.create.content.trains.track.BezierConnection.SegmentAngles;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class TrackVisual extends AbstractBlockEntityVisual<TrackBlockEntity> {

	private final List<BezierTrackVisual> visuals = new ArrayList<>();

	public TrackVisual(VisualizationContext context, TrackBlockEntity track, float partialTick) {
		super(context, track, partialTick);

		collectConnections();
	}

	@Override
	public void update(float pt) {
		if (blockEntity.connections.isEmpty())
			return;

		_delete();

		collectConnections();

		lightSections.sections(collectLightSections());
	}

	private void collectConnections() {
		blockEntity.connections.values()
			.stream()
			.map(this::createInstance)
			.filter(Objects::nonNull)
			.forEach(visuals::add);
	}

	@Override
	public void updateLight(float partialTick) {
		visuals.forEach(BezierTrackVisual::updateLight);
	}

	@Nullable
	private BezierTrackVisual createInstance(BezierConnection bc) {
		if (!bc.isPrimary())
			return null;
		return new BezierTrackVisual(bc);
	}

	@Override
	public void _delete() {
		visuals.forEach(BezierTrackVisual::delete);
		visuals.clear();
	}

	public LongSet collectLightSections() {
		LongSet longSet = new LongArraySet();

		if (blockEntity.connections.isEmpty()) {
			return LongSet.of();
		}
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (BlockPos pos : blockEntity.connections.keySet()) {
			minX = Math.min(minX, pos.getX());
			minY = Math.min(minY, pos.getY());
			minZ = Math.min(minZ, pos.getZ());
			maxX = Math.max(maxX, pos.getX());
			maxY = Math.max(maxY, pos.getY());
			maxZ = Math.max(maxZ, pos.getZ());
		}
		SectionPos.betweenClosedStream(SectionPos.blockToSectionCoord(minX), SectionPos.blockToSectionCoord(minY), SectionPos.blockToSectionCoord(minZ), SectionPos.blockToSectionCoord(maxX), SectionPos.blockToSectionCoord(maxY), SectionPos.blockToSectionCoord(maxZ))
			.mapToLong(SectionPos::asLong)
			.forEach(longSet::add);

		return longSet;
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
        for (BezierTrackVisual instance : visuals) {
            instance.collectCrumblingInstances(consumer);
        }
    }

	private class BezierTrackVisual {

		private final TransformedInstance[] ties;
		private final TransformedInstance[] left;
		private final TransformedInstance[] right;
		private final BlockPos[] tiesLightPos;
		private final BlockPos[] leftLightPos;
		private final BlockPos[] rightLightPos;

		private @Nullable GirderVisual girder;

		private BezierTrackVisual(BezierConnection bc) {
			BlockPos tePosition = bc.bePositions.getFirst();
			girder = bc.hasGirder ? new GirderVisual(bc) : null;

			PoseStack pose = new PoseStack();
			TransformStack.of(pose)
				.translate(getVisualPosition());

			int segCount = bc.getSegmentCount();
			ties = new TransformedInstance[segCount];
			left = new TransformedInstance[segCount];
			right = new TransformedInstance[segCount];
			tiesLightPos = new BlockPos[segCount];
			leftLightPos = new BlockPos[segCount];
			rightLightPos = new BlockPos[segCount];

			TrackMaterial.TrackModelHolder modelHolder = bc.getMaterial().getModelHolder();

			instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(modelHolder.tie()))
				.createInstances(ties);
			instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(modelHolder.leftSegment()))
				.createInstances(left);
			instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(modelHolder.rightSegment()))
				.createInstances(right);

			SegmentAngles[] segments = bc.getBakedSegments();
			for (int i = 1; i < segments.length; i++) {
				SegmentAngles segment = segments[i];
				var modelIndex = i - 1;

				ties[modelIndex].setTransform(pose)
					.mul(segment.tieTransform)
					.setChanged();
				tiesLightPos[modelIndex] = segment.lightPosition.offset(tePosition);

				for (boolean first : Iterate.trueAndFalse) {
					Pose transform = segment.railTransforms.get(first);
					(first ? this.left : this.right)[modelIndex].setTransform(pose)
						.mul(transform)
						.setChanged();
					(first ? leftLightPos : rightLightPos)[modelIndex] = segment.lightPosition.offset(tePosition);
				}
			}

			updateLight();
		}

		void delete() {
			for (var d : ties)
				d.delete();
			for (var d : left)
				d.delete();
			for (var d : right)
				d.delete();
			if (girder != null)
				girder.delete();
		}

		void updateLight() {
			for (int i = 0; i < ties.length; i++)
				TrackVisual.updateLight(ties[i], level, tiesLightPos[i]);
			for (int i = 0; i < left.length; i++)
				TrackVisual.updateLight(left[i], level, leftLightPos[i]);
			for (int i = 0; i < right.length; i++)
				TrackVisual.updateLight(right[i], level, rightLightPos[i]);
			if (girder != null)
				girder.updateLight();
		}

		public void collectCrumblingInstances(Consumer<Instance> consumer) {
			for (var d : ties)
				consumer.accept(d);
			for (var d : left)
				consumer.accept(d);
			for (var d : right)
				consumer.accept(d);
			if (girder != null)
				girder.collectCrumblingInstances(consumer);
		}

		private class GirderVisual {

			private final Couple<TransformedInstance[]> beams;
			private final Couple<Couple<TransformedInstance[]>> beamCaps;
			private final BlockPos[] lightPos;

			private GirderVisual(BezierConnection bc) {
				BlockPos tePosition = bc.bePositions.getFirst();
				PoseStack pose = new PoseStack();
				TransformStack.of(pose)
					.translate(getVisualPosition())
					.nudge((int) bc.bePositions.getFirst()
						.asLong());

				int segCount = bc.getSegmentCount();
				beams = Couple.create(() -> new TransformedInstance[segCount]);
				beamCaps = Couple.create(() -> Couple.create(() -> new TransformedInstance[segCount]));
				lightPos = new BlockPos[segCount];
				beams.forEach(instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GIRDER_SEGMENT_MIDDLE))::createInstances);
				beamCaps.forEachWithContext((c, top) -> {
					var partialModel = Models.partial(top ? AllPartialModels.GIRDER_SEGMENT_TOP : AllPartialModels.GIRDER_SEGMENT_BOTTOM);
					c.forEach(instancerProvider().instancer(InstanceTypes.TRANSFORMED, partialModel)::createInstances);
				});

				GirderAngles[] bakedGirders = bc.getBakedGirders();
				for (int i = 1; i < bakedGirders.length; i++) {
					GirderAngles segment = bakedGirders[i];
					var modelIndex = i - 1;
					lightPos[modelIndex] = segment.lightPosition.offset(tePosition);

					for (boolean first : Iterate.trueAndFalse) {
						Pose beamTransform = segment.beams.get(first);
						beams.get(first)[modelIndex].setTransform(pose)
							.mul(beamTransform)
							.setChanged();
						for (boolean top : Iterate.trueAndFalse) {
							Pose beamCapTransform = segment.beamCaps.get(top)
								.get(first);
							beamCaps.get(top)
								.get(first)[modelIndex].setTransform(pose)
								.mul(beamCapTransform)
								.setChanged();
						}
					}
				}

				updateLight();
			}

			void delete() {
				beams.forEach(arr -> {
					for (var d : arr)
						d.delete();
				});
				beamCaps.forEach(c -> c.forEach(arr -> {
					for (var d : arr)
						d.delete();
				}));
			}

			void updateLight() {
				beams.forEach(arr -> {
					for (int i = 0; i < arr.length; i++)
						TrackVisual.updateLight(arr[i], level, lightPos[i]);
				});
				beamCaps.forEach(c -> c.forEach(arr -> {
					for (int i = 0; i < arr.length; i++)
						TrackVisual.updateLight(arr[i], level, lightPos[i]);
				}));
			}

			public void collectCrumblingInstances(Consumer<Instance> consumer) {
				beams.forEach(arr -> {
					for (var d : arr)
						consumer.accept(d);
				});
				beamCaps.forEach(c -> c.forEach(arr -> {
					for (var d : arr)
						consumer.accept(d);
				}));
			}
		}

	}

	private static void updateLight(FlatLit instance, Level level, BlockPos pos) {
		instance.light(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos))
				.setChanged();
	}
}
