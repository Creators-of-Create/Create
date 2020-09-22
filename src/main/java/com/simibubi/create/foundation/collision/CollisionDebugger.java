package com.simibubi.create.foundation.collision;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.collision.ContinuousOBBCollider.ContinuousSeparationManifold;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.outliner.AABBOutline;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;

public class CollisionDebugger {

	public static AxisAlignedBB AABB = new AxisAlignedBB(BlockPos.ZERO.up(10));
	public static OrientedBB OBB = new OrientedBB(new AxisAlignedBB(BlockPos.ZERO));
	public static Vector3d motion = Vector3d.ZERO;
	static ContinuousSeparationManifold seperation;
	static double angle = 0;
	static AABBOutline outline;

	public static void onScroll(double delta) {
		angle += delta;
		angle = (int) angle;
		OBB.setRotation(new Matrix3d().asZRotation(AngleHelper.rad(angle)));
	}

	public static void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
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

//		ms.push();
//		if (motion.length() != 0 && (seperation == null || seperation.getTimeOfImpact() != 1)) {
//			outline.getParams()
//				.colored(0x6544ff)
//				.lineWidth(1 / 32f);
//			MatrixStacker.of(ms)
//				.translate(seperation != null ? seperation.getAllowedMotion(motion) : motion)
//				.translate(OBB.center);
//			ms.peek()
//				.getModel()
//				.multiply(OBB.rotation.getAsMatrix4f());
//			MatrixStacker.of(ms)
//				.translateBack(OBB.center);
//			outline.render(ms, buffer);
//		}
//		ms.pop();

		ms.push();
		if (seperation != null) {
			Vector3d asSeparationVec = seperation.asSeparationVec(.5f);
			if (asSeparationVec != null) {
				outline.getParams()
					.colored(0x65ff44)
					.lineWidth(1 / 32f);
				MatrixStacker.of(ms)
					.translate(asSeparationVec)
					.translate(OBB.center);
				ms.peek()
					.getModel()
					.multiply(OBB.rotation.getAsMatrix4f());
				MatrixStacker.of(ms)
					.translateBack(OBB.center);
				outline.render(ms, buffer);
			}
		}
		ms.pop();
	}

	public static void tick() {
		AABB = new AxisAlignedBB(BlockPos.ZERO.up(60)).offset(.5, 0, .5);
		motion = Vector3d.ZERO;
		RayTraceResult mouse = Minecraft.getInstance().objectMouseOver;
		if (mouse != null && mouse.getType() == Type.BLOCK) {
			BlockRayTraceResult hit = (BlockRayTraceResult) mouse;
			OBB.setCenter(hit.getHitVec());
			seperation = OBB.intersect(AABB, motion);
		}
		CreateClient.outliner.showAABB(AABB, AABB)
			.withFaceTexture(seperation == null ? AllSpecialTextures.CHECKERED : null);
	}

	static void showDebugLine(Vector3d relativeStart, Vector3d relativeEnd, int color, String id, int offset) {
		Vector3d center = CollisionDebugger.AABB.getCenter()
			.add(0, 1 + offset / 16f, 0);
		CreateClient.outliner.showLine(id + OBBCollider.checkCount, center.add(relativeStart), center.add(relativeEnd))
			.colored(color)
			.lineWidth(1 / 32f);
	}

}
