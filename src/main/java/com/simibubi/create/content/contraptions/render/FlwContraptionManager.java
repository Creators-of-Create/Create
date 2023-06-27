package com.simibubi.create.content.contraptions.render;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.config.BackendType;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.compile.ProgramContext;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.Textures;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.render.CreateContexts;

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

		GlStateTracker.State restoreState = GlStateTracker.getRestoreState();
		GlTextureUnit active = GlTextureUnit.getActive();

		var backendType = Backend.getBackendType();
		if (backendType != BackendType.OFF) {
			renderStructures(event);
		}

		if (backendType != BackendType.BATCHING && event.getLayer() != null) {
			for (FlwContraption renderer : visible) {
				renderer.renderInstanceLayer(event);
			}
		}

		// clear the light volume state
		GlTextureUnit.T4.makeActive();
		glBindTexture(GL_TEXTURE_3D, 0);

		event.type.clearRenderState();
		active.makeActive();
		restoreState.restore();
	}

	private void renderStructures(RenderLayerEvent event) {

		event.type.setupRenderState();
		Textures.bindActiveTextures();

		ContraptionProgram structureShader = CreateContexts.STRUCTURE.getProgram(ProgramContext.create(Materials.Names.PASSTHRU, Formats.BLOCK, RenderLayer.getLayer(event.type)));

		structureShader.bind();
		structureShader.uploadViewProjection(event.viewProjection);
		structureShader.uploadCameraPos(event.camX, event.camY, event.camZ);

		for (FlwContraption flwContraption : visible) {
			flwContraption.renderStructureLayer(event.type, structureShader);
		}
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
