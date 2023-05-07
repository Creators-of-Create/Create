package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class ChasingAABBOutline extends AABBOutline {

	AABB targetBB;
	AABB prevBB;

	public ChasingAABBOutline(AABB bb) {
		super(bb);
		prevBB = bb.inflate(0);
		targetBB = bb.inflate(0);
	}

	public void target(AABB target) {
		targetBB = target;
	}

	@Override
	public void tick() {
		prevBB = bb;
		setBounds(interpolateBBs(bb, targetBB, .5f));
	}

	@Override
	public void render(PoseStack ms, SuperRenderTypeBuffer buffer, float pt) {
		renderBB(ms, buffer, interpolateBBs(prevBB, bb, pt));
	}

	private static AABB interpolateBBs(AABB current, AABB target, float pt) {
		return new AABB(Mth.lerp(pt, current.minX, target.minX),
			Mth.lerp(pt, current.minY, target.minY), Mth.lerp(pt, current.minZ, target.minZ),
			Mth.lerp(pt, current.maxX, target.maxX), Mth.lerp(pt, current.maxY, target.maxY),
			Mth.lerp(pt, current.maxZ, target.maxZ));
	}

}
