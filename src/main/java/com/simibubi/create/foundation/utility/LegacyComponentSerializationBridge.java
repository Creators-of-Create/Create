package com.simibubi.create.foundation.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public final class LegacyComponentSerializationBridge {
	private LegacyComponentSerializationBridge() {}

	public static String toJson(Component component, HolderLookup.Provider registries) {
		return ComponentSerialization.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), component)
			.result()
			.map(JsonElement::toString)
			.orElseGet(() -> Component.empty()
				.getString());
	}

	public static Component fromJson(String json, HolderLookup.Provider registries) {
		if (json == null || json.isBlank())
			return Component.empty();
		try {
			return ComponentSerialization.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE),
					JsonParser.parseString(json))
				.result()
				.orElseGet(() -> Component.literal(json));
		} catch (RuntimeException e) {
			return Component.literal(json);
		}
	}
}
