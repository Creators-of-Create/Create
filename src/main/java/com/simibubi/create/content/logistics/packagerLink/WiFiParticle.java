package com.simibubi.create.content.logistics.packagerLink;

import org.joml.Quaternionf;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.equipment.bell.BasicParticleData;
import com.simibubi.create.content.equipment.bell.CustomRotationParticle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.util.Mth;

public class WiFiParticle extends CustomRotationParticle {

	private SpriteSet animatedSprite;
	private boolean downward;

	public WiFiParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz,
		SpriteSet spriteSet) {
		super(worldIn, x, y + (vy < 0 ? -1 : 1), z, spriteSet, 0);
		this.animatedSprite = spriteSet;
		this.quadSize = 0.5f;
		this.setSize(this.quadSize, this.quadSize);
		this.loopLength = 16;
		this.lifetime = 16;
		this.setSpriteFromAge(spriteSet);
		this.stoppedByCollision = true; // disable movement
		this.downward = vy < 0;
	}

	@Override
	public void tick() {
		setSpriteFromAge(animatedSprite);
		if (age++ >= lifetime)
			remove();
	}

	@Override
	public Quaternionf getCustomRotation(Camera camera, float partialTicks) {
		return new Quaternionf().rotateY(-camera.yRot() * Mth.DEG_TO_RAD)
			.mul(new Quaternionf().rotateZ(downward ? Mth.PI : 0));
	}

	public static class Data extends BasicParticleData<WiFiParticle> implements ParticleOptions {

		@Override
		public ParticleType<?> getType() {
			return AllParticleTypes.WIFI.get();
		}

	}

}
