package com.simibubi.create.content.contraptions.components.structureMovement.render;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.state.RenderLayer;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.TextureBinder;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.render.AllProgramSpecs;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.LevelAccessor;

public class FlwContraptionManager extends ContraptionRenderManager<RenderedContraption> {

	public FlwContraptionManager(LevelAccessor world) {
		super(world);
	}

	@Override
	public void tick() {
		super.tick();

		for (RenderedContraption contraption : visible) {
			contraption.kinetics.tick();
		}
	}

	@Override
	public void renderLayer(RenderLayerEvent event) {
		super.renderLayer(event);

		if (visible.isEmpty()) return;

		GlTextureUnit active = GlTextureUnit.getActive();

		RenderType layer = event.getType();

		layer.setupRenderState();

		TextureBinder.bindActiveTextures();

		ContraptionProgram structureShader = CreateContexts.STRUCTURE.getProgram(AllProgramSpecs.STRUCTURE);

		structureShader.bind();
		structureShader.uploadViewProjection(event.viewProjection);
		structureShader.uploadCameraPos(event.camX, event.camY, event.camZ);

		for (RenderedContraption renderedContraption : visible) {
			renderedContraption.doRenderLayer(layer, structureShader);
		}

		if (Backend.getInstance().canUseInstancing()) {
			RenderLayer renderLayer = event.getLayer();
			if (renderLayer != null) {
				for (RenderedContraption renderer : visible) {
					renderer.materialManager.render(event, event.buffers.bufferSource());
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
	protected RenderedContraption create(Contraption c) {
		PlacementSimulationWorld renderWorld = ContraptionRenderDispatcher.setupRenderWorld(world, c);
		return new RenderedContraption(c, renderWorld);
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
