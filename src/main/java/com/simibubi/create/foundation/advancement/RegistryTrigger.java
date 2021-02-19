package com.simibubi.create.foundation.advancement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegistryTrigger<T extends IForgeRegistryEntry<T>> extends CriterionTriggerBase<RegistryTrigger.Instance<T>> {
	private final Class<T> registryType;

	public RegistryTrigger(String id, Class<T> registryType) {
		super(id);
		this.registryType = registryType;
	}

	public Instance<T> forEntry(@Nullable T registryEntry) {
		return new Instance<>(getId(), registryEntry);
	}

	public void trigger(ServerPlayerEntity player, T registryEntry) {
		trigger(player, Collections.singletonList(() -> registryEntry));
	}

	@Override
	public Instance<T> deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		T entry = null;
		if (json.has("registry_entry")) {
			ResourceLocation entryLocation = new ResourceLocation(JSONUtils.getString(json, "registry_entry"));
			entry = RegistryManager.ACTIVE.getRegistry(registryType).getValue(entryLocation);

			if (entry == null)
				throw new JsonSyntaxException("Unknown registry entry '" + entryLocation + "'");
		}

		return forEntry(entry);
	}


	public static class Instance<T extends IForgeRegistryEntry<T>> extends CriterionTriggerBase.Instance {

		@Nullable
		private final T entry;

		public Instance(ResourceLocation id, @Nullable T registryEntry) {
			super(id);
			entry = registryEntry;
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			if (entry == null || suppliers == null || suppliers.isEmpty())
				return false;
			return entry.equals(suppliers.get(0).get());
		}

		@Override
		public JsonElement serialize() {
			JsonObject jsonobject = new JsonObject();
			if (entry == null)
				return jsonobject;

			ResourceLocation key = RegistryManager.ACTIVE.getRegistry(entry.getRegistryType()).getKey(entry);
			if (key != null) {
				jsonobject.addProperty("registry_entry", key.toString());
			}
			return jsonobject;
		}
	}
}
