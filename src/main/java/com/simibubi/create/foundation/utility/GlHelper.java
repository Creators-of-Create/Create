package com.simibubi.create.foundation.utility;

import org.lwjgl.opengl.GL11;

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

	public static void enableTextureRepeat() {
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10242, GL11.GL_REPEAT);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10243, GL11.GL_REPEAT);
	}
	
	public static void disableTextureRepeat() {
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10242, GL11.GL_CLAMP);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, 10243, GL11.GL_CLAMP);
	}
	
}
