package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.foundation.data.BasicParticleData;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Quaternion;

public class SoulParticle extends CustomRotationParticle {

	private final IAnimatedSprite animatedSprite;

	public SoulParticle(ClientWorld worldIn, double x, double y, double z, double vx, double vy, double vz,
						IAnimatedSprite spriteSet) {
		super(worldIn, x, y, z, spriteSet, 0);
		this.animatedSprite = spriteSet;
		this.particleScale = 0.5f;
		this.setSize(this.particleScale,this.particleScale);
		this.maxAge = (int)(16.0F / (this.rand.nextFloat() * 0.36F + 0.64F));
		this.selectSpriteWithAge(animatedSprite);
		this.field_21507 = true; // disable movement
	}

	@Override
	public void tick() {
		if (this.age++ >= this.maxAge) {
			this.setExpired();
		} else {
			this.selectSpriteWithAge(animatedSprite);
		}
	}

	@Override
	public Quaternion getCustomRotation(ActiveRenderInfo camera, float partialTicks) {
		return new Quaternion(0, -camera.getYaw(), 0, true);
	}

	public static class Data extends BasicParticleData<SoulParticle> {
		@Override
		public IBasicParticleFactory<SoulParticle> getBasicFactory() {
			return SoulParticle::new;
		}
		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL.get();
		}
	}
}
