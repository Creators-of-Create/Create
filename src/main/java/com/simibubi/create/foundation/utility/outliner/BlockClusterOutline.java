package com.simibubi.create.foundation.utility.outliner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BlockClusterOutline extends Outline {

	private Cluster cluster;
	private float alpha;

	public BlockClusterOutline(Iterable<BlockPos> selection) {
		cluster = new Cluster();
		selection.forEach(cluster::include);
		alpha = .5f;
	}

	@Override
	public void render(BufferBuilder buffer) {
		begin();
		Vec3d color = ColorHelper.getRGB(0xDDDDDD);
		AllSpecialTextures.SELECTION.bind();

		for (MergeEntry face : cluster.visibleFaces.keySet()) {
			AxisDirection axisDirection = cluster.visibleFaces.get(face);
			Direction direction = Direction.getFacingFromAxis(axisDirection, face.axis);
			BlockPos pos = face.pos;
			if (axisDirection == AxisDirection.POSITIVE)
				pos = pos.offset(direction.getOpposite());
			renderFace(pos, direction, color, alpha * .25f, 1 / 64d, buffer);
		}

		flush();
		AllSpecialTextures.BLANK.bind();

		for (MergeEntry edge : cluster.visibleEdges) {
			lineWidth = 1 / 16f * alpha;
			Vec3d start = new Vec3d(edge.pos);
			Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, edge.axis);
			renderAACuboidLine(start, new Vec3d(edge.pos.offset(direction)), color, 1, buffer);
		}

		draw();
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	private static class Cluster {

		Map<MergeEntry, AxisDirection> visibleFaces;
		Set<MergeEntry> visibleEdges;

		public Cluster() {
			visibleEdges = new HashSet<>();
			visibleFaces = new HashMap<>();
		}

		public void include(BlockPos pos) {

			// 6 FACES
			for (Axis axis : Axis.values()) {
				Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
				for (int offset : new int[] { 0, 1 }) {
					MergeEntry entry = new MergeEntry(axis, pos.offset(direction, offset));
					if (visibleFaces.remove(entry) == null)
						visibleFaces.put(entry, offset == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
				}
			}

			// 12 EDGES
			for (Axis axis : Axis.values()) {
				for (Axis axis2 : Axis.values()) {
					if (axis == axis2)
						continue;
					for (Axis axis3 : Axis.values()) {
						if (axis == axis3)
							continue;
						if (axis2 == axis3)
							continue;

						Direction direction = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis2);
						Direction direction2 = Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis3);

						for (int offset : new int[] { 0, 1 }) {
							BlockPos entryPos = pos.offset(direction, offset);
							for (int offset2 : new int[] { 0, 1 }) {
								entryPos = entryPos.offset(direction2, offset2);
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

		Axis axis;
		BlockPos pos;

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
