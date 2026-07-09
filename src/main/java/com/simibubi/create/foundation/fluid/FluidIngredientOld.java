package com.simibubi.create.foundation.fluid;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

import com.mojang.serialization.Codec;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

@ScheduledForRemoval(inVersion = "1.21.1+ Port")
@Deprecated(since = "6.0.7", forRemoval = true)
public class FluidIngredientOld {
	public static final Codec<SizedFluidIngredient> CODEC = SizedFluidIngredient.CODEC;
}
