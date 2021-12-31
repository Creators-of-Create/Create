package com.simibubi.create.content.contraptions.components.structureMovement.render;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.Textures;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.CreateContexts;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.LevelAccessor;

public class FlwContraptionManager extends ContraptionRenderingWorld<FlwContraption> {

	public FlwContraptionManager(LevelAccessor world) {
		super(world);
	}

	@Override
	public void tick() {
		super.tick();

		for (FlwContraption contraption : visible) {
			contraption.tick();
		}
	}

	@Override
	public void renderLayer(RenderLayerEvent event) {
		super.renderLayer(event);

		if (visible.isEmpty()) return;

		GlTextureUnit active = GlTextureUnit.getActive();

		RenderType layer = event.getType();

		layer.setupRenderState();

		Textures.bindActiveTextures();

		ContraptionProgram structureShader = CreateContexts.STRUCTURE.getProgram(AllProgramSpecs.STRUCTURE);

		structureShader.bind();
		structureShader.uploadViewProjection(event.viewProjection);
		structureShader.uploadCameraPos(event.camX, event.camY, event.camZ);

		for (FlwContraption flwContraption : visible) {
			flwContraption.renderStructureLayer(layer, structureShader);
		}

		GlVertexArray.unbind();

        if (Backend.isOn()) {
			RenderLayer renderLayer = event.getLayer();
			if (renderLayer != null) {

				for (FlwContraption renderer : visible) {
					renderer.renderInstanceLayer(event);
				}
			}
		}

		// clear the light volume state
		GlTextureUnit.T4.makeActive();
		glBindTexture(GL_TEXTURE_3D, 0);

		layer.clearRenderState();
		active.makeActive();
	}

	@Override
	protected FlwContraption create(Contraption c) {
		VirtualRenderWorld renderWorld = ContraptionRenderDispatcher.setupRenderWorld(world, c);
		return new FlwContraption(c, renderWorld);
	}

	@Override
	public void removeDeadRenderers() {
		boolean removed = renderInfos.values()
				.removeIf(renderer -> {
					if (renderer.isDead()) {
						renderer.invalidate();
						return true;
					}
					return false;
				});

		// we use visible in #tick() so we have to re-evaluate it if any were removed
		if (removed) collectVisible();
	}
}
