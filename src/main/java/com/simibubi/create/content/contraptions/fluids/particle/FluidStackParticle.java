package com.simibubi.create.content.contraptions.fluids.particle;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackParticle extends SpriteTexturedParticle {
	private final float field_217587_G;
	private final float field_217588_H;
	private FluidStack fluid;

	public static FluidStackParticle create(ParticleType<FluidParticleData> type, World world, FluidStack fluid, double x,
		double y, double z, double vx, double vy, double vz) {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return new BasinFluidParticle(world, fluid, x, y, z, vx, vy, vz);
		return new FluidStackParticle(world, fluid, x, y, z, vx, vy, vz);
	}

	public FluidStackParticle(World world, FluidStack fluid, double x, double y, double z, double vx, double vy,
		double vz) {
		super(world, x, y, z, vx, vy, vz);
		this.fluid = fluid;
		this.setSprite(Minecraft.getInstance()
			.getSpriteAtlas(PlayerContainer.BLOCK_ATLAS_TEXTURE)
			.apply(fluid.getFluid()
				.getAttributes()
				.getStillTexture()));

		this.particleGravity = 1.0F;
		this.particleRed = 0.8F;
		this.particleGreen = 0.8F;
		this.particleBlue = 0.8F;
		this.multiplyColor(fluid.getFluid()
			.getAttributes()
			.getColor(fluid));
		
		this.motionX = vx;
		this.motionY = vy;
		this.motionZ = vz;

		this.particleScale /= 2.0F;
		this.field_217587_G = this.rand.nextFloat() * 3.0F;
		this.field_217588_H = this.rand.nextFloat() * 3.0F;
	}

	@Override
	protected int getBrightnessForRender(float p_189214_1_) {
		int brightnessForRender = super.getBrightnessForRender(p_189214_1_);
		int skyLight = brightnessForRender >> 20;
		int blockLight = (brightnessForRender >> 4) & 0xf;
		blockLight = Math.max(blockLight, fluid.getFluid()
			.getAttributes()
			.getLuminosity(fluid));
		return (skyLight << 20) | (blockLight << 4);
	}

	protected void multiplyColor(int color) {
		this.particleRed *= (float) (color >> 16 & 255) / 255.0F;
		this.particleGreen *= (float) (color >> 8 & 255) / 255.0F;
		this.particleBlue *= (float) (color & 255) / 255.0F;
	}

	protected float getMinU() {
		return this.sprite.getInterpolatedU((double) ((this.field_217587_G + 1.0F) / 4.0F * 16.0F));
	}

	protected float getMaxU() {
		return this.sprite.getInterpolatedU((double) (this.field_217587_G / 4.0F * 16.0F));
	}

	protected float getMinV() {
		return this.sprite.getInterpolatedV((double) (this.field_217588_H / 4.0F * 16.0F));
	}

	protected float getMaxV() {
		return this.sprite.getInterpolatedV((double) ((this.field_217588_H + 1.0F) / 4.0F * 16.0F));
	}

	@Override
	public void tick() {
		super.tick();
		if (!canEvaporate())
			return;
		if (onGround)
			setExpired();
		if (!isExpired)
			return;
		if (!onGround && world.rand.nextFloat() < 1 / 8f)
			return;

		Vec3d rgb = ColorHelper.getRGB(fluid.getFluid()
			.getAttributes()
			.getColor(fluid));
		world.addParticle(ParticleTypes.ENTITY_EFFECT, posX, posY, posZ, rgb.x, rgb.y, rgb.z);
	}
	
	protected boolean canEvaporate() {
		return fluid.getFluid() instanceof PotionFluid;
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.TERRAIN_SHEET;
	}

}
