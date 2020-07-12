package com.simibubi.create.foundation.collision;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

public class CollisionDebugger {

	public static AxisAlignedBB AABB = null;
	public static OrientedBB OBB = null;
	static Vec3d seperation;
	static double angle = 0;
	static AABBOutline outline;

	public static void onScroll(double delta) {
//		angle += delta;
//		movingBB = new OrientedBB(new AxisAlignedBB(BlockPos.ZERO).expand(0, 1, 0));
//		movingBB.setRotation(new Matrix3d().asZRotation(AngleHelper.rad(angle)));
	}

	public static void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		if (OBB == null)
			return;
		ms.push();
		outline = new AABBOutline(OBB.getAsAxisAlignedBB());
		outline.getParams()
			.withFaceTexture(seperation == null ? AllSpecialTextures.CHECKERED : null)
			.colored(0xffffff);
		if (seperation != null)
			outline.getParams()
				.lineWidth(1 / 64f)
				.colored(0xff6544);
		MatrixStacker.of(ms)
			.translate(OBB.center);
		ms.peek()
			.getModel()
			.multiply(OBB.rotation.getAsMatrix4f());
		MatrixStacker.of(ms)
			.translateBack(OBB.center);
		outline.render(ms, buffer);
		ms.pop();

		ms.push();
		if (seperation != null) {
			outline.getParams()
				.colored(0x65ff44)
				.lineWidth(1 / 32f);
			MatrixStacker.of(ms)
				.translate(seperation)
				.translate(OBB.center);
			ms.peek()
				.getModel()
				.multiply(OBB.rotation.getAsMatrix4f());
			MatrixStacker.of(ms)
				.translateBack(OBB.center);
			outline.render(ms, buffer);
		}
		ms.pop();
	}

	public static void tick() {
		if (OBB == null)
			return;
		if (AABB == null)
			return;
		seperation = OBB.intersect(AABB);
		CreateClient.outliner.showAABB(AABB, AABB)
			.withFaceTexture(seperation == null ? AllSpecialTextures.CHECKERED : null);
	}

}
