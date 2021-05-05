package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;

public class FlywheelListeners {

	private final List<SetupFrame> setupFrameListeners = new ArrayList<>();
	private final List<RenderLayer> renderLayerListeners = new ArrayList<>();
	private final List<Refresh> refreshListeners = new ArrayList<>();

	public void setupFrameListener(SetupFrame setupFrame) {
		setupFrameListeners.add(setupFrame);
	}

	public void renderLayerListener(RenderLayer renderLayer) {
		renderLayerListeners.add(renderLayer);
	}

	public void refreshListener(Refresh refresh) {
		refreshListeners.add(refresh);
	}

	public void setupFrame(ClientWorld world, MatrixStack stack, ActiveRenderInfo info, GameRenderer gameRenderer, LightTexture lightTexture) {
		for (SetupFrame listener : setupFrameListeners) {
			listener.setupFrame(world, stack, info, gameRenderer, lightTexture);
		}
	}

	public void renderLayer(ClientWorld world, RenderType type, Matrix4f stack, double camX, double camY, double camZ) {
		for (RenderLayer listener : renderLayerListeners) {
			listener.renderLayer(world, type, stack, camX, camY, camZ);
		}
	}

	public void refresh(ClientWorld world) {
		for (Refresh listener : refreshListeners) {
			listener.refresh(world);
		}
	}

	@FunctionalInterface
	public interface SetupFrame {
		void setupFrame(ClientWorld world, MatrixStack stack, ActiveRenderInfo info, GameRenderer gameRenderer, LightTexture lightTexture);
	}

	@FunctionalInterface
	public interface RenderLayer {
		void renderLayer(ClientWorld world, RenderType type, Matrix4f viewProjection, double camX, double camY, double camZ);
	}

	@FunctionalInterface
	public interface Refresh {
		void refresh(ClientWorld world);
	}
}
