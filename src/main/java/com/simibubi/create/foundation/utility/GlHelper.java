package com.simibubi.create.foundation.utility;

import org.lwjgl.opengl.GL11;

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

	public static void enableTextureRepeat() {
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10242, GL11.GL_REPEAT);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10243, GL11.GL_REPEAT);
	}
	
	public static void disableTextureRepeat() {
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10242, GL11.GL_CLAMP);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, 10243, GL11.GL_CLAMP);
	}
	
}
