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
	public static MechanicalCraftingRecipeBuilder shapedRecipe(IItemProvider p_200470_0_) {
		return shapedRecipe(p_200470_0_, 1);
	}

	/**
	 * Creates a new builder for a shaped recipe.
	 */
	public static MechanicalCraftingRecipeBuilder shapedRecipe(IItemProvider p_200468_0_, int p_200468_1_) {
		return new MechanicalCraftingRecipeBuilder(p_200468_0_, p_200468_1_);
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character p_200469_1_, Tag<Item> p_200469_2_) {
		return this.key(p_200469_1_, Ingredient.fromTag(p_200469_2_));
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character p_200462_1_, IItemProvider p_200462_2_) {
		return this.key(p_200462_1_, Ingredient.fromItems(p_200462_2_));
	}

	/**
	 * Adds a key to the recipe pattern.
	 */
	public MechanicalCraftingRecipeBuilder key(Character p_200471_1_, Ingredient p_200471_2_) {
		if (this.key.containsKey(p_200471_1_)) {
			throw new IllegalArgumentException("Symbol '" + p_200471_1_ + "' is already defined!");
		} else if (p_200471_1_ == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(p_200471_1_, p_200471_2_);
			return this;
		}
	}

	/**
	 * Adds a new entry to the patterns for this recipe.
	 */
	public MechanicalCraftingRecipeBuilder patternLine(String p_200472_1_) {
		if (!this.pattern.isEmpty() && p_200472_1_.length() != this.pattern.get(0)
			.length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.pattern.add(p_200472_1_);
			return this;
		}
	}

	/**
	 * Builds this recipe into an {@link IFinishedRecipe}.
	 */
	public void build(Consumer<IFinishedRecipe> p_200464_1_) {
		this.build(p_200464_1_, ForgeRegistries.ITEMS.getKey(this.result));
	}

	/**
	 * Builds this recipe into an {@link IFinishedRecipe}. Use
	 * {@link #build(Consumer)} if save is the same as the ID for the result.
	 */
	public void build(Consumer<IFinishedRecipe> p_200466_1_, String p_200466_2_) {
		ResourceLocation resourcelocation = ForgeRegistries.ITEMS.getKey(this.result);
		if ((new ResourceLocation(p_200466_2_)).equals(resourcelocation)) {
			throw new IllegalStateException("Shaped Recipe " + p_200466_2_ + " should remove its 'save' argument");
		} else {
			this.build(p_200466_1_, new ResourceLocation(p_200466_2_));
		}
	}

	/**
	 * Builds this recipe into an {@link IFinishedRecipe}.
	 */
	public void build(Consumer<IFinishedRecipe> p_200467_1_, ResourceLocation p_200467_2_) {
		validate(p_200467_2_);
		p_200467_1_.accept(new MechanicalCraftingRecipeBuilder.Result(p_200467_2_, result, count, pattern, key));
	}

	/**
	 * Makes sure that this recipe is valid.
	 */
	private void validate(ResourceLocation p_200463_1_) {
		if (pattern.isEmpty()) {
			throw new IllegalStateException("No pattern is defined for shaped recipe " + p_200463_1_ + "!");
		} else {
			Set<Character> set = Sets.newHashSet(key.keySet());
			set.remove(' ');

			for (String s : pattern) {
				for (int i = 0; i < s.length(); ++i) {
					char c0 = s.charAt(i);
					if (!key.containsKey(c0) && c0 != ' ')
						throw new IllegalStateException(
							"Pattern in recipe " + p_200463_1_ + " uses undefined symbol '" + c0 + "'");
					set.remove(c0);
				}
			}

			if (!set.isEmpty())
				throw new IllegalStateException(
					"Ingredients are defined but not used in pattern for recipe " + p_200463_1_);
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

		public void serialize(JsonObject p_218610_1_) {
			JsonArray jsonarray = new JsonArray();
			for (String s : this.pattern)
				jsonarray.add(s);

			p_218610_1_.add("pattern", jsonarray);
			JsonObject jsonobject = new JsonObject();
			for (Entry<Character, Ingredient> entry : this.key.entrySet())
				jsonobject.add(String.valueOf(entry.getKey()), entry.getValue()
					.serialize());

			p_218610_1_.add("key", jsonobject);
			JsonObject jsonobject1 = new JsonObject();
			jsonobject1.addProperty("item", ForgeRegistries.ITEMS.getKey(this.result)
				.toString());
			if (this.count > 1)
				jsonobject1.addProperty("count", this.count);

			p_218610_1_.add("result", jsonobject1);
		}

		public IRecipeSerializer<?> getSerializer() {
			return AllRecipeTypes.MECHANICAL_CRAFTING.serializer;
		}

		public ResourceLocation getID() {
			return this.id;
		}

		@Nullable
		public JsonObject getAdvancementJson() {
			return null;
		}

		@Nullable
		public ResourceLocation getAdvancementID() {
			return null;
		}
	}

}
