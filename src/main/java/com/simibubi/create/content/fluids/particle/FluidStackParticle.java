package com.simibubi.create.content.fluids.particle;

import com.simibubi.create.AllParticleTypes;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackParticle extends TextureSheetParticle {
	private final float uo;
	private final float vo;
	private FluidStack fluid;
	private IClientFluidTypeExtensions clientFluid;

	public static FluidStackParticle create(ParticleType<FluidParticleData> type, ClientLevel world, FluidStack fluid,
		double x, double y, double z, double vx, double vy, double vz) {
		if (type == AllParticleTypes.BASIN_FLUID.get())
			return new BasinFluidParticle(world, fluid, x, y, z, vx, vy, vz);
		return new FluidStackParticle(world, fluid, x, y, z, vx, vy, vz);
	}

	public FluidStackParticle(ClientLevel world, FluidStack fluid, double x, double y, double z, double vx, double vy,
		double vz) {
		super(world, x, y, z, vx, vy, vz);

		clientFluid = IClientFluidTypeExtensions.of(fluid.getFluid());

		this.fluid = fluid;
		this.setSprite(Minecraft.getInstance()
			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
			.apply(clientFluid.getStillTexture(fluid)));

		this.gravity = 1.0F;
		this.rCol = 0.8F;
		this.gCol = 0.8F;
		this.bCol = 0.8F;
		this.multiplyColor(clientFluid.getTintColor(fluid));

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
			.getFluidType()
			.getLightLevel(fluid));
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

		Color color = new Color(clientFluid.getTintColor(fluid));
		level.addParticle(ParticleTypes.ENTITY_EFFECT, x, y, z, color.getRedAsFloat(), color.getGreenAsFloat(),
			color.getBlueAsFloat());
	}

	protected boolean canEvaporate() {
		return fluid.getFluid() instanceof PotionFluid;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.TERRAIN_SHEET;
	}

}
