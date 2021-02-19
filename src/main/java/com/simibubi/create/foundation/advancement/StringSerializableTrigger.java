package com.simibubi.create.foundation.advancement;

import com.google.common.collect.Sets;
import com.google.gson.*;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class StringSerializableTrigger<T> extends CriterionTriggerBase<StringSerializableTrigger.Instance<T>> {
	public StringSerializableTrigger(String id) {
		super(id);
	}

	@SafeVarargs
	public final Instance<T> forEntries(@Nullable T... entries) {
		return new Instance<>(this, entries == null ? null : Sets.newHashSet(entries));
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
			return new Instance<>(this,
				StreamSupport.stream(elements.spliterator(), false).map(JsonElement::getAsString)
					.map(rl -> {
						T entry = getValue(rl);
						if (entry == null)
							throw new JsonSyntaxException("Unknown entry '" + rl + "'");
						return entry;
					}).collect(Collectors.toSet()));
		}

		return forEntries((T) null);
	}

	@Nullable
	abstract protected T getValue(String key);

	@Nullable
	abstract protected String getKey(T value);

	public static class Instance<T> extends CriterionTriggerBase.Instance {

		@Nullable
		private final Set<T> entries;
		private final StringSerializableTrigger<T> trigger;

		public Instance(StringSerializableTrigger<T> trigger, @Nullable Set<T> entries) {
			super(trigger.getId());
			this.trigger = trigger;
			this.entries = entries;
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
				String key = trigger.getKey(entry);
				if (key != null)
					elements.add(key);
			}

			jsonobject.add("accepted_entries", elements);
			return jsonobject;
		}
	}
}
