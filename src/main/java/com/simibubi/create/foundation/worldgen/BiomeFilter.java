package com.simibubi.create.foundation.worldgen;

import java.util.function.Function;

import net.minecraft.core.HolderSet;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;

public interface BiomeFilter extends Function<RegistryOps<?>, HolderSet<Biome>> {

}
