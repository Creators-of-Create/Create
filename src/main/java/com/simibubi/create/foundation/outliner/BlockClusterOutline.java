package com.simibubi.create.foundation.outliner;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
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
		cluster = Cluster.of(positions);
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
		Pose pose = ms.last();
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

		Pose pose = ms.last();
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

	protected void bufferBlockFace(Pose pose, VertexConsumer consumer, BlockPos pos, Direction face, Vector4f color, int lightmap) {
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

		private static final Map<Iterable<BlockPos>, Cluster> cache = new Object2ObjectOpenHashMap<>();

		private BlockPos anchor;
		private final Map<MergeEntry, AxisDirection> visibleFaces;
		private final Set<MergeEntry> visibleEdges;

		private Cluster(BlockPos anchor) {
			visibleEdges = new ObjectOpenHashSet<>();
			visibleFaces = new Object2ObjectOpenHashMap<>();
			this.anchor = anchor;
		}

		public boolean isEmpty() {
			return anchor == null;
		}

		public static Cluster of(Iterable<BlockPos> positions) {
			return cache.computeIfAbsent(positions, p -> {
				Cluster cluster = new Cluster(positions.iterator().next());
				cluster.include(convertToSet(positions));
				return cluster;
			});
		}

		private static Set<BlockPos> convertToSet(Iterable<BlockPos> positions) {
			if(positions instanceof ObjectOpenHashSet<BlockPos> set) { // usually will happen with glue
				return set;
			}
			Set<BlockPos> set = new ObjectOpenHashSet<>();
			positions.forEach(set::add);
			return set;
		}

		public void include(Set<BlockPos> nonRelativePositions) {
			if(nonRelativePositions.isEmpty()) {
				return;
			}
			Set<BlockPos> positions = new ObjectLinkedOpenHashSet<>();
			nonRelativePositions.forEach(p -> positions.add(p.subtract(anchor)));

			Table table = new Table(positions);
			System.out.println("table: " + (table.xLength() - 2) + "x" + (table.yLength() - 2) + "x" + (table.zLength() - 2));

			for(BlockPos pos : positions) {
				addFaces(pos, table);
				addEdges(pos, table);
			}
		}

		private void addFaces(BlockPos pos, Table table) {
			for(Direction d : Iterate.directions) {
				BlockPos offset = pos.relative(d);
				if(!table.get(offset)) {
					visibleFaces.put(new MergeEntry(d.getAxis(), pos), d.getAxisDirection());
				}
			}
		}

		private void addEdges(BlockPos pos, Table table) {
			for(Direction d : Iterate.directions) {
				for(Direction d2 : Iterate.directions) {
					if(d == d2 || d == d2.getOpposite()) continue;

					BlockPos offset = pos.relative(d).relative(d2);
					if(!table.get(offset)) {
						visibleEdges.add(new MergeEntry(d.getAxis(), pos));
					}
				}
			}
		}
	}

	private static class Table {
		private final boolean[][][] table;

		public Table(Set<BlockPos> positions){
			BlockPos extreme = extreme(positions);
			System.out.println("extreme: " + extreme);

			// add 3: +2 for each side, +1 for the block itself
			table = new boolean[extreme.getX() + 3][extreme.getY() + 3][extreme.getZ() + 3];

			for(BlockPos pos : positions) {
				table[Math.abs(pos.getX()) + 1][Math.abs(pos.getY()) + 1][Math.abs(pos.getZ()) + 1] = true;
			}
		}

		public int xLength() {
			return table.length;
		}

		public int yLength() {
			return table[0].length;
		}

		public int zLength() {
			return table[0][0].length;
		}

		public boolean get(BlockPos pos) {
			return table[Math.abs(pos.getX()) + 1][Math.abs(pos.getY()) + 1][Math.abs(pos.getZ()) + 1];
		}

		private static BlockPos extreme(Set<BlockPos> positions) {
			int x = 0, y = 0, z = 0;
			for(BlockPos pos : positions) {
				int curX = Math.abs(pos.getX()), curY = Math.abs(pos.getY()), curZ = Math.abs(pos.getZ());
				if(curX > x) x = curX;
				if(curY > y) y = curY;
				if(curZ > z) z = curZ;
			}

			return new BlockPos(x, y, z);
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
