package com.simibubi.create.modules.contraptions.processing;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;

public class ProcessingIngredient implements Predicate<ItemStack> {

	private float outputChance;
	private Ingredient ingredient;
	private static Random r = new Random();

	public ProcessingIngredient(Ingredient ingredient) {
		this(ingredient, 0);
	}

	public ProcessingIngredient(Ingredient ingredient, float outputChance) {
		this.ingredient = ingredient;
		this.outputChance = outputChance;
	}

	public float getOutputChance() {
		return outputChance;
	}

	public boolean isCatalyst() {
		return outputChance > 0;
	}

	public static ProcessingIngredient parse(PacketBuffer buffer) {
		Ingredient ingredient = Ingredient.read(buffer);
		return new ProcessingIngredient(ingredient, buffer.readFloat());
	}

	public static ProcessingIngredient parse(JsonObject json) {
		Ingredient ingredient = Ingredient.deserialize(json);
		float chance = 0;
		if (json.has("return_chance"))
			chance = json.get("return_chance").getAsFloat();
		return new ProcessingIngredient(ingredient, chance);
	}

	public void write(PacketBuffer buffer) {
		getIngredient().write(buffer);
		buffer.writeFloat(outputChance);
	}

	@Override
	public boolean test(ItemStack t) {
		return ingredient.test(t);
	}

	public Ingredient getIngredient() {
		return ingredient;
	}

	public static List<ProcessingIngredient> list(List<Ingredient> ingredients) {
		return ingredients.stream().map(ProcessingIngredient::new).collect(Collectors.toList());
	}

	public boolean remains() {
		return r.nextFloat() <= outputChance;
	}

}
