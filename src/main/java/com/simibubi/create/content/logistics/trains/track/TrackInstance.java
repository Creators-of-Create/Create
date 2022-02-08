package com.simibubi.create.content.logistics.trains.track;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.light.LightUpdater;
import com.jozufozu.flywheel.util.FlwUtil;
import com.jozufozu.flywheel.util.box.GridAlignedBB;
import com.jozufozu.flywheel.util.box.ImmutableBox;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.logistics.trains.BezierConnection;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class TrackInstance extends BlockEntityInstance<TrackTileEntity> {

	private List<BezierInstance> instances;

	public TrackInstance(MaterialManager materialManager, TrackTileEntity track) {
		super(materialManager, track);

		update();
	}

	@Override
	public void update() {
		if (blockEntity.connections.stream().allMatch(Map::isEmpty)) {
			return;
		}

		instances = blockEntity.connections.stream()
				.flatMap(FlwUtil::mapValues)
				.map(this::createInstance)
				.filter(Objects::nonNull)
				.toList();
		LightUpdater.get(world).addListener(this);
	}

	@Override
	public ImmutableBox getVolume() {
		List<BlockPos> out = new ArrayList<>();
		out.addAll(blockEntity.connections.getFirst()
				.keySet());
		out.addAll(blockEntity.connections.getSecond()
				.keySet());
		return GridAlignedBB.containingAll(out);
	}

	@Override
	public void updateLight() {
		if (instances == null) return;
		instances.forEach(BezierInstance::updateLight);
	}

	@Nullable
	private BezierInstance createInstance(BezierConnection bc) {
		if (!bc.isPrimary()) return null;
		return new BezierInstance(bc);
	}

	@Override
	public void remove() {
		if (instances == null) return;
		instances.forEach(BezierInstance::delete);
	}

	private class BezierInstance {

		private final ModelData[] ties;
		private final ModelData[] left;
		private final ModelData[] right;
		private final BlockPos[] tiesLightPos;
		private final BlockPos[] leftLightPos;
		private final BlockPos[] rightLightPos;

		private BezierInstance(BezierConnection bc) {
			BlockPos tePosition = bc.tePositions.getFirst();

			PoseStack pose = new PoseStack();
			TransformStack.cast(pose)
					.translate(getInstancePosition())
					.nudge((int) bc.tePositions.getFirst()
							.asLong());

			var mat = materialManager.cutout(RenderType.cutoutMipped())
					.material(Materials.TRANSFORMED);

			int segCount = bc.getSegmentCount();
			ties = new ModelData[segCount];
			left = new ModelData[segCount];
			right = new ModelData[segCount];
			tiesLightPos = new BlockPos[segCount];
			leftLightPos = new BlockPos[segCount];
			rightLightPos = new BlockPos[segCount];

			mat.getModel(AllBlockPartials.TRACK_TIE)
					.createInstances(ties);
			mat.getModel(AllBlockPartials.TRACK_SEGMENT_LEFT)
					.createInstances(left);
			mat.getModel(AllBlockPartials.TRACK_SEGMENT_RIGHT)
					.createInstances(right);

			Vec3 leftPrevious = null;
			Vec3 rightPrevious = null;

			for (BezierConnection.Segment segment : bc) {
				Vec3 left = segment.position.add(segment.normal.scale(.965f));
				Vec3 right = segment.position.subtract(segment.normal.scale(.965f));

				if (leftPrevious != null) {
					var modelIndex = segment.index - 1;
					{
						// Tie
						Vec3 railMiddle = left.add(right)
								.scale(.5);
						Vec3 prevMiddle = leftPrevious.add(rightPrevious)
								.scale(.5);

						var tie = ties[modelIndex].setTransform(pose);
						Vec3 diff = railMiddle.subtract(prevMiddle);
						Vec3 angles = TrackRenderer.getModelAngles(segment.normal, diff);

						tie.translate(prevMiddle)
							.rotateYRadians(angles.y)
							.rotateXRadians(angles.x)
							.rotateZRadians(angles.z)
							.translate(-1 / 2f, -2 / 16f - 1 / 256f, 0);
						tiesLightPos[modelIndex] = new BlockPos(railMiddle).offset(tePosition);
					}

					// Rails
					for (boolean first : Iterate.trueAndFalse) {
						Vec3 railI = first ? left : right;
						Vec3 prevI = first ? leftPrevious : rightPrevious;

						var rail = (first ? this.left : this.right)[modelIndex].setTransform(pose);
						Vec3 diff = railI.subtract(prevI);
						Vec3 angles = TrackRenderer.getModelAngles(segment.normal, diff);

						rail.translate(prevI)
							.rotateYRadians(angles.y)
							.rotateXRadians(angles.x)
							.rotateZRadians(angles.z)
							.translate(0, -2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 256f, 0)
							.scale(1, 1, (float) diff.length() * 2.1f);
						(first ? leftLightPos : rightLightPos)[modelIndex] = new BlockPos(prevI).offset(tePosition);
					}
				}

				leftPrevious = left;
				rightPrevious = right;
			}

			updateLight();
		}

		void delete() {
			for (ModelData d : ties) d.delete();
			for (ModelData d : left) d.delete();
			for (ModelData d : right) d.delete();
		}

		void updateLight() {
			for (int i = 0; i < ties.length; i++) {
				ties[i].updateLight(world, tiesLightPos[i]);
			}
			for (int i = 0; i < left.length; i++) {
				left[i].updateLight(world, leftLightPos[i]);
			}
			for (int i = 0; i < right.length; i++) {
				right[i].updateLight(world, rightLightPos[i]);
			}
		}
	}
}
