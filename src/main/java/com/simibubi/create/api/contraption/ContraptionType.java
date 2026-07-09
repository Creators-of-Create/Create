package com.simibubi.create.api.contraption;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllContraptionTypes;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

public final class ContraptionType {
	public final Supplier<? extends Contraption> factory;
	public final Holder.Reference<ContraptionType> holder = CreateBuiltInRegistries.CONTRAPTION_TYPE.createIntrusiveHolder(this);

	public ContraptionType(Supplier<? extends Contraption> factory) {
		this.factory = factory;
	}

	public boolean is(TagKey<ContraptionType> tag) {
		return this.holder.is(tag);
	}

	/**
	 * Lookup the ContraptionType with the given ID, and create a new Contraption from it if present.
	 * If it doesn't exist, returns null.
	 */
	@Nullable
	public static Contraption fromType(String typeId) {
		ContraptionType legacy = AllContraptionTypes.BY_LEGACY_NAME.get(typeId);
		if (legacy != null) {
			return legacy.factory.get();
		}

		Identifier id = Identifier.tryParse(typeId);
		ContraptionType type = CreateBuiltInRegistries.CONTRAPTION_TYPE.get(id)
			.map(Holder::value)
			.orElse(null);
		return type == null ? null : type.factory.get();
	}
}
