package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.platform.Lighting;

public interface ILightingSettings {

	void applyLighting();

	static final ILightingSettings DEFAULT_3D = () -> Lighting.setupFor3DItems();
	static final ILightingSettings DEFAULT_FLAT = () -> Lighting.setupForFlatItems();

}
