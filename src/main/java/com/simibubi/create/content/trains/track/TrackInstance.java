package com.simibubi.create.content.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.box.Box;
import com.jozufozu.flywheel.lib.box.MutableBox;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.light.LightUpdater;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.trains.track.BezierConnection.GirderAngles;
import com.simibubi.create.content.trains.track.BezierConnection.SegmentAngles;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.model.data.ModelData;

public class TrackInstance extends AbstractBlockEntityVisual<TrackBlockEntity> {

	private List<BezierTrackInstance> instances;

	public TrackInstance(VisualizationContext materialManager, TrackBlockEntity track) {
		super(materialManager, track);
	}

	@Override
	public void update(float pt) {
		if (blockEntity.connections.isEmpty())
			return;

		_delete();
		instances = blockEntity.connections.values()
			.stream()
			.map(this::createInstance)
			.filter(Objects::nonNull)
			.toList();
		LightUpdater.get(level)
			.addListener(this);
	}

	@Override
	public Box getVolume() {
		List<BlockPos> out = new ArrayList<>();
		out.addAll(blockEntity.connections.keySet());
		out.addAll(blockEntity.connections.keySet());
		return MutableBox.containingAll(out);
	}

	@Override
	public void updateLight() {
		if (instances == null)
			return;
		instances.forEach(BezierTrackInstance::updateLight);
	}

	@Nullable
	private BezierTrackInstance createInstance(BezierConnection bc) {
		if (!bc.isPrimary())
			return null;
		return new BezierTrackInstance(bc);
	}

	@Override
	public void _delete() {
		if (instances == null)
			return;
		instances.forEach(BezierTrackInstance::delete);
	}

	private class BezierTrackInstance {

		private final TransformedInstance[] ties;
		private final TransformedInstance[] left;
		private final TransformedInstance[] right;
		private final BlockPos[] tiesLightPos;
		private final BlockPos[] leftLightPos;
		private final BlockPos[] rightLightPos;

		private @Nullable GirderInstance girder;

		private BezierTrackInstance(BezierConnection bc) {
			BlockPos tePosition = bc.tePositions.getFirst();
			girder = bc.hasGirder ? new GirderInstance(bc) : null;

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

			instancerProvider.instancerr(InstanceTypes.TRANSFORMED, Models.partial(modelHolder.tie()), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstances(ties);
			instancerProvider.instancerr(InstanceTypes.TRANSFORMED, Models.partial(modelHolder.segment_left()), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstances(left);
			instancerProvider.instancerr(InstanceTypes.TRANSFORMED, Models.partial(modelHolder.segment_right()), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstances(right);

			SegmentAngles[] segments = bc.getBakedSegments();
			for (int i = 1; i < segments.length; i++) {
				SegmentAngles segment = segments[i];
				var modelIndex = i - 1;

				ties[modelIndex].setTransform(pose)
					.mulPose(segment.tieTransform.pose())
					.mulNormal(segment.tieTransform.normal());
				tiesLightPos[modelIndex] = segment.lightPosition.offset(tePosition);

				for (boolean first : Iterate.trueAndFalse) {
					Pose transform = segment.railTransforms.get(first);
					(first ? this.left : this.right)[modelIndex].setTransform(pose)
						.mulPose(transform.pose())
						.mulNormal(transform.normal());
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
				ties[i].updateLight(level, tiesLightPos[i]);
			for (int i = 0; i < left.length; i++)
				left[i].updateLight(level, leftLightPos[i]);
			for (int i = 0; i < right.length; i++)
				right[i].updateLight(level, rightLightPos[i]);
			if (girder != null)
				girder.updateLight();
		}

		private class GirderInstance {

			private final Couple<TransformedInstance[]> beams;
			private final Couple<Couple<TransformedInstance[]>> beamCaps;
			private final BlockPos[] lightPos;

			private GirderInstance(BezierConnection bc) {
				BlockPos tePosition = bc.tePositions.getFirst();
				PoseStack pose = new PoseStack();
				TransformStack.of(pose)
					.translate(getVisualPosition())
					.nudge((int) bc.tePositions.getFirst()
						.asLong());

				int segCount = bc.getSegmentCount();
				beams = Couple.create(() -> new TransformedInstance[segCount]);
				beamCaps = Couple.create(() -> Couple.create(() -> new TransformedInstance[segCount]));
				lightPos = new BlockPos[segCount];
				beams.forEach(instancerProvider.instancerr(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.GIRDER_SEGMENT_MIDDLE), RenderStage.AFTER_BLOCK_ENTITIES)::createInstances);
				beamCaps.forEachWithContext((c, top) -> {
					var partialModel = Models.partial(top ? AllPartialModels.GIRDER_SEGMENT_TOP : AllPartialModels.GIRDER_SEGMENT_BOTTOM);
					c.forEach(instancerProvider.instancerr(InstanceTypes.TRANSFORMED, partialModel, RenderStage.AFTER_BLOCK_ENTITIES)::createInstances);
				});

				GirderAngles[] bakedGirders = bc.getBakedGirders();
				for (int i = 1; i < bakedGirders.length; i++) {
					GirderAngles segment = bakedGirders[i];
					var modelIndex = i - 1;
					lightPos[modelIndex] = segment.lightPosition.offset(tePosition);

					for (boolean first : Iterate.trueAndFalse) {
						Pose beamTransform = segment.beams.get(first);
						beams.get(first)[modelIndex].setTransform(pose)
							.mulPose(beamTransform.pose())
							.mulNormal(beamTransform.normal());
						for (boolean top : Iterate.trueAndFalse) {
							Pose beamCapTransform = segment.beamCaps.get(top)
								.get(first);
							beamCaps.get(top)
								.get(first)[modelIndex].setTransform(pose)
									.mulPose(beamCapTransform.pose())
									.mulNormal(beamCapTransform.normal());
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
						arr[i].updateLight(level, lightPos[i]);
				});
				beamCaps.forEach(c -> c.forEach(arr -> {
					for (int i = 0; i < arr.length; i++)
						arr[i].updateLight(level, lightPos[i]);
				}));
			}

		}

	}
}
