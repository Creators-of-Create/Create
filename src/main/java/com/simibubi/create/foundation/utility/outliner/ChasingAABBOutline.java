package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ChasingAABBOutline extends AABBOutline {

	AxisAlignedBB targetBB;
	AxisAlignedBB prevBB;

	public ChasingAABBOutline(AxisAlignedBB bb) {
		super(bb);
		prevBB = bb.grow(0);
		targetBB = bb.grow(0);
	}

	public void target(AxisAlignedBB target) {
		targetBB = target;
	}

	@Override
	public void tick() {
		prevBB = bb;
		bb = interpolateBBs(bb, targetBB, .5f);
	}

	@Override
	public void render(MatrixStack ms, IRenderTypeBuffer buffer) {
		Vec3d color = ColorHelper.getRGB(0xFFFFFF);
		float alpha = 1f;
		renderBB(ms, buffer, interpolateBBs(prevBB, bb, Minecraft.getInstance()
			.getRenderPartialTicks()), color, alpha, true);
	}

	private static AxisAlignedBB interpolateBBs(AxisAlignedBB current, AxisAlignedBB target, float pt) {
		return new AxisAlignedBB(MathHelper.lerp(pt, current.minX, target.minX),
			MathHelper.lerp(pt, current.minY, target.minY), MathHelper.lerp(pt, current.minZ, target.minZ),
			MathHelper.lerp(pt, current.maxX, target.maxX), MathHelper.lerp(pt, current.maxY, target.maxY),
			MathHelper.lerp(pt, current.maxZ, target.maxZ));
	}

}
