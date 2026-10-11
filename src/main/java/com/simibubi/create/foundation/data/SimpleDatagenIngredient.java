package com.simibubi.create.foundation.data;

import java.util.Collection;
import java.util.List;

import net.minecraft.world.item.crafting.Ingredient;

import net.minecraft.world.item.crafting.Ingredient.Value;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.data.recipe.DatagenMod;
import com.simibubi.create.foundation.data.recipe.Mods;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Internal
public record SimpleDatagenIngredient(DatagenMod mod, String id) implements Ingredient.Value {
	public static final MapCodec<SimpleDatagenIngredient> MAP_CODEC = RecordCodecBuilder.mapCodec((instance) ->
		instance.group(ResourceLocation.CODEC.fieldOf("item").forGetter((i) -> i.mod.asResource(i.id)))
			.apply(instance, (location) -> {
				for (Mods mod : Mods.values()) {
					if (mod.getId().equals(location.getNamespace())) {
						return new SimpleDatagenIngredient(mod, location.getPath());
					}
				}
				throw new AssertionError("ID " + location.getNamespace() + " doesn't correspond to any compat mod." +
					" SimpleDatagenIngredient is not meant for deserialization anyway");
			}));


	@Override
	public Collection<ItemStack> getItems() {
		throw new AssertionError("Only for datagen output");
	}

	public static Ingredient of(DatagenMod mod, String id) {
		Ingredient.Value[] values = new Value[] { new SimpleDatagenIngredient(mod, id) };
		return new Ingredient(values);
	}
}
