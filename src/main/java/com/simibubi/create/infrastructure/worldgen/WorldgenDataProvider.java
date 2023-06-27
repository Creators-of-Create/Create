package com.simibubi.create.infrastructure.worldgen;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.Create;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.RegistrySetBuilder.RegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class WorldgenDataProvider extends DatapackBuiltinEntriesProvider {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(Registries.CONFIGURED_FEATURE, (RegistryBootstrap) AllConfiguredFeatures::bootstrap)
			.add(Registries.PLACED_FEATURE, AllPlacedFeatures::bootstrap)
			.add(ForgeRegistries.Keys.BIOME_MODIFIERS, AllBiomeModifiers::bootstrap);

	public WorldgenDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, BUILDER, Set.of(Create.ID));
	}

	public static DataProvider.Factory<WorldgenDataProvider> makeFactory(CompletableFuture<HolderLookup.Provider> registries) {
		return output -> new WorldgenDataProvider(output, registries);
	}

	@Override
	public String getName() {
		return "Create's Worldgen Data";
	}
}
