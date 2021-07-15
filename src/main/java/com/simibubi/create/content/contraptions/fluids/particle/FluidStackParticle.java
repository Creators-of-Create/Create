package com.simibubi.create.content.contraptions.fluids.particle;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackParticle extends SpriteTexturedParticle {
	private final float uo;
	private final float vo;
	private FluidStack fluid;

	public static FluidStackParticle create(ParticleType<FluidParticleData> type, ClientWorld world, FluidStack fluid, double x,
		double y, double z, double vx, double vy, double vz) {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return new BasinFluidParticle(world, fluid, x, y, z, vx, vy, vz);
		return new FluidStackParticle(world, fluid, x, y, z, vx, vy, vz);
	}

	public FluidStackParticle(ClientWorld world, FluidStack fluid, double x, double y, double z, double vx, double vy,
		double vz) {
		super(world, x, y, z, vx, vy, vz);
		this.fluid = fluid;
		this.setSprite(Minecraft.getInstance()
			.getTextureAtlas(PlayerContainer.BLOCK_ATLAS)
			.apply(fluid.getFluid()
				.getAttributes()
				.getStillTexture()));

		this.gravity = 1.0F;
		this.rCol = 0.8F;
		this.gCol = 0.8F;
		this.bCol = 0.8F;
		this.multiplyColor(fluid.getFluid()
			.getAttributes()
			.getColor(fluid));
		
		this.xd = vx;
		this.yd = vy;
		this.zd = vz;

		this.quadSize /= 2.0F;
		this.uo = this.random.nextFloat() * 3.0F;
		this.vo = this.random.nextFloat() * 3.0F;
	}

	@Override
	protected int getLightColor(float p_189214_1_) {
		int brightnessForRender = super.getLightColor(p_189214_1_);
		int skyLight = brightnessForRender >> 20;
		int blockLight = (brightnessForRender >> 4) & 0xf;
		blockLight = Math.max(blockLight, fluid.getFluid()
			.getAttributes()
			.getLuminosity(fluid));
		return (skyLight << 20) | (blockLight << 4);
	}

	protected void multiplyColor(int color) {
		this.rCol *= (float) (color >> 16 & 255) / 255.0F;
		this.gCol *= (float) (color >> 8 & 255) / 255.0F;
		this.bCol *= (float) (color & 255) / 255.0F;
	}

	protected float getU0() {
		return this.sprite.getU((double) ((this.uo + 1.0F) / 4.0F * 16.0F));
	}

	protected float getU1() {
		return this.sprite.getU((double) (this.uo / 4.0F * 16.0F));
	}

	protected float getV0() {
		return this.sprite.getV((double) (this.vo / 4.0F * 16.0F));
	}

	protected float getV1() {
		return this.sprite.getV((double) ((this.vo + 1.0F) / 4.0F * 16.0F));
	}

	@Override
	public void tick() {
		super.tick();
		if (!canEvaporate())
			return;
		if (onGround)
			remove();
		if (!removed)
			return;
		if (!onGround && level.random.nextFloat() < 1 / 8f)
			return;

		Vector3d rgb = ColorHelper.getRGB(fluid.getFluid()
			.getAttributes()
			.getColor(fluid));
		level.addParticle(ParticleTypes.ENTITY_EFFECT, x, y, z, rgb.x, rgb.y, rgb.z);
	}
	
	protected boolean canEvaporate() {
		return fluid.getFluid() instanceof PotionFluid;
	}

	@Override
	public IParticleRenderType getRenderType() {
		return IParticleRenderType.TERRAIN_SHEET;
	}

}
