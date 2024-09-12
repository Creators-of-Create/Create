package com.simibubi.create.foundation.data;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fluids.ForgeFlowingFluid.Properties;

/**
 * For registering fluids with no buckets/blocks
 */
public class VirtualFluidBuilder<T extends ForgeFlowingFluid, P> extends FluidBuilder<T, P> {

	public VirtualFluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
		ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory,
		NonNullFunction<Properties, T> sourceFactory,
	    NonNullFunction<Properties, T> flowingFactory
   ) {
		super(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, flowingFactory);
		source(sourceFactory);
	}

	@Override
	public NonNullSupplier<T> asSupplier() {
		return this::getEntry;
	}

}
