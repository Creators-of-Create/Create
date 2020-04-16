package com.simibubi.create.foundation.utility.outliner;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AABBOutline extends Outline {

	private AxisAlignedBB bb = new AxisAlignedBB(new BlockPos(25, 70, 90)).expand(0, 1, 0);

	@Override
	public void render(BufferBuilder buffer) {
		begin();

		Vec3d color = ColorHelper.getRGB(0xFFFFFF);
		float alpha = 1f;

		AllSpecialTextures.BLANK.bind();
		Vec3d xyz = new Vec3d(bb.minX, bb.minY, bb.minZ);
		Vec3d Xyz = new Vec3d(bb.maxX, bb.minY, bb.minZ);
		Vec3d xYz = new Vec3d(bb.minX, bb.maxY, bb.minZ);
		Vec3d XYz = new Vec3d(bb.maxX, bb.maxY, bb.minZ);
		Vec3d xyZ = new Vec3d(bb.minX, bb.minY, bb.maxZ);
		Vec3d XyZ = new Vec3d(bb.maxX, bb.minY, bb.maxZ);
		Vec3d xYZ = new Vec3d(bb.minX, bb.maxY, bb.maxZ);
		Vec3d XYZ = new Vec3d(bb.maxX, bb.maxY, bb.maxZ);

		Vec3d start = xyz;
		renderAACuboidLine(start, Xyz, color, alpha, buffer);
		renderAACuboidLine(start, xYz, color, alpha, buffer);
		renderAACuboidLine(start, xyZ, color, alpha, buffer);

		start = XyZ;
		renderAACuboidLine(start, xyZ, color, alpha, buffer);
		renderAACuboidLine(start, XYZ, color, alpha, buffer);
		renderAACuboidLine(start, Xyz, color, alpha, buffer);

		start = XYz;
		renderAACuboidLine(start, xYz, color, alpha, buffer);
		renderAACuboidLine(start, Xyz, color, alpha, buffer);
		renderAACuboidLine(start, XYZ, color, alpha, buffer);

		start = xYZ;
		renderAACuboidLine(start, XYZ, color, alpha, buffer);
		renderAACuboidLine(start, xyZ, color, alpha, buffer);
		renderAACuboidLine(start, xYz, color, alpha, buffer);

		draw();
	}

}
