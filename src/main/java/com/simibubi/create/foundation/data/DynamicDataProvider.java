package com.simibubi.create.foundation.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.RegistryData;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;

public class DynamicDataProvider<T> implements DataProvider {
	private final DataGenerator generator;
	private final String name;
	private final RegistryAccess registryAccess;
	private final RegistryAccess.RegistryData<T> registryData;
	private final Map<ResourceLocation, T> values;

	public DynamicDataProvider(DataGenerator generator, String name, RegistryAccess registryAccess, RegistryAccess.RegistryData<T> registryData, Map<ResourceLocation, T> values) {
		this.generator = generator;
		this.name = name;
		this.registryAccess = registryAccess;
		this.registryData = registryData;
		this.values = values;
	}

	@Nullable
	public static <T> DynamicDataProvider<T> create(DataGenerator generator, String name, RegistryAccess registryAccess, ResourceKey<? extends Registry<T>> registryKey, Map<ResourceLocation, T> values) {
		@SuppressWarnings("unchecked")
		RegistryAccess.RegistryData<T> registryData = (RegistryData<T>) RegistryAccess.REGISTRIES.get(registryKey);
		if (registryData == null) {
			return null;
		}
		return new DynamicDataProvider<>(generator, name, registryAccess, registryData, values);
	}

	@Override
	public void run(CachedOutput cache) throws IOException {
		Path path = generator.getOutputFolder();
		DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

		dumpValues(path, cache, ops, registryData.key(), values, registryData.codec());
	}

	private void dumpValues(Path rootPath, CachedOutput cache, DynamicOps<JsonElement> ops, ResourceKey<? extends Registry<T>> registryKey, Map<ResourceLocation, T> values, Encoder<T> encoder) {
		DataGenerator.PathProvider pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, ForgeHooks.prefixNamespace(registryKey.location()));

		for (Entry<ResourceLocation, T> entry : values.entrySet()) {
			dumpValue(pathProvider.json(entry.getKey()), cache, ops, encoder, entry.getValue());
		}
	}

	// From WorldgenRegistryDumpReport
	private void dumpValue(Path path, CachedOutput cache, DynamicOps<JsonElement> ops, Encoder<T> encoder, T value) {
		try {
			Optional<JsonElement> optional = encoder.encodeStart(ops, value).resultOrPartial((message) -> {
				Create.LOGGER.error("Couldn't serialize element {}: {}", path, message);
			});
			if (optional.isPresent()) {
				DataProvider.saveStable(cache, optional.get(), path);
			}
		} catch (IOException e) {
			Create.LOGGER.error("Couldn't save element {}", path, e);
		}
	}

	@Override
	public String getName() {
		return name;
	}
}
