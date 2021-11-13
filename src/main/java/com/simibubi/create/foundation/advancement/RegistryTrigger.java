package com.simibubi.create.foundation.advancement;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegistryTrigger<T> extends StringSerializableTrigger<T> {
	private final Registry<T> registry;

	public RegistryTrigger(String id, Registry<T> registry) {
		super(id);
		this.registry = registry;
	}

	@Nullable
	@Override
	protected T getValue(String key) {
		return registry.get(new ResourceLocation(key));
	}

	@Nullable
	@Override
	protected String getKey(T value) {
		ResourceLocation key = registry.getKey(value);
		return key == null ? null : key.toString();
	}
}
