package com.simibubi.create.foundation.mixin;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

@Mixin(BufferUploader.class)
public abstract class RenderSystemMixin {

	@Shadow
	private static void updateVertexSetup(VertexFormat pFormat) {
	}

	@Shadow
	private static int lastIndexBufferObject;

	/**
	 * @author e
	 */
	@Overwrite
	private static void _end(ByteBuffer pBuffer, VertexFormat.Mode pMode, VertexFormat pFormat, int pVertexCount, VertexFormat.IndexType pIndexType, int pIndexCount, boolean pSequentialIndex) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		pBuffer.clear();
		if (pVertexCount > 0) {
			int i = pVertexCount * pFormat.getVertexSize();
			updateVertexSetup(pFormat);
			pBuffer.position(0);
			pBuffer.limit(i);
			GlStateManager._glBufferData(34962, pBuffer, 35048);
			int j;
			if (pSequentialIndex) {
				RenderSystem.AutoStorageIndexBuffer rendersystem$autostorageindexbuffer = RenderSystem.getSequentialBuffer(pMode, pIndexCount);
				int k = rendersystem$autostorageindexbuffer.name();
				if (k != lastIndexBufferObject) {
					GlStateManager._glBindBuffer(34963, k);
					lastIndexBufferObject = k;
				}

				j = rendersystem$autostorageindexbuffer.type().asGLType;
			} else {
				int i1 = pFormat.getOrCreateIndexBufferObject();
				if (i1 != lastIndexBufferObject) {
					GlStateManager._glBindBuffer(34963, i1);
					lastIndexBufferObject = i1;
				}

				pBuffer.position(i);
				pBuffer.limit(i + pIndexCount * pIndexType.bytes);
				GlStateManager._glBufferData(34963, pBuffer, 35048);
				j = pIndexType.asGLType;
			}

			ShaderInstance shaderinstance = RenderSystem.getShader();
			if(shaderinstance == null) {
				shaderinstance = GameRenderer.getRendertypeTranslucentMovingBlockShader();
			}

			for(int j1 = 0; j1 < 8; ++j1) {
				int l = RenderSystem.getShaderTexture(j1);
				shaderinstance.setSampler("Sampler" + j1, l);
			}

			if (shaderinstance.MODEL_VIEW_MATRIX != null) {
				shaderinstance.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
			}

			if (shaderinstance.PROJECTION_MATRIX != null) {
				shaderinstance.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
			}

			if (shaderinstance.COLOR_MODULATOR != null) {
				shaderinstance.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
			}

			if (shaderinstance.FOG_START != null) {
				shaderinstance.FOG_START.set(RenderSystem.getShaderFogStart());
			}

			if (shaderinstance.FOG_END != null) {
				shaderinstance.FOG_END.set(RenderSystem.getShaderFogEnd());
			}

			if (shaderinstance.FOG_COLOR != null) {
				shaderinstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
			}

			if (shaderinstance.TEXTURE_MATRIX != null) {
				shaderinstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
			}

			if (shaderinstance.GAME_TIME != null) {
				shaderinstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
			}

			if (shaderinstance.SCREEN_SIZE != null) {
				Window window = Minecraft.getInstance().getWindow();
				shaderinstance.SCREEN_SIZE.set((float)window.getWidth(), (float)window.getHeight());
			}

			if (shaderinstance.LINE_WIDTH != null && (pMode == VertexFormat.Mode.LINES || pMode == VertexFormat.Mode.LINE_STRIP)) {
				shaderinstance.LINE_WIDTH.set(RenderSystem.getShaderLineWidth());
			}

			RenderSystem.setupShaderLights(shaderinstance);
			shaderinstance.apply();
			GlStateManager._drawElements(pMode.asGLMode, pIndexCount, j, 0L);
			shaderinstance.clear();
			pBuffer.position(0);
		}
	}
}
