package com.simibubi.create.content.logistics.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.content.logistics.trains.BezierConnection.GirderAngles;
import com.simibubi.create.content.logistics.trains.BezierConnection.SegmentAngles;
import com.simibubi.create.content.logistics.trains.TrackMaterial;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

public class TrackInstance extends BlockEntityInstance<TrackBlockEntity> {

	private List<BezierTrackInstance> instances;

	public TrackInstance(MaterialManager materialManager, TrackBlockEntity track) {
		super(materialManager, track);

		update();
	}

	@Override
	public void update() {
		if (blockEntity.connections.isEmpty())
			return;

		remove();
		instances = blockEntity.connections.values()
			.stream()
			.map(this::createInstance)
			.filter(Objects::nonNull)
			.toList();
		LightUpdater.get(world)
			.addListener(this);
	}

	@Override
	public ImmutableBox getVolume() {
		List<BlockPos> out = new ArrayList<>();
		out.addAll(blockEntity.connections.keySet());
		out.addAll(blockEntity.connections.keySet());
		return GridAlignedBB.containingAll(out);
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
	public void remove() {
		if (instances == null)
			return;
		instances.forEach(BezierTrackInstance::delete);
	}

	private class BezierTrackInstance {

		private final ModelData[] ties;
		private final ModelData[] left;
		private final ModelData[] right;
		private final BlockPos[] tiesLightPos;
		private final BlockPos[] leftLightPos;
		private final BlockPos[] rightLightPos;

		private @Nullable GirderInstance girder;

		private BezierTrackInstance(BezierConnection bc) {
			BlockPos tePosition = bc.tePositions.getFirst();
			girder = bc.hasGirder ? new GirderInstance(bc) : null;

			PoseStack pose = new PoseStack();
			TransformStack.cast(pose)
				.translate(getInstancePosition());

			var mat = materialManager.cutout(RenderType.cutoutMipped())
				.material(Materials.TRANSFORMED);

			int segCount = bc.getSegmentCount();
			ties = new ModelData[segCount];
			left = new ModelData[segCount];
			right = new ModelData[segCount];
			tiesLightPos = new BlockPos[segCount];
			leftLightPos = new BlockPos[segCount];
			rightLightPos = new BlockPos[segCount];

			TrackMaterial.TrackModelHolder modelHolder = bc.getMaterial().getModelHolder();

			mat.getModel(modelHolder.tie())
				.createInstances(ties);
			mat.getModel(modelHolder.segment_left())
				.createInstances(left);
			mat.getModel(modelHolder.segment_right())
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
			for (ModelData d : ties)
				d.delete();
			for (ModelData d : left)
				d.delete();
			for (ModelData d : right)
				d.delete();
			if (girder != null)
				girder.delete();
		}

		void updateLight() {
			for (int i = 0; i < ties.length; i++)
				ties[i].updateLight(world, tiesLightPos[i]);
			for (int i = 0; i < left.length; i++)
				left[i].updateLight(world, leftLightPos[i]);
			for (int i = 0; i < right.length; i++)
				right[i].updateLight(world, rightLightPos[i]);
			if (girder != null)
				girder.updateLight();
		}

		private class GirderInstance {

			private final Couple<ModelData[]> beams;
			private final Couple<Couple<ModelData[]>> beamCaps;
			private final BlockPos[] lightPos;

			private GirderInstance(BezierConnection bc) {
				BlockPos tePosition = bc.tePositions.getFirst();
				PoseStack pose = new PoseStack();
				TransformStack.cast(pose)
					.translate(getInstancePosition())
					.nudge((int) bc.tePositions.getFirst()
						.asLong());

				var mat = materialManager.cutout(RenderType.cutoutMipped())
					.material(Materials.TRANSFORMED);

				int segCount = bc.getSegmentCount();
				beams = Couple.create(() -> new ModelData[segCount]);
				beamCaps = Couple.create(() -> Couple.create(() -> new ModelData[segCount]));
				lightPos = new BlockPos[segCount];
				beams.forEach(mat.getModel(AllPartialModels.GIRDER_SEGMENT_MIDDLE)::createInstances);
				beamCaps.forEachWithContext((c, top) -> c.forEach(mat.getModel(top ? AllPartialModels.GIRDER_SEGMENT_TOP
					: AllPartialModels.GIRDER_SEGMENT_BOTTOM)::createInstances));

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
					for (ModelData d : arr)
						d.delete();
				});
				beamCaps.forEach(c -> c.forEach(arr -> {
					for (ModelData d : arr)
						d.delete();
				}));
			}

			void updateLight() {
				beams.forEach(arr -> {
					for (int i = 0; i < arr.length; i++)
						arr[i].updateLight(world, lightPos[i]);
				});
				beamCaps.forEach(c -> c.forEach(arr -> {
					for (int i = 0; i < arr.length; i++)
						arr[i].updateLight(world, lightPos[i]);
				}));
			}

		}

	}
}
