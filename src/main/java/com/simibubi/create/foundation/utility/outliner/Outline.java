package com.simibubi.create.foundation.utility.outliner;

import org.lwjgl.opengl.GL11;

import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class Outline {

	protected float lineWidth = 1 / 32f;

	public abstract void render(BufferBuilder buffer);

	protected void begin() {
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

	protected void draw() {
		Tessellator.getInstance().draw();
	}

	protected void flush() {
		draw();
		begin();
	}

	protected void renderAACuboidLine(Vec3d start, Vec3d end, Vec3d rgb, float alpha, BufferBuilder buffer) {
		Vec3d diff = end.subtract(start);
		if (diff.x + diff.y + diff.z < 0) {
			Vec3d temp = start;
			start = end;
			end = temp;
			diff = diff.scale(-1);
		}

		Vec3d extension = diff.normalize().scale(lineWidth / 2);
		Vec3d plane = VecHelper.planeByNormal(diff);
		Axis axis = Direction.getFacingFromVector(diff.x, diff.y, diff.z).getAxis();

		start = start.subtract(extension);
		end = end.add(extension);
		plane = plane.scale(lineWidth / 2);
		Vec3d a1 = plane.add(start);
		Vec3d b1 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a2 = plane.add(start);
		Vec3d b2 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a3 = plane.add(start);
		Vec3d b3 = plane.add(end);
		plane = VecHelper.rotate(plane, -90, axis);
		Vec3d a4 = plane.add(start);
		Vec3d b4 = plane.add(end);

		putQuad(b4, b3, b2, b1, rgb, alpha, buffer);
		putQuad(a1, a2, a3, a4, rgb, alpha, buffer);

		putQuad(a1, b1, b2, a2, rgb, alpha, buffer);
		putQuad(a2, b2, b3, a3, rgb, alpha, buffer);
		putQuad(a3, b3, b4, a4, rgb, alpha, buffer);
		putQuad(a4, b4, b1, a1, rgb, alpha, buffer);
	}

	protected void renderFace(BlockPos pos, Direction face, Vec3d rgb, float alpha, double scaleOffset,
			BufferBuilder buffer) {
		Vec3d center = VecHelper.getCenterOf(pos);
		Vec3d offset = new Vec3d(face.getDirectionVec());
		Vec3d plane = VecHelper.planeByNormal(offset);
		Axis axis = face.getAxis();

		offset = offset.scale(1 / 2f + scaleOffset);
		plane = plane.scale(1 / 2f).add(offset);

		int deg = face.getAxisDirection().getOffset() * 90;
		Vec3d a1 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3d a2 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3d a3 = plane.add(center);
		plane = VecHelper.rotate(plane, deg, axis);
		Vec3d a4 = plane.add(center);

		putQuad(a1, a2, a3, a4, rgb, alpha, buffer);
	}

	protected void putQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, Vec3d rgb, float alpha, BufferBuilder buffer) {
		putVertex(v1, rgb, 0, 0, alpha, buffer);
		putVertex(v2, rgb, 1, 0, alpha, buffer);
		putVertex(v3, rgb, 1, 1, alpha, buffer);
		putVertex(v4, rgb, 0, 1, alpha, buffer);
	}

	protected void putVertex(Vec3d pos, Vec3d rgb, float u, float v, float alpha, BufferBuilder buffer) {
		int i = 15 << 20 | 15 << 4;
		int j = i >> 16 & '\uffff';
		int k = i & '\uffff';
		buffer.pos(pos.x, pos.y, pos.z).tex(u, v).color((float) rgb.x, (float) rgb.y, (float) rgb.z, alpha)
				.lightmap(j, k).endVertex();
	}

}
