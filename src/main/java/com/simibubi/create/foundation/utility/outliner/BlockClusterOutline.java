package com.simibubi.create.foundation.utility.outliner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;

public class BlockClusterOutline extends Outline {

	private Cluster cluster;

	public BlockClusterOutline(Iterable<BlockPos> selection) {
		cluster = new Cluster();
		selection.forEach(cluster::include);
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt) {
		cluster.visibleEdges.forEach(edge -> {
			Vec3 start = Vec3.atLowerCornerOf(edge.pos);
			Direction direction = Direction.get(AxisDirection.POSITIVE, edge.axis);
			renderAACuboidLine(ms, buffer, start, Vec3.atLowerCornerOf(edge.pos.relative(direction)));
		});

		Optional<AllSpecialTextures> faceTexture = params.faceTexture;
		if (!faceTexture.isPresent())
			return;

		RenderType translucentType = RenderTypes.getOutlineTranslucent(faceTexture.get()
			.getLocation(), true);
		VertexConsumer builder = buffer.getLateBuffer(translucentType);

		cluster.visibleFaces.forEach((face, axisDirection) -> {
			Direction direction = Direction.get(axisDirection, face.axis);
			BlockPos pos = face.pos;
			if (axisDirection == AxisDirection.POSITIVE)
				pos = pos.relative(direction.getOpposite());
			renderBlockFace(ms, builder, pos, direction);
		});
	}

	protected void renderBlockFace(PoseStack ms, VertexConsumer builder, BlockPos pos, Direction face) {
		Vec3 center = VecHelper.getCenterOf(pos);
		Vec3 offset = Vec3.atLowerCornerOf(face.getNormal());
		Vec3 plane = VecHelper.axisAlingedPlaneOf(offset);
		Axis axis = face.getAxis();

		offset = offset.scale(1 / 2f + 1 / 128d);
		plane = plane.scale(1 / 2f)
			.add(offset);

		int deg = face.getAxisDirection()
			.getStep() * 90;
		Vec3 a1 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3 a2 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3 a3 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3 a4 = plane.add(center);

		putQuad(ms, builder, a1, a2, a3, a4, face);
	}

	private static class Cluster {

		private Map<MergeEntry, AxisDirection> visibleFaces;
		private Set<MergeEntry> visibleEdges;

		public Cluster() {
			visibleEdges = new HashSet<>();
			visibleFaces = new HashMap<>();
		}

		public void include(BlockPos pos) {

			// 6 FACES
			for (Axis axis : Iterate.axes) {
				Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
				for (int offset : Iterate.zeroAndOne) {
					MergeEntry entry = new MergeEntry(axis, pos.relative(direction, offset));
					if (visibleFaces.remove(entry) == null)
						visibleFaces.put(entry, offset == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
				}
			}

			// 12 EDGES
			for (Axis axis : Iterate.axes) {
				for (Axis axis2 : Iterate.axes) {
					if (axis == axis2)
						continue;
					for (Axis axis3 : Iterate.axes) {
						if (axis == axis3)
							continue;
						if (axis2 == axis3)
							continue;

						Direction direction = Direction.get(AxisDirection.POSITIVE, axis2);
						Direction direction2 = Direction.get(AxisDirection.POSITIVE, axis3);

						for (int offset : Iterate.zeroAndOne) {
							BlockPos entryPos = pos.relative(direction, offset);
							for (int offset2 : Iterate.zeroAndOne) {
								entryPos = entryPos.relative(direction2, offset2);
								MergeEntry entry = new MergeEntry(axis, entryPos);
								if (!visibleEdges.remove(entry))
									visibleEdges.add(entry);
							}
						}
					}

					break;
				}
			}

		}

	}

	private static class MergeEntry {

		private Axis axis;
		private BlockPos pos;

		public MergeEntry(Axis axis, BlockPos pos) {
			this.axis = axis;
			this.pos = pos;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof MergeEntry))
				return false;

			MergeEntry other = (MergeEntry) o;
			return this.axis == other.axis && this.pos.equals(other.pos);
		}

		@Override
		public int hashCode() {
			return this.pos.hashCode() * 31 + axis.ordinal();
		}
	}

}
