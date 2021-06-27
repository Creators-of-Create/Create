package com.simibubi.create.foundation.gui;

import net.minecraft.client.renderer.RenderHelper;

public interface ILightingSettings {

	void applyLighting();

	static final ILightingSettings DEFAULT_3D = () -> RenderHelper.enableGuiDepthLighting();
	static final ILightingSettings DEFAULT_FLAT = () -> RenderHelper.disableGuiDepthLighting();

}
