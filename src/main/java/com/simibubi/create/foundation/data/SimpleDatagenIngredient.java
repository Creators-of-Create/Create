package com.simibubi.create.foundation.data;

import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.api.data.recipe.DatagenMod;

import net.minecraft.world.item.crafting.Ingredient;

public class SimpleDatagenIngredient extends Ingredient {

	private DatagenMod mod;
	private String id;

	public SimpleDatagenIngredient(DatagenMod mod, String id) {
		super(Stream.empty());
		this.mod = mod;
		this.id = id;
	}

	@Override
	public JsonElement toJson() {
		JsonObject jsonobject = new JsonObject();
		jsonobject.addProperty("item", mod.asResource(id)
			.toString());
		return jsonobject;
	}

}
