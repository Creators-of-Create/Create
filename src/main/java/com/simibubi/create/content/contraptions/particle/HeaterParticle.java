package com.simibubi.create.content.contraptions.particle;

import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HeaterParticle extends SimpleAnimatedParticle {

	private final IAnimatedSprite animatedSprite;

	public HeaterParticle(World worldIn, float r, float g, float b, double x, double y, double z, double vx, double vy,
		double vz, IAnimatedSprite spriteSet) {
		super(worldIn, x, y, z, spriteSet, worldIn.rand.nextFloat() * .5f);

		this.animatedSprite = spriteSet;

		this.motionX = this.motionX * (double) 0.01F + vx;
		this.motionY = this.motionY * (double) 0.01F + vy;
		this.motionZ = this.motionZ * (double) 0.01F + vz;

		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;

		this.posX += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
		this.posY += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
		this.posZ += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;

		this.maxAge = (int) (8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
		this.particleScale *= 1.875F;
		this.selectSpriteWithAge(animatedSprite);

	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.PARTICLE_SHEET_LIT;
	}

	@Override
	public float getScale(float p_217561_1_) {
		float f = ((float) this.age + p_217561_1_) / (float) this.maxAge;
		return this.particleScale * (1.0F - f * f * 0.5F);
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox()
			.offset(x, y, z));
		this.resetPositionToBB();
	}

	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		float f = ((float) this.age + p_189214_1_) / (float) this.maxAge;
		f = MathHelper.clamp(f, 0.0F, 1.0F);
		int i = super.getBrightnessForRender(p_189214_1_);
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
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		if (this.age++ >= this.maxAge) {
			this.setExpired();
		} else {
			this.selectSpriteWithAge(animatedSprite);
			this.move(this.motionX, this.motionY, this.motionZ);
			this.motionX *= (double) 0.96F;
			this.motionY *= (double) 0.96F;
			this.motionZ *= (double) 0.96F;
			if (this.onGround) {
				this.motionX *= (double) 0.7F;
				this.motionZ *= (double) 0.7F;
			}
		}
	}

	public static class Factory implements IParticleFactory<HeaterParticleData> {
		private final IAnimatedSprite spriteSet;

		public Factory(IAnimatedSprite animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		@Override
		public Particle makeParticle(HeaterParticleData data, World worldIn, double x, double y, double z, double vx,
			double vy, double vz) {
			return new HeaterParticle(worldIn, data.r, data.g, data.b, x, y, z, vx, vy, vz, this.spriteSet);
		}
	}
}
