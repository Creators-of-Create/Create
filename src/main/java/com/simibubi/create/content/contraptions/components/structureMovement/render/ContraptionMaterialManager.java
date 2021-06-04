package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.MaterialRenderer;
import com.jozufozu.flywheel.core.WorldContext;
import com.jozufozu.flywheel.core.shader.IProgramCallback;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Matrix4f;

public class ContraptionMaterialManager extends MaterialManager<ContraptionProgram> {
	public ContraptionMaterialManager(WorldContext<ContraptionProgram> context) {
		super(context);
	}

	@Override
	public void render(RenderType layer, Matrix4f viewProjection, double camX, double camY, double camZ, IProgramCallback<ContraptionProgram> callback) {
		for (MaterialRenderer<ContraptionProgram> material : renderers) {
			material.render(layer, viewProjection, camX, camY, camZ, callback);
		}
	}
}
