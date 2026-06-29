package com.simibubi.create.content.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.trains.track.BezierConnection.GirderAngles;
import com.simibubi.create.content.trains.track.BezierConnection.SegmentAngles;
import com.simibubi.create.foundation.render.SpecialModels;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;

// Manually implement BlockEntityVisual because we don't need LightUpdatedVisual.
public class TrackVisual extends AbstractVisual implements BlockEntityVisual<TrackBlockEntity>, ShaderLightVisual {

	private final List<BezierTrackVisual> visuals = new ArrayList<>();

	protected final TrackBlockEntity blockEntity;
	protected final BlockPos pos;
	protected final BlockPos visualPos;
	@UnknownNullability
	protected SectionCollector lightSections;

	public TrackVisual(VisualizationContext context, TrackBlockEntity track, float partialTick) {
		super(context, track.getLevel(), partialTick);
		this.blockEntity = track;
		this.pos = blockEntity.getBlockPos();
		this.visualPos = pos.subtract(context.renderOrigin());

		collectConnections();
	}

	@Override
	public void setSectionCollector(SectionCollector sectionCollector) {
		this.lightSections = sectionCollector;
		lightSections.sections(collectLightSections());
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
		if (blockEntity.connections.isEmpty()) {
			return LongSet.of();
		}
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		for (BezierConnection connection : blockEntity.connections.values()) {
			// The start and end positions are not enough to enclose the entire curve.
			// Check the computed bounds but expand by one for safety.
			var bounds = connection.getBounds();
			minX = Math.min(minX, Mth.floor(bounds.minX) - 1);
			minY = Math.min(minY, Mth.floor(bounds.minY) - 1);
			minZ = Math.min(minZ, Mth.floor(bounds.minZ) - 1);
			maxX = Math.max(maxX, Mth.ceil(bounds.maxX) + 1);
			maxY = Math.max(maxY, Mth.ceil(bounds.maxY) + 1);
			maxZ = Math.max(maxZ, Mth.ceil(bounds.maxZ) + 1);
		}

		var minSectionX = SectionPos.blockToSectionCoord(minX);
		var minSectionY = SectionPos.blockToSectionCoord(minY);
		var minSectionZ = SectionPos.blockToSectionCoord(minZ);
		int maxSectionX = SectionPos.blockToSectionCoord(maxX);
		int maxSectionY = SectionPos.blockToSectionCoord(maxY);
		int maxSectionZ = SectionPos.blockToSectionCoord(maxZ);

		LongSet out = new LongArraySet();

		for (int x = minSectionX; x <= maxSectionX; x++) {
			for (int y = minSectionY; y <= maxSectionY; y++) {
				for (int z = minSectionZ; z <= maxSectionZ; z++) {
					out.add(SectionPos.asLong(x, y, z));
				}
			}
		}

		return out;
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

		private @Nullable GirderVisual girder;

		private BezierTrackVisual(BezierConnection bc) {
			girder = bc.hasGirder ? new GirderVisual(bc) : null;

			PoseStack pose = new PoseStack();
			TransformStack.of(pose)
				.translate(visualPos);

			int segCount = bc.getSegmentCount();
			ties = new TransformedInstance[segCount];
			left = new TransformedInstance[segCount];
			right = new TransformedInstance[segCount];

			TrackMaterial.TrackModelHolder modelHolder = bc.getMaterial().getModelHolder();

			instancerProvider().instancer(InstanceTypes.TRANSFORMED, SpecialModels.flatChunk(modelHolder.tie()))
				.createInstances(ties);
			instancerProvider().instancer(InstanceTypes.TRANSFORMED, SpecialModels.flatChunk(modelHolder.leftSegment()))
				.createInstances(left);
			instancerProvider().instancer(InstanceTypes.TRANSFORMED, SpecialModels.flatChunk(modelHolder.rightSegment()))
				.createInstances(right);

			SegmentAngles segment = bc.getBakedSegments();
			for (int i = 1; i < segment.length; i++) {
				var modelIndex = i - 1;

				ties[modelIndex].setTransform(pose)
					.mul(segment.tieTransform[i])
					.setChanged();

				for (boolean first : Iterate.trueAndFalse) {
					Pose transform = segment.railTransforms[i].get(first);
					(first ? this.left : this.right)[modelIndex].setTransform(pose)
						.mul(transform)
						.setChanged();
				}
			}
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

			private GirderVisual(BezierConnection bc) {
				PoseStack pose = new PoseStack();
				TransformStack.of(pose)
					.translate(visualPos)
					.nudge((int) bc.bePositions.getFirst()
						.asLong());

				int segCount = bc.getSegmentCount();
				beams = Couple.create(() -> new TransformedInstance[segCount]);
				beamCaps = Couple.create(() -> Couple.create(() -> new TransformedInstance[segCount]));
				beams.forEach(instancerProvider().instancer(InstanceTypes.TRANSFORMED, SpecialModels.flatChunk(AllPartialModels.GIRDER_SEGMENT_MIDDLE))::createInstances);
				beamCaps.forEachWithContext((c, top) -> {
					var partialModel = SpecialModels.flatChunk(top ? AllPartialModels.GIRDER_SEGMENT_TOP : AllPartialModels.GIRDER_SEGMENT_BOTTOM);
					c.forEach(instancerProvider().instancer(InstanceTypes.TRANSFORMED, partialModel)::createInstances);
				});

				GirderAngles segment = bc.getBakedGirders();
				for (int i = 1; i < segment.length; i++) {
					var modelIndex = i - 1;

					for (boolean first : Iterate.trueAndFalse) {
						Pose beamTransform = segment.beams[i].get(first);
						beams.get(first)[modelIndex].setTransform(pose)
							.mul(beamTransform)
							.setChanged();
						for (boolean top : Iterate.trueAndFalse) {
							Pose beamCapTransform = segment.beamCaps[i].get(top)
								.get(first);
							beamCaps.get(top)
								.get(first)[modelIndex].setTransform(pose)
								.mul(beamCapTransform)
								.setChanged();
						}
					}
				}
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
}
