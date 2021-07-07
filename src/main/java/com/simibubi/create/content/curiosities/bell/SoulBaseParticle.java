package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class SoulBaseParticle extends CustomRotationParticle {

	private final IAnimatedSprite animatedSprite;

	public SoulBaseParticle(ClientWorld worldIn, double x, double y, double z, double vx, double vy, double vz,
                            IAnimatedSprite spriteSet) {
		super(worldIn, x, y, z, spriteSet, 0);
		this.animatedSprite = spriteSet;
		this.particleScale = 0.5f;
		this.setSize(this.particleScale, this.particleScale);
		this.loopLength = 16 + (int) (this.rand.nextFloat() * 2f - 1f);
		this.maxAge = (int) (90.0F / (this.rand.nextFloat() * 0.36F + 0.64F));
		this.selectSpriteLoopingWithAge(animatedSprite);
		this.field_21507 = true; // disable movement
	}

	@Override
	public void tick() {
		selectSpriteLoopingWithAge(animatedSprite);

		BlockPos pos = new BlockPos(posX, posY, posZ);
		if (age++ >= maxAge || !SoulPulseEffect.canSpawnSoulAt(world, pos, false))
			setExpired();
	}

	@Override
	public Quaternion getCustomRotation(ActiveRenderInfo camera, float partialTicks) {
		return Vector3f.POSITIVE_X.getDegreesQuaternion(90);
	}

	public static class Data extends BasicParticleData<SoulBaseParticle> {
		@Override
		public IBasicParticleFactory<SoulBaseParticle> getBasicFactory() {
			return SoulBaseParticle::new;
		}

		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.SOUL_BASE.get();
		}
	}
}
