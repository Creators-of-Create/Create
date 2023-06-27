package com.simibubi.create.content.equipment.bell;

import org.joml.Quaternionf;

import com.mojang.math.Axis;
import com.simibubi.create.AllParticleTypes;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleType;

public class SoulBaseParticle extends CustomRotationParticle {

	private final SpriteSet animatedSprite;

	public SoulBaseParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz,
                            SpriteSet spriteSet) {
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

		BlockPos pos = BlockPos.containing(x, y, z);
		if (age++ >= lifetime || !SoulPulseEffect.isDark(level, pos))
			remove();
	}

	@Override
	public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
		return Axis.XP.rotationDegrees(90);
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
