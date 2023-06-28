package com.simibubi.create.foundation.advancement;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.server.level.ServerPlayer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class StringSerializableTrigger<T> extends CriterionTriggerBase<StringSerializableTrigger.Instance<T>> {

	protected String getJsonKey() {
		return "accepted_entries";
	}

	protected StringSerializableTrigger(String id) {
		super(id);
	}

	@SafeVarargs
	public final Instance<T> forEntries(@Nullable T... entries) {
		return new Instance<>(this, entries == null ? null : createLinkedHashSet(entries));
	}

	public void trigger(ServerPlayer player, @Nullable T registryEntry) {
		trigger(player, Collections.singletonList(() -> registryEntry));
	}

	public ITriggerable constructTriggerFor(@Nullable T entry) {
		return player -> trigger(player, entry);
	}

	@Override
	public Instance<T> createInstance(JsonObject json, DeserializationContext context) {
		if (json.has(getJsonKey())) {
			JsonArray elements = json.getAsJsonArray(getJsonKey());
			return new Instance<>(this, StreamSupport.stream(elements.spliterator(), false)
				.map(JsonElement::getAsString)
				.map(key -> {
					T entry = getValue(key);
					if (entry == null)
						throw new JsonSyntaxException("Unknown entry '" + key + "'");
					return entry;
				})
				.collect(Collectors.toSet()));
		}
		return new Instance<>(this, null);
	}

	@Nullable
	protected abstract T getValue(String key);

	@Nullable
	protected abstract String getKey(T value);

	private static <T> LinkedHashSet<T> createLinkedHashSet(T[] elements) {
		LinkedHashSet<T> set = new LinkedHashSet<>(elements.length);
		Collections.addAll(set, elements);
		return set;
	}

	public static class Instance<T> extends CriterionTriggerBase.Instance {

		@Nullable
		private final Set<T> entries;
		private final StringSerializableTrigger<T> trigger;

		public Instance(StringSerializableTrigger<T> trigger, @Nullable Set<T> entries) {
			super(trigger.getId(), ContextAwarePredicate.ANY);
			this.trigger = trigger;
			this.entries = entries;
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			if (entries == null || suppliers == null || suppliers.isEmpty())
				return false;
			return entries.contains(suppliers.get(0)
				.get());
		}

		@Override
		public JsonObject serializeToJson(SerializationContext p_230240_1_) {
			JsonObject jsonobject = super.serializeToJson(p_230240_1_);
			JsonArray elements = new JsonArray();

			if (entries == null) {
				jsonobject.add(trigger.getJsonKey(), elements);
				return jsonobject;
			}

			for (T entry : entries) {
				if (entry == null)
					continue;
				String key = trigger.getKey(entry);
				if (key != null)
					elements.add(key);
			}

			jsonobject.add(trigger.getJsonKey(), elements);
			return jsonobject;
		}
	}
}
