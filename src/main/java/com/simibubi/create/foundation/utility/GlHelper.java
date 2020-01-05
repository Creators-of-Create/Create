package com.simibubi.create.foundation.utility;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.util.math.Vec3d;

public class GlHelper {

	public static void renderTransformed(Vec3d position, Vec3d rotation, float scale, Runnable render) {
		if (position == null)
			return;

		GlStateManager.pushMatrix();
		GlStateManager.translated(position.x, position.y, position.z);
		GlStateManager.rotated(rotation.y, 0, 1, 0);
		GlStateManager.rotated(rotation.z, 1, 0, 0);
		GlStateManager.rotated(rotation.x, 0, 0, 1);
		GlStateManager.scaled(scale, scale, scale);
		render.run();
		GlStateManager.popMatrix();
	}

}
