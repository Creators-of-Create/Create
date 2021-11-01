package com.simibubi.create.foundation.advancement;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegistryTrigger<T extends IForgeRegistryEntry<T>> extends StringSerializableTrigger<T> {
	private final IForgeRegistry<T> registry;

	public RegistryTrigger(String id, IForgeRegistry<T> registry) {
		super(id);
		this.registry = registry;
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
