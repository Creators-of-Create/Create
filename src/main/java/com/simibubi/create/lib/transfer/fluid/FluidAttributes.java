package com.simibubi.create.lib.transfer.fluid;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluid;

public class FluidAttributes {

	private String translationKey;

	private final ResourceLocation stillTexture;
	private final ResourceLocation flowingTexture;

	@Nullable
	private final ResourceLocation overlayTexture;

	private final SoundEvent fillSound;
	private final SoundEvent emptySound;

	/**
	 * The light level emitted by this fluid.
	 *
	 * Default value is 0, as most fluids do not actively emit light.
	 */
	private final int luminosity;

	/**
	 * Density of the fluid - completely arbitrary; negative density indicates that the fluid is
	 * lighter than air.
	 *
	 * Default value is approximately the real-life density of water in kg/m^3.
	 */
	private final int density;

	/**
	 * Temperature of the fluid - completely arbitrary; higher temperature indicates that the fluid is
	 * hotter than air.
	 *
	 * Default value is approximately the real-life room temperature of water in degrees Kelvin.
	 */
	private final int temperature;

	/**
	 * Viscosity ("thickness") of the fluid - completely arbitrary; negative values are not
	 * permissible.
	 *
	 * Default value is approximately the real-life density of water in m/s^2 (x10^-3).
	 *
	 * Higher viscosity means that a fluid flows more slowly, like molasses.
	 * Lower viscosity means that a fluid flows more quickly, like helium.
	 *
	 */
	private final int viscosity;

	/**
	 * This indicates if the fluid is gaseous.
	 *
	 * Generally this is associated with negative density fluids.
	 */
	private final boolean isGaseous;

	/**
	 * The rarity of the fluid.
	 *
	 * Used primarily in tool tips.
	 */
	private final Rarity rarity;

	/**
	 * Color used by universal bucket and the ModelFluid baked model.
	 * Note that this int includes the alpha so converting this to RGB with alpha would be
	 *   float r = ((color >> 16) & 0xFF) / 255f; // red
	 *   float g = ((color >> 8) & 0xFF) / 255f; // green
	 *   float b = ((color >> 0) & 0xFF) / 255f; // blue
	 *   float a = ((color >> 24) & 0xFF) / 255f; // alpha
	 */
	private final int color;

	protected FluidAttributes(Builder builder, Fluid fluid)
	{
		this.translationKey = builder.translationKey != null ? builder.translationKey :  Util.makeDescriptionId("fluid", Registry.FLUID.getKey(fluid));
		this.stillTexture = builder.stillTexture;
		this.flowingTexture = builder.flowingTexture;
		this.overlayTexture = builder.overlayTexture;
		this.color = builder.color;
		this.fillSound = builder.fillSound;
		this.emptySound = builder.emptySound;
		this.luminosity = builder.luminosity;
		this.temperature = builder.temperature;
		this.viscosity = builder.viscosity;
		this.density = builder.density;
		this.isGaseous = builder.isGaseous;
		this.rarity = builder.rarity;
	}

	/**
	 * Returns the localized name of this fluid.
	 */
	public Component getDisplayName(FluidStack stack)
	{
		return new TranslatableComponent(getTranslationKey(stack));
	}

	/**
	 * A FluidStack sensitive version of getTranslationKey
	 */
	public String getTranslationKey(FluidStack stack)
	{
		return this.translationKey;
	}

	public int getColor()
	{
		return color;
	}

	public int getColor(FluidStack stack){ return getColor(); }

	public static class Builder
	{
		private final ResourceLocation stillTexture;
		private final ResourceLocation flowingTexture;
		private ResourceLocation overlayTexture;
		private int color = 0xFFFFFFFF;
		private String translationKey;
		private SoundEvent fillSound;
		private SoundEvent emptySound;
		private int luminosity = 0;
		private int density = 1000;
		private int temperature = 300;
		private int viscosity = 1000;
		private boolean isGaseous;
		private Rarity rarity = Rarity.COMMON;
		private BiFunction<Builder,Fluid,FluidAttributes> factory;

		protected Builder(ResourceLocation stillTexture, ResourceLocation flowingTexture, BiFunction<Builder,Fluid,FluidAttributes> factory) {
			this.factory = factory;
			this.stillTexture = stillTexture;
			this.flowingTexture = flowingTexture;
		}

		public final Builder translationKey(String translationKey)
		{
			this.translationKey = translationKey;
			return this;
		}

		public final Builder color(int color)
		{
			this.color = color;
			return this;
		}

		public final Builder overlay(ResourceLocation texture)
		{
			overlayTexture = texture;
			return this;
		}

		public final Builder luminosity(int luminosity)
		{
			this.luminosity = luminosity;
			return this;
		}

		public final Builder density(int density)
		{
			this.density = density;
			return this;
		}

		public final Builder temperature(int temperature)
		{
			this.temperature = temperature;
			return this;
		}

		public final Builder viscosity(int viscosity)
		{
			this.viscosity = viscosity;
			return this;
		}

		public final Builder gaseous()
		{
			isGaseous = true;
			return this;
		}

		public final Builder rarity(Rarity rarity)
		{
			this.rarity = rarity;
			return this;
		}

		public final Builder sound(SoundEvent sound)
		{
			this.fillSound = this.emptySound = sound;
			return this;
		}

		public final Builder sound(SoundEvent fillSound, SoundEvent emptySound)
		{
			this.fillSound = fillSound;
			this.emptySound = emptySound;
			return this;
		}

		public FluidAttributes build(Fluid fluid)
		{
			return factory.apply(this, fluid);
		}
	}
}
