package com.simibubi.create.foundation.utility.outliner;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class OutlineParticle extends Particle {

	private Outline outline;

	private OutlineParticle(Outline outline, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn);
		this.outline = outline;
		this.maxAge = 1024;
	}

	public static OutlineParticle create(Outline outline) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		OutlineParticle effect = new OutlineParticle(outline, mc.world, player.posX, player.posY, player.posZ);
		mc.particles.addEffect(effect);
		return effect;
	}

	public void remove() {
		isExpired = true;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, ActiveRenderInfo entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		Vec3d view = entityIn.getProjectedView();
		GlStateManager.translated(-view.x, -view.y, -view.z);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		GlStateManager.enableBlend();
		outline.render(buffer);
		GlStateManager.disableBlend();

		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.CUSTOM;
	}

}
