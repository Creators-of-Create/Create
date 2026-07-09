package com.simibubi.create.foundation.fluid;

import com.simibubi.create.AllFluids.TintedFluidType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public final class FluidRenderProperties {
	private static final int NO_TINT = 0xffffffff;

	private FluidRenderProperties() {
	}

	public static TextureAtlasSprite getStillTexture(FluidStack stack) {
		return getModel(stack).stillMaterial()
			.sprite();
	}

	public static TextureAtlasSprite getFlowingTexture(FluidStack stack) {
		return getModel(stack).flowingMaterial()
			.sprite();
	}

	public static int getTintColor(FluidStack stack) {
		FluidType type = stack.getFluid()
			.getFluidType();
		if (type instanceof TintedFluidType tinted)
			return tinted.getTintColor(stack);
		return NO_TINT;
	}

	private static FluidModel getModel(FluidStack stack) {
		FluidState state = stack.getFluid()
			.defaultFluidState();
		return Minecraft.getInstance()
			.getModelManager()
			.getFluidStateModelSet()
			.get(state);
	}
}
