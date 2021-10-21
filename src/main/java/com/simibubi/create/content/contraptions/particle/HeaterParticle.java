package com.simibubi.create.content.contraptions.particle;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HeaterParticle extends SimpleAnimatedParticle {

	private final IAnimatedSprite animatedSprite;

	public HeaterParticle(ClientWorld worldIn, float r, float g, float b, double x, double y, double z, double vx, double vy,
						  double vz, IAnimatedSprite spriteSet) {
		super(worldIn, x, y, z, spriteSet, worldIn.random.nextFloat() * .5f);

		this.animatedSprite = spriteSet;

		this.xd = this.xd * (double) 0.01F + vx;
		this.yd = this.yd * (double) 0.01F + vy;
		this.zd = this.zd * (double) 0.01F + vz;

		this.rCol = r;
		this.gCol = g;
		this.bCol = b;

		this.x += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
		this.y += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;
		this.z += (this.random.nextFloat() - this.random.nextFloat()) * 0.05F;

		this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
		this.quadSize *= 1.875F;
		this.setSpriteFromAge(animatedSprite);

	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_LIT;
	}

	@Override
	public float getQuadSize(float p_217561_1_) {
		float f = ((float) this.age + p_217561_1_) / (float) this.lifetime;
		return this.quadSize * (1.0F - f * f * 0.5F);
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox()
			.move(x, y, z));
		this.setLocationFromBoundingbox();
	}

	@Override
	public int getLightColor(float p_189214_1_) {
		float f = ((float) this.age + p_189214_1_) / (float) this.lifetime;
		f = MathHelper.clamp(f, 0.0F, 1.0F);
		int i = super.getLightColor(p_189214_1_);
		int j = i & 255;
		int k = i >> 16 & 255;
		j = j + (int) (f * 15.0F * 16.0F);
		if (j > 240) {
			j = 240;
		}

		return j | k << 16;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.setSpriteFromAge(animatedSprite);
			this.move(this.xd, this.yd, this.zd);
			this.xd *= (double) 0.96F;
			this.yd *= (double) 0.96F;
			this.zd *= (double) 0.96F;
			if (this.onGround) {
				this.xd *= (double) 0.7F;
				this.zd *= (double) 0.7F;
			}
		}
	}

	public static class Factory implements IParticleFactory<HeaterParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		@Override
		public Particle createParticle(HeaterParticleData data, ClientWorld worldIn, double x, double y, double z, double vx,
			double vy, double vz) {
			return new HeaterParticle(worldIn, data.r, data.g, data.b, x, y, z, vx, vy, vz, this.spriteSet);
		}
	}
}
