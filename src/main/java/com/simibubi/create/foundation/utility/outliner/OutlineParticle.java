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

public class OutlineParticle<O extends Outline> extends Particle {

	protected O outline;

	protected OutlineParticle(O outline, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn);
		this.outline = outline;
		this.maxAge = 1024;
	}

	public static <O extends Outline> OutlineParticle<O> create(O outline) {
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		OutlineParticle<O> effect = new OutlineParticle<>(outline, mc.world, player.posX, player.posY, player.posZ);
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
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		GlStateManager.enableBlend();
		getOutline().render(buffer);
		GlStateManager.disableBlend();

		GlStateManager.popMatrix();
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.CUSTOM;
	}

	public O getOutline() {
		return outline;
	}

}
