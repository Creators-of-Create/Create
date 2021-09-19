package com.simibubi.create.foundation.ponder;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import com.mojang.blaze3d.vertex.Tesselator;

public class PonderWorldParticles {

	private final Map<ParticleRenderType, Queue<Particle>> byType = Maps.newIdentityHashMap();
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

	public void renderParticles(PoseStack ms, MultiBufferSource buffer, Camera renderInfo, float pt) {
		Minecraft mc = Minecraft.getInstance();
		LightTexture lightTexture = mc.gameRenderer.lightTexture();

		lightTexture.turnOnLightLayer();
		Runnable enable = () -> {
//			RenderSystem.enableAlphaTest();
//			RenderSystem.defaultAlphaFunc();
			RenderSystem.enableDepthTest();
//			RenderSystem.enableFog();
		};
		ms.pushPose();

		for (ParticleRenderType iparticlerendertype : this.byType.keySet()) {
			if (iparticlerendertype == ParticleRenderType.NO_RENDER)
				continue;
			enable.run();
			Iterable<Particle> iterable = this.byType.get(iparticlerendertype);
			if (iterable != null) {
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				Tesselator tessellator = Tesselator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuilder();
				iparticlerendertype.begin(bufferbuilder, mc.textureManager);

				for (Particle particle : iterable)
					particle.render(bufferbuilder, renderInfo, pt);

				iparticlerendertype.end(tessellator);
			}
		}

		ms.popPose();
		RenderSystem.depthMask(true);
		RenderSystem.depthFunc(515);
		RenderSystem.disableBlend();
//		RenderSystem.defaultAlphaFunc();
		lightTexture.turnOffLightLayer();
//		RenderSystem.disableFog();
	}

	public void clearEffects() {
		this.byType.clear();
	}

}
