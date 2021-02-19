package com.simibubi.create.foundation.advancement;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegistryTrigger<T extends IForgeRegistryEntry<T>> extends StringSerializableTrigger<T> {
	private final IForgeRegistry<T> registry;

	public RegistryTrigger(String id, Class<T> registryType) {
		super(id);
		this.registry = RegistryManager.ACTIVE.getRegistry(registryType);
	}

	@Nullable
	@Override
	protected T getValue(String key) {

		return registry.getValue(new ResourceLocation(key));
	}

	@Nullable
	@Override
	protected String getKey(T value) {
		ResourceLocation key = registry.getKey(value);
		return key == null ? null : key.toString();
	}
}
