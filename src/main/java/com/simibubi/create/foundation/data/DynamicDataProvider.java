package com.simibubi.create.foundation.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.RegistryData;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DynamicDataProvider<T> implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
	public void run(HashCache cache) throws IOException {
		Path path = generator.getOutputFolder();
		DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

		dumpValues(path, cache, ops, registryData.key(), values, registryData.codec());
	}

	private void dumpValues(Path rootPath, HashCache cache, DynamicOps<JsonElement> ops, ResourceKey<? extends Registry<T>> registryKey, Map<ResourceLocation, T> values, Encoder<T> encoder) {
		for (Entry<ResourceLocation, T> entry : values.entrySet()) {
			Path path = createPath(rootPath, registryKey.location(), entry.getKey());
			dumpValue(path, cache, ops, encoder, entry.getValue());
		}
	}

	// From WorldgenRegistryDumpReport
	private void dumpValue(Path path, HashCache cache, DynamicOps<JsonElement> ops, Encoder<T> encoder, T value) {
		try {
			Optional<JsonElement> optional = encoder.encodeStart(ops, value).resultOrPartial((message) -> {
				Create.LOGGER.error("Couldn't serialize element {}: {}", path, message);
			});
			if (optional.isPresent()) {
				DataProvider.save(GSON, cache, optional.get(), path);
			}
		} catch (IOException e) {
			Create.LOGGER.error("Couldn't save element {}", path, e);
		}
	}

	private Path createPath(Path path, ResourceLocation registry, ResourceLocation value) {
		return path.resolve("data").resolve(value.getNamespace()).resolve(registry.getPath()).resolve(value.getPath() + ".json");
	}

	@Override
	public String getName() {
		return name;
	}
}
