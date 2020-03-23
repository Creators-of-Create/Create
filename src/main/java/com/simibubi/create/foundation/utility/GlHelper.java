package com.simibubi.create.foundation.utility;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.util.math.Vec3d;

public class GlHelper {

	public static void renderTransformed(Vec3d position, Vec3d rotation, float scale, Runnable render) {
		if (position == null)
			return;

		RenderSystem.pushMatrix();
		RenderSystem.translated(position.x, position.y, position.z);
		RenderSystem.rotatef((float) rotation.y, 0, 1, 0);
		RenderSystem.rotatef((float) rotation.z, 1, 0, 0);
		RenderSystem.rotatef((float) rotation.x, 0, 0, 1);
		RenderSystem.scaled(scale, scale, scale);
		render.run();
		RenderSystem.popMatrix();
	}

}
