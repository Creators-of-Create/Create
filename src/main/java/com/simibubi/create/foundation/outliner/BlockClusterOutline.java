package com.simibubi.create.foundation.outliner;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;

public class BlockClusterOutline extends Outline {

	private final Cluster cluster;

	protected final Vector3f pos0Temp = new Vector3f();
	protected final Vector3f pos1Temp = new Vector3f();
	protected final Vector3f pos2Temp = new Vector3f();
	protected final Vector3f pos3Temp = new Vector3f();
	protected final Vector3f normalTemp = new Vector3f();
	protected final Vector3f originTemp = new Vector3f();

	public BlockClusterOutline(Iterable<BlockPos> positions) {
		cluster = new Cluster();
		positions.forEach(cluster::include);
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt) {
		params.loadColor(colorTemp);
		Vector4f color = colorTemp;
		int lightmap = params.lightmap;
		boolean disableLineNormals = params.disableLineNormals;

		renderFaces(ms, buffer, camera, pt, color, lightmap);
		renderEdges(ms, buffer, camera, pt, color, lightmap, disableLineNormals);
	}

	protected void renderFaces(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt, Vector4f color, int lightmap) {
		Optional<AllSpecialTextures> optionalFaceTexture = params.faceTexture;
		if (!optionalFaceTexture.isPresent())
			return;
		if (cluster.isEmpty())
			return;

		ms.pushPose();
		ms.translate(cluster.anchor.getX() - camera.x, cluster.anchor.getY() - camera.y,
			cluster.anchor.getZ() - camera.z);

		AllSpecialTextures faceTexture = optionalFaceTexture.get();
		PoseStack.Pose pose = ms.last();
		RenderType renderType = RenderTypes.getOutlineTranslucent(faceTexture.getLocation(), true);
		VertexConsumer consumer = buffer.getLateBuffer(renderType);

		cluster.visibleFaces.forEach((face, axisDirection) -> {
			Direction direction = Direction.get(axisDirection, face.axis);
			BlockPos pos = face.pos;
			if (axisDirection == AxisDirection.POSITIVE)
				pos = pos.relative(direction.getOpposite());
			bufferBlockFace(pose, consumer, pos, direction, color, lightmap);
		});

		ms.popPose();
	}

	protected void renderEdges(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, float pt, Vector4f color, int lightmap, boolean disableNormals) {
		float lineWidth = params.getLineWidth();
		if (lineWidth == 0)
			return;
		if (cluster.isEmpty())
			return;

		ms.pushPose();
		ms.translate(cluster.anchor.getX() - camera.x, cluster.anchor.getY() - camera.y,
			cluster.anchor.getZ() - camera.z);

		PoseStack.Pose pose = ms.last();
		VertexConsumer consumer = buffer.getBuffer(RenderTypes.getOutlineSolid());

		cluster.visibleEdges.forEach(edge -> {
			BlockPos pos = edge.pos;
			Vector3f origin = originTemp;
			origin.set(pos.getX(), pos.getY(), pos.getZ());
			Direction direction = Direction.get(AxisDirection.POSITIVE, edge.axis);
			bufferCuboidLine(pose, consumer, origin, direction, 1, lineWidth, color, lightmap, disableNormals);
		});

		ms.popPose();
	}

	public static void loadFaceData(Direction face, Vector3f pos0, Vector3f pos1, Vector3f pos2, Vector3f pos3, Vector3f normal) {
		switch (face) {
		case DOWN -> {
			// 0 1 2 3
			pos0.set(0, 0, 1);
			pos1.set(0, 0, 0);
			pos2.set(1, 0, 0);
			pos3.set(1, 0, 1);
			normal.set(0, -1, 0);
		}
		case UP -> {
			// 4 5 6 7
			pos0.set(0, 1, 0);
			pos1.set(0, 1, 1);
			pos2.set(1, 1, 1);
			pos3.set(1, 1, 0);
			normal.set(0, 1, 0);
		}
		case NORTH -> {
			// 7 2 1 4
			pos0.set(1, 1, 0);
			pos1.set(1, 0, 0);
			pos2.set(0, 0, 0);
			pos3.set(0, 1, 0);
			normal.set(0, 0, -1);
		}
		case SOUTH -> {
			// 5 0 3 6
			pos0.set(0, 1, 1);
			pos1.set(0, 0, 1);
			pos2.set(1, 0, 1);
			pos3.set(1, 1, 1);
			normal.set(0, 0, 1);
		}
		case WEST -> {
			// 4 1 0 5
			pos0.set(0, 1, 0);
			pos1.set(0, 0, 0);
			pos2.set(0, 0, 1);
			pos3.set(0, 1, 1);
			normal.set(-1, 0, 0);
		}
		case EAST -> {
			// 6 3 2 7
			pos0.set(1, 1, 1);
			pos1.set(1, 0, 1);
			pos2.set(1, 0, 0);
			pos3.set(1, 1, 0);
			normal.set(1, 0, 0);
		}
		}
	}

