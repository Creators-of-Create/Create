package com.simibubi.create.content.curiosities.bell;

import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import com.simibubi.create.content.curiosities.bell.BasicParticleData.IBasicParticleFactory;

public class SoulBaseParticle extends CustomRotationParticle {

	private final IAnimatedSprite animatedSprite;

	public SoulBaseParticle(ClientWorld worldIn, double x, double y, double z, double vx, double vy, double vz,
                            IAnimatedSprite spriteSet) {
		super(worldIn, x, y, z, spriteSet, 0);
		this.animatedSprite = spriteSet;
		this.quadSize = 0.5f;
		this.setSize(this.quadSize, this.quadSize);
		this.loopLength = 16 + (int) (this.random.nextFloat() * 2f - 1f);
		this.lifetime = (int) (90.0F / (this.random.nextFloat() * 0.36F + 0.64F));
		this.selectSpriteLoopingWithAge(animatedSprite);
		this.stoppedByCollision = true; // disable movement
	}

	@Override
	public void tick() {
		selectSpriteLoopingWithAge(animatedSprite);

		BlockPos pos = new BlockPos(x, y, z);
		if (age++ >= lifetime || !SoulPulseEffect.isDark(level, pos))
			remove();
	}

	@Override
	public Quaternion getCustomRotation(ActiveRenderInfo camera, float partialTicks) {
		return Vector3f.XP.rotationDegrees(90);
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
