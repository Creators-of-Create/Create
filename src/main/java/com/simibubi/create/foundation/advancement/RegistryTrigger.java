package com.simibubi.create.foundation.advancement;

import com.google.common.collect.Sets;
import com.google.gson.*;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RegistryTrigger<T extends IForgeRegistryEntry<T>> extends CriterionTriggerBase<RegistryTrigger.Instance<T>> {
	private final Class<T> registryType;

	public RegistryTrigger(String id, Class<T> registryType) {
		super(id);
		this.registryType = registryType;
	}

	@SafeVarargs
	public final Instance<T> forEntries(@Nullable T... entries) {
		return new Instance<>(getId(), entries == null ? null : Sets.newHashSet(entries));
	}

	public void trigger(ServerPlayerEntity player, T registryEntry) {
		trigger(player, Collections.singletonList(() -> registryEntry));
	}

	public ITriggerable constructTriggerFor(T entry) {
		BiConsumer<ServerPlayerEntity, T> trigger = this::trigger;
		return player -> trigger.accept(player, entry);
	}

	@Override
	public Instance<T> deserializeInstance(JsonObject json, JsonDeserializationContext context) {
		if (json.has("accepted_entries")) {
			JsonArray elements = json.getAsJsonArray("accepted_entries");
			IForgeRegistry<T> registry = RegistryManager.ACTIVE.getRegistry(registryType);

			return new Instance<>(getId(),
				StreamSupport.stream(elements.spliterator(), false).map(JsonElement::getAsString).map(ResourceLocation::new)
					.map(rl -> {
						T entry = registry.getValue(rl);
						if (entry == null)
							throw new JsonSyntaxException("Unknown registry entry '" + rl + "'");
						return entry;
					}).collect(Collectors.toSet()));
		}

		return forEntries((T) null);
	}

	public static class Instance<T extends IForgeRegistryEntry<T>> extends CriterionTriggerBase.Instance {

		@Nullable
		private final Set<T> entries;

		public Instance(ResourceLocation id, @Nullable Set<T> registryEntries) {
			super(id);
			entries = registryEntries;
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			if (entries == null || suppliers == null || suppliers.isEmpty())
				return false;
			return entries.contains(suppliers.get(0).get());
		}

		@Override
		public JsonElement serialize() {
			JsonObject jsonobject = new JsonObject();
			JsonArray elements = new JsonArray();

			if (entries == null) {
				jsonobject.add("accepted_entries", elements);
				return jsonobject;
			}

			for (T entry : entries) {
				if (entry == null)
					continue;
				ResourceLocation key = RegistryManager.ACTIVE.getRegistry(entry.getRegistryType()).getKey(entry);
				if (key != null)
					elements.add(key.toString());
			}

			jsonobject.add("accepted_entries", elements);
			return jsonobject;
		}
	}
}