	public static void addPos(float x, float y, float z, Vector3f pos0, Vector3f pos1, Vector3f pos2, Vector3f pos3) {
		pos0.add(x, y, z);
		pos1.add(x, y, z);
		pos2.add(x, y, z);
		pos3.add(x, y, z);
	}

	protected void bufferBlockFace(PoseStack.Pose pose, VertexConsumer consumer, BlockPos pos, Direction face, Vector4f color, int lightmap) {
		Vector3f pos0 = pos0Temp;
		Vector3f pos1 = pos1Temp;
		Vector3f pos2 = pos2Temp;
		Vector3f pos3 = pos3Temp;
		Vector3f normal = normalTemp;

		loadFaceData(face, pos0, pos1, pos2, pos3, normal);
		addPos(pos.getX() + face.getStepX() * (1 / 128f),
			   pos.getY() + face.getStepY() * (1 / 128f),
			   pos.getZ() + face.getStepZ() * (1 / 128f),
				pos0, pos1, pos2, pos3);

		bufferQuad(pose, consumer, pos0, pos1, pos2, pos3, color, lightmap, normal);
	}

	private static class Cluster {

		private BlockPos anchor;
		private final Map<MergeEntry, AxisDirection> visibleFaces;
		private final Set<MergeEntry> visibleEdges;

		public Cluster() {
			visibleEdges = new ObjectOpenHashSet<>();
			visibleFaces = new Object2ObjectOpenHashMap<>();
		}

		public boolean isEmpty() {
			return anchor == null;
		}

		public void include(BlockPos pos) {
			if (anchor == null)
				anchor = pos;
			int dx = pos.getX() - anchor.getX();
			int dy = pos.getY() - anchor.getY();
			int dz = pos.getZ() - anchor.getZ();

			// 6 FACES
			for (Axis axis : Iterate.axes) {
				Direction direction = Direction.get(AxisDirection.POSITIVE, axis);
				for (int offset : Iterate.zeroAndOne) {
					MergeEntry entry = new MergeEntry(axis, pos.relative(direction, offset));
					if(!visibleFaces.containsKey(entry)) {
						visibleFaces.put(entry, offset == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
					} else {
						visibleFaces.remove(entry);
					}
				}
			}

			// 12 EDGES
			for (Axis axis : Iterate.axes) {
				for (Axis axis2 : Iterate.axes) {
					if (axis == axis2)
						continue;
					for (Axis axis3 : Iterate.axes) {
						if (axis == axis3 || axis2 == axis3)
							continue;

						for (int offset : Iterate.zeroAndOne) {
							BlockPos entryPos = new BlockPos(
									dx + (axis2 == Axis.X ? offset : 0),
									dy + (axis2 == Axis.Y ? offset : 0),
									dz + (axis2 == Axis.Z ? offset : 0)
							);

							for (int offset2 : Iterate.zeroAndOne) {
								BlockPos finalEntryPos = new BlockPos(
										entryPos.getX() + (axis3 == Axis.X ? offset2 : 0),
										entryPos.getY() + (axis3 == Axis.Y ? offset2 : 0),
										entryPos.getZ() + (axis3 == Axis.Z ? offset2 : 0)
								);
								MergeEntry entry = new MergeEntry(axis, finalEntryPos);
								if(!visibleEdges.contains(entry)) {
									visibleEdges.add(entry);
								} else {
									visibleEdges.remove(entry);
								}
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
			if (!(o instanceof MergeEntry other))
				return false;

			return this.axis == other.axis && this.pos.equals(other.pos);
		}

		@Override
		public int hashCode() {
			return this.pos.hashCode() * 31 + axis.ordinal();
		}
	}
}
