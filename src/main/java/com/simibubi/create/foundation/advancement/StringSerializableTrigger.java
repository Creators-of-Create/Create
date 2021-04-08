package com.simibubi.create.foundation.advancement;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;

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
		return new Instance<>(this, entries == null ? null : Sets.newHashSet(entries));
	}

	public void trigger(ServerPlayerEntity player, @Nullable T registryEntry) {
		trigger(player, Collections.singletonList(() -> registryEntry));
	}

	public ITriggerable constructTriggerFor(@Nullable T entry) {
		return player -> trigger(player, entry);
	}

	@Override
	public Instance<T> conditionsFromJson(JsonObject json, ConditionArrayParser context) {
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

	public static class Instance<T> extends CriterionTriggerBase.Instance {

		@Nullable
		private final Set<T> entries;
		private final StringSerializableTrigger<T> trigger;

		public Instance(StringSerializableTrigger<T> trigger, @Nullable Set<T> entries) {
			super(trigger.getId(), EntityPredicate.AndPredicate.EMPTY);
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
		public JsonObject toJson(ConditionArraySerializer p_230240_1_) {
			JsonObject jsonobject = super.toJson(p_230240_1_);
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
