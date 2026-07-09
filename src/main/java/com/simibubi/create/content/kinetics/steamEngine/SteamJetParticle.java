package com.simibubi.create.content.kinetics.steamEngine;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SteamJetParticle extends SimpleAnimatedParticle {

	private float yaw, pitch;

	protected SteamJetParticle(ClientLevel world, SteamJetParticleData data, double x, double y, double z, double dx,
		double dy, double dz, SpriteSet sprite) {
		super(world, x, y, z, sprite, world.getRandom().nextFloat() * .5f);
		xd = 0;
		yd = 0;
		zd = 0;
		gravity = 0;
		quadSize = .375f;
		setLifetime(21);
		setPos(x, y, z);
		roll = oRoll = world.getRandom().nextFloat() * Mth.PI;
		yaw = (float) Mth.atan2(dx, dz) - Mth.PI;
		pitch = (float) Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) - Mth.PI / 2;
		this.setSpriteFromAge(sprite);
	}

	@Override
	public int getLightCoords(float partialTick) {
		BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
		return this.level.isLoaded(blockpos) ? com.simibubi.create.foundation.render.LegacyLightTexture.getLightColor(level, blockpos) : 0;
	}

	public static class Factory implements ParticleProvider<SteamJetParticleData> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle createParticle(SteamJetParticleData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			return new SteamJetParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

}
