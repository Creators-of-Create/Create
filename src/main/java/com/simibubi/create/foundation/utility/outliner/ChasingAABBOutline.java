package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

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
		setBounds(interpolateBBs(bb, targetBB, .5f));
	}

	@Override
	public void render(MatrixStack ms, SuperRenderTypeBuffer buffer) {
		renderBB(ms, buffer, interpolateBBs(prevBB, bb, AnimationTickHolder.getPartialTicks()));
	}

	private static AxisAlignedBB interpolateBBs(AxisAlignedBB current, AxisAlignedBB target, float pt) {
		return new AxisAlignedBB(MathHelper.lerp(pt, current.minX, target.minX),
			MathHelper.lerp(pt, current.minY, target.minY), MathHelper.lerp(pt, current.minZ, target.minZ),
			MathHelper.lerp(pt, current.maxX, target.maxX), MathHelper.lerp(pt, current.maxY, target.maxY),
			MathHelper.lerp(pt, current.maxZ, target.maxZ));
	}

}
