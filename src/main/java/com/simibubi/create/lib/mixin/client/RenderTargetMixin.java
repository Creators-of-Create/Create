package com.simibubi.create.lib.mixin.client;

import java.nio.IntBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.lib.extensions.RenderTargetExtensions;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin implements RenderTargetExtensions {

	@Shadow
	public abstract void resize(int width, int height, boolean clearError);

	@Shadow
	public int viewWidth;
	@Shadow
	public int viewHeight;

	@Shadow
	protected int depthBufferId;

	@Shadow
	public int width;

	@Shadow
	public int height;

	@Redirect(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V", ordinal = 1, remap = false))
	private void stencilBuffer(int i, int j, int k, int l, int m) {
		if(!stencilEnabled)
			GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, this.depthBufferId, 0);
		else {
			GlStateManager._glFramebufferTexture2D(org.lwjgl.opengl.GL30.GL_FRAMEBUFFER, org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT, 3553, this.depthBufferId, 0);
			GlStateManager._glFramebufferTexture2D(org.lwjgl.opengl.GL30.GL_FRAMEBUFFER, org.lwjgl.opengl.GL30.GL_STENCIL_ATTACHMENT, 3553, this.depthBufferId, 0);
		}
	}

	@Redirect(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", ordinal = 0, remap = false))
	private void stencilBuffer1(int i, int j, int k, int l, int m, int n, int o, int p, IntBuffer intBuffer) {
		if (!stencilEnabled)
			GlStateManager._texImage2D(3553, 0, 6402, this.width, this.height, 0, 6402, 5126, null);
		else
			GlStateManager._texImage2D(3553, 0, org.lwjgl.opengl.GL30.GL_DEPTH32F_STENCIL8, this.width, this.height, 0, org.lwjgl.opengl.GL30.GL_DEPTH_STENCIL, org.lwjgl.opengl.GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
	}

	@Unique
	private boolean stencilEnabled = false;

	@Unique
	@Override
	public void enableStencil() {
		if(stencilEnabled) return;
		stencilEnabled = true;
		this.resize(viewWidth, viewHeight, net.minecraft.client.Minecraft.ON_OSX);
	}

	@Unique
	@Override
	public boolean isStencilEnabled() {
		return this.stencilEnabled;
	}
}
