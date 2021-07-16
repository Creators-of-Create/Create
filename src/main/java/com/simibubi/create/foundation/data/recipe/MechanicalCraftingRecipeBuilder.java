package com.simibubi.create.foundation.data.recipe;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class MechanicalCraftingRecipeBuilder {

	private final Item result;
	private final int count;
	private final List<String> pattern = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();

	public MechanicalCraftingRecipeBuilder(IItemProvider p_i48261_1_, int p_i48261_2_) {
		result = p_i48261_1_.asItem();
		count = p_i48261_2_;
	}

	/**
	 * Creates a new builder for a shaped recipe.
	 */
	public static MechanicalCraftingRecipeBuilder shapedRecipe(IItemProvider pResultIn) {
		return shapedRecipe(pResultIn, 1);
	}

	/**
	 * Creates a new builder for a shaped recipe.
	 */
	public static MechanicalCraftingRecipeBuilder shapedRecipe(IItemProvider pResultIn, int pCountIn) {
		return new MechanicalCraftingRecipeBuilder(pResultIn, pCountIn);
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character pSymbol, Tag<Item> pTagIn) {
		return this.key(pSymbol, Ingredient.of(pTagIn));
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character pSymbol, IItemProvider pItemIn) {
		return this.key(pSymbol, Ingredient.of(pItemIn));
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character pSymbol, Ingredient pIngredientIn) {
		if (this.key.containsKey(pSymbol)) {
			throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
		} else if (pSymbol == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(pSymbol, pIngredientIn);
			return this;
		}
	}

	/**
	 * Adds a new entry to the patterns for this recipe.
	 */
	public MechanicalCraftingRecipeBuilder patternLine(String pPatternIn) {
		if (!this.pattern.isEmpty() && pPatternIn.length() != this.pattern.get(0)
			.length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.pattern.add(pPatternIn);
			return this;
		}
	}

	/**
	 * Builds this recipe into an {@link IFinishedRecipe}.
	 */
	public void build(Consumer<IFinishedRecipe> pConsumerIn) {
		this.build(pConsumerIn, ForgeRegistries.ITEMS.getKey(this.result));
	}

	/**
	 * Builds this recipe into an {@link IFinishedRecipe}. Use
	 * {@link #build(Consumer)} if save is the same as the ID for the result.
	 */
	public void build(Consumer<IFinishedRecipe> pConsumerIn, String pSave) {
		ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(this.result);
		if ((new ResourceLocation(pSave)).equals(resourcelocation)) {
			throw new IllegalStateException("Shaped Recipe " + pSave + " should remove its 'save' argument");
		} else {
			this.build(pConsumerIn, new ResourceLocation(pSave));
		}
	}

	/**
	 * Builds this recipe into an {@link IFinishedRecipe}.
	 */
	public void build(Consumer<IFinishedRecipe> pConsumerIn, ResourceLocation pId) {
		validate(pId);
		pConsumerIn.accept(new MechanicalCraftingRecipeBuilder.Result(pId, result, count, pattern, key));
	}

	/**
	 * Makes sure that this recipe is valid.
	 */
	private void validate(ResourceLocation pId) {
		if (pattern.isEmpty()) {
			throw new IllegalStateException("No pattern is defined for shaped recipe " + pId + "!");
		} else {
			Set<Character> set = Sets.newHashSet(key.keySet());
			set.remove(' ');

			for (String s : pattern) {
				for (int i = 0; i < s.length(); ++i) {
					char c0 = s.charAt(i);
					if (!key.containsKey(c0) && c0 != ' ')
						throw new IllegalStateException(
							"Pattern in recipe " + pId + " uses undefined symbol '" + c0 + "'");
					set.remove(c0);
				}
			}

			if (!set.isEmpty())
				throw new IllegalStateException(
					"Ingredients are defined but not used in pattern for recipe " + pId);
		}
	}

	public class Result implements IFinishedRecipe {
		private final ResourceLocation id;
		private final Item result;
		private final int count;
		private final List<String> pattern;
		private final Map<Character, Ingredient> key;

		public Result(ResourceLocation p_i48271_2_, Item p_i48271_3_, int p_i48271_4_, List<String> p_i48271_6_,
			Map<Character, Ingredient> p_i48271_7_) {
			this.id = p_i48271_2_;
			this.result = p_i48271_3_;
			this.count = p_i48271_4_;
			this.pattern = p_i48271_6_;
			this.key = p_i48271_7_;
		}

		public void serializeRecipeData(JsonObject pJson) {
			JsonArray jsonarray = new JsonArray();
			for (String s : this.pattern)
				jsonarray.add(s);

			pJson.add("pattern", jsonarray);
			JsonObject jsonobject = new JsonObject();
			for (Entry<Character, Ingredient> entry : this.key.entrySet())
				jsonobject.add(String.valueOf(entry.getKey()), entry.getValue()
					.toJson());

			pJson.add("key", jsonobject);
			JsonObject jsonobject1 = new JsonObject();
			jsonobject1.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result)
				.toString());
			if (this.count > 1)
				jsonobject1.addProperty("count", this.count);

			pJson.add("result", jsonobject1);
		}

		public IRecipeSerializer<?> getType() {
			return AllRecipeTypes.MECHANICAL_CRAFTING.serializer;
		}

		public ResourceLocation getId() {
			return this.id;
		}

		@Nullable
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Nullable
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}

}
