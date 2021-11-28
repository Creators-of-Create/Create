package com.simibubi.create.foundation.worldgen;

import java.util.function.BiPredicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome.BiomeCategory;

public interface BiomeFilter extends BiPredicate<ResourceLocation, BiomeCategory> {

}
