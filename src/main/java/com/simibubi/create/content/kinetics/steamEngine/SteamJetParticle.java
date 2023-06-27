package com.simibubi.create.content.kinetics.steamEngine;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SteamJetParticle extends SimpleAnimatedParticle {

	private float yaw, pitch;

	protected SteamJetParticle(ClientLevel world, SteamJetParticleData data, double x, double y, double z, double dx,
		double dy, double dz, SpriteSet sprite) {
		super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
		xd = 0;
		yd = 0;
		zd = 0;
		gravity = 0;
		quadSize = .375f;
		setLifetime(21);
		setPos(x, y, z);
		roll = oRoll = world.random.nextFloat() * Mth.PI;
		yaw = (float) Mth.atan2(dx, dz) - Mth.PI;
		pitch = (float) Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) - Mth.PI / 2;
		this.setSpriteFromAge(sprite);
	}

	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
		Vec3 vec3 = pRenderInfo.getPosition();
		float f = (float) (x - vec3.x);
		float f1 = (float) (y - vec3.y);
		float f2 = (float) (z - vec3.z);
		float f3 = Mth.lerp(pPartialTicks, this.oRoll, this.roll);
		float f7 = this.getU0();
		float f8 = this.getU1();
		float f5 = this.getV0();
		float f6 = this.getV1();
		float f4 = this.getQuadSize(pPartialTicks);

		for (int i = 0; i < 4; i++) {
			Quaternionf quaternion = Axis.YP.rotation(yaw);
			quaternion.mul(Axis.XP.rotation(pitch));
			quaternion.mul(Axis.YP.rotation(f3 + Mth.PI / 2 * i + roll));
			Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
			vector3f1.rotate(quaternion);

			Vector3f[] avector3f = new Vector3f[] { new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F) };

			for (int j = 0; j < 4; ++j) {
				Vector3f vector3f = avector3f[j];
				vector3f.add(0, 1, 0);
				vector3f.rotate(quaternion);
				vector3f.mul(f4);
				vector3f.add(f, f1, f2);
			}

			int j = this.getLightColor(pPartialTicks);
			pBuffer.vertex((double) avector3f[0].x(), (double) avector3f[0].y(), (double) avector3f[0].z())
				.uv(f8, f6)
				.color(this.rCol, this.gCol, this.bCol, this.alpha)
				.uv2(j)
				.endVertex();
			pBuffer.vertex((double) avector3f[1].x(), (double) avector3f[1].y(), (double) avector3f[1].z())
				.uv(f8, f5)
				.color(this.rCol, this.gCol, this.bCol, this.alpha)
				.uv2(j)
				.endVertex();
			pBuffer.vertex((double) avector3f[2].x(), (double) avector3f[2].y(), (double) avector3f[2].z())
				.uv(f7, f5)
				.color(this.rCol, this.gCol, this.bCol, this.alpha)
				.uv2(j)
				.endVertex();
			pBuffer.vertex((double) avector3f[3].x(), (double) avector3f[3].y(), (double) avector3f[3].z())
				.uv(f7, f6)
				.color(this.rCol, this.gCol, this.bCol, this.alpha)
				.uv2(j)
				.endVertex();

		}
	}

	@Override
	public int getLightColor(float partialTick) {
		BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
		return this.level.isLoaded(blockpos) ? LevelRenderer.getLightColor(level, blockpos) : 0;
	}

	public static class Factory implements ParticleProvider<SteamJetParticleData> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public Particle createParticle(SteamJetParticleData data, ClientLevel worldIn, double x, double y, double z,
			double xSpeed, double ySpeed, double zSpeed) {
			return new SteamJetParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

}
