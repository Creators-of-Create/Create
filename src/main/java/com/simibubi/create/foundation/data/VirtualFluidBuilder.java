package com.simibubi.create.foundation.data;

import java.util.function.BiFunction;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;

/**
 * For registering fluids with no buckets/blocks
 */
public class VirtualFluidBuilder<T extends ForgeFlowingFluid, P> extends FluidBuilder<T, P> {

	public VirtualFluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		ResourceLocation stillTexture, ResourceLocation flowingTexture,
		BiFunction<FluidAttributes.Builder, Fluid, FluidAttributes> attributesFactory,
		NonNullFunction<Properties, T> factory) {
		super(owner, parent, name, callback, stillTexture, flowingTexture, attributesFactory, factory);
		source(factory);
	}

	@Override
	public NonNullSupplier<T> asSupplier() {
		return this::getEntry;
	}

}
