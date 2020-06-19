package com.simibubi.create.foundation.collision;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
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
import net.minecraft.util.math.Vec3d;

public class CollisionDebugger {

	static AxisAlignedBB staticBB = new AxisAlignedBB(BlockPos.ZERO.up(10));
	static OrientedBB movingBB = new OrientedBB(new AxisAlignedBB(BlockPos.ZERO));
	static Vec3d seperation;
	static double angle = 0;
	static AABBOutline outline;

	public static void onScroll(double delta) {
		angle += delta;
		movingBB.setRotation(new Matrix3d().asZRotation(AngleHelper.rad(angle)));
	}

	public static void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		ms.push();
		outline = new AABBOutline(movingBB.getAsAxisAlignedBB());
		outline.getParams()
			.withFaceTexture(seperation == null ? AllSpecialTextures.CHECKERED : null)
			.colored(0xffffff);
		if (seperation != null)
			outline.getParams()
				.lineWidth(1 / 64f)
				.colored(0xff6544);
		MatrixStacker.of(ms)
			.translate(movingBB.center);
		ms.peek()
			.getModel()
			.multiply(movingBB.rotation.getAsMatrix4f());
		MatrixStacker.of(ms)
			.translateBack(movingBB.center);
		outline.render(ms, buffer);
		ms.pop();

		ms.push();
		if (seperation != null) {
			outline.getParams()
				.colored(0x65ff44)
				.lineWidth(1 / 32f);
			MatrixStacker.of(ms)
				.translate(seperation)
				.translate(movingBB.center);
			ms.peek()
				.getModel()
				.multiply(movingBB.rotation.getAsMatrix4f());
			MatrixStacker.of(ms)
				.translateBack(movingBB.center);
			outline.render(ms, buffer);
		}
		ms.pop();
	}

	public static void tick() {
		staticBB = new AxisAlignedBB(BlockPos.ZERO.up(60));
		RayTraceResult mouse = Minecraft.getInstance().objectMouseOver;
		if (mouse != null && mouse.getType() == Type.BLOCK) {
			BlockRayTraceResult hit = (BlockRayTraceResult) mouse;
			movingBB.setCenter(hit.getHitVec());
			seperation = movingBB.intersect(staticBB);
		}
		CreateClient.outliner.showAABB(staticBB, staticBB)
			.withFaceTexture(seperation == null ? AllSpecialTextures.CHECKERED : null);
	}

}
