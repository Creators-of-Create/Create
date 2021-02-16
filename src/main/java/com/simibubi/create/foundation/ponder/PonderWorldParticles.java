package com.simibubi.create.foundation.ponder;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;

public class PonderWorldParticles {

	private final Map<IParticleRenderType, Queue<Particle>> byType = Maps.newIdentityHashMap();
	private final Queue<Particle> queue = Queues.newArrayDeque();

	PonderWorld world;

	public PonderWorldParticles(PonderWorld world) {
		this.world = world;
	}

	public void addParticle(Particle p) {
		this.queue.add(p);
	}

	public void tick() {
		this.byType.forEach((p_228347_1_, p_228347_2_) -> this.tickParticleList(p_228347_2_));

		Particle particle;
		if (queue.isEmpty())
			return;
		while ((particle = this.queue.poll()) != null)
			this.byType.computeIfAbsent(particle.getRenderType(), $ -> EvictingQueue.create(16384))
				.add(particle);
	}

	private void tickParticleList(Collection<Particle> p_187240_1_) {
		if (p_187240_1_.isEmpty())
			return;

		Iterator<Particle> iterator = p_187240_1_.iterator();
		while (iterator.hasNext()) {
			Particle particle = iterator.next();
			particle.tick();
			if (!particle.isAlive())
				iterator.remove();
		}
	}

	public void renderParticles(MatrixStack ms, IRenderTypeBuffer buffer, ActiveRenderInfo p_228345_4_) {
		Minecraft mc = Minecraft.getInstance();
		LightTexture p_228345_3_ = mc.gameRenderer.getLightmapTextureManager();
		float p_228345_5_ = mc.getRenderPartialTicks();

		p_228345_3_.enableLightmap();
		Runnable enable = () -> {
			RenderSystem.enableAlphaTest();
			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableDepthTest();
			RenderSystem.enableFog();
		};
		RenderSystem.pushMatrix();
		RenderSystem.multMatrix(ms.peek()
			.getModel());

		for (IParticleRenderType iparticlerendertype : this.byType.keySet()) { // Forge: allow custom
																				// IParticleRenderType's
			if (iparticlerendertype == IParticleRenderType.NO_RENDER)
				continue;
			enable.run(); // Forge: MC-168672 Make sure all render types have the correct GL state.
			Iterable<Particle> iterable = this.byType.get(iparticlerendertype);
			if (iterable != null) {
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				iparticlerendertype.beginRender(bufferbuilder, mc.textureManager);

				for (Particle particle : iterable)
					particle.buildGeometry(bufferbuilder, p_228345_4_, p_228345_5_);

				iparticlerendertype.finishRender(tessellator);
			}
		}

		RenderSystem.popMatrix();
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.defaultAlphaFunc();
		p_228345_3_.disableLightmap();
		RenderSystem.disableFog();
	}

	public void clearEffects() {
		this.byType.clear();
	}

}
