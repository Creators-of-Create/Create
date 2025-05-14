package com.simibubi.create.api.data.recipe;

import java.util.ArrayList;
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

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

/**
 * The builder for building Mechanical Crafting recipes.
 * @see MechanicalCraftingRecipeGen
 */
public class MechanicalCraftingRecipeBuilder {

	private final Item result;
	private final int count;
	private final List<String> pattern = Lists.newArrayList();
	private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
	private boolean acceptMirrored;
	private List<ICondition> recipeConditions;

	public MechanicalCraftingRecipeBuilder(ItemLike result, int resultCount) {
		this.result = result.asItem();
		count = resultCount;
		acceptMirrored = true;
		recipeConditions = new ArrayList<>();
	}

	/**
	 * Creates a new builder for a shaped recipe with the specified result with a count of 1
	 */
	public static MechanicalCraftingRecipeBuilder shapedRecipe(ItemLike result) {
		return shapedRecipe(result, 1);
	}

	/**
	 * Creates a new builder for a shaped recipe with the specified result and count.
	 */
	public static MechanicalCraftingRecipeBuilder shapedRecipe(ItemLike result, int resultCount) {
		return new MechanicalCraftingRecipeBuilder(result, resultCount);
	}

	/**
	 * Adds a new unique key to the recipe key for use in the pattern
	 */
	public MechanicalCraftingRecipeBuilder key(Character c, TagKey<Item> tag) {
		return this.key(c, Ingredient.of(tag));
	}

	/**
	 * Adds a new unique key to the recipe key for use in the pattern
	 */
	public MechanicalCraftingRecipeBuilder key(Character c, ItemLike item) {
		return this.key(c, Ingredient.of(item));
	}

	/**
	 * Adds a new unique key to the recipe key for use in the pattern
	 */
	public MechanicalCraftingRecipeBuilder key(Character c, Ingredient ingredient) {
		if (this.key.containsKey(c)) {
			throw new IllegalArgumentException("Symbol '" + c + "' is already defined!");
		} else if (c == ' ') {
			throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
		} else {
			this.key.put(c, ingredient);
			return this;
		}
	}

	/**
	 * Adds a new line to the pattern for this recipe. All lines
	 * for a pattern must be the same length, pad with spaces (empty slots)
	 * if necessary.
	 */
	public MechanicalCraftingRecipeBuilder patternLine(String line) {
		if (!this.pattern.isEmpty() && line.length() != this.pattern.get(0)
			.length()) {
			throw new IllegalArgumentException("Pattern must be the same width on every line!");
		} else {
			this.pattern.add(line);
			return this;
		}
	}

	/**
	 * Prevents the crafters from matching a vertically flipped version of the recipe
	 */
	public MechanicalCraftingRecipeBuilder disallowMirrored() {
		acceptMirrored = false;
		return this;
	}

	/**
	 * Builds this recipe into a {@link FinishedRecipe}.
	 */
	public void build(Consumer<FinishedRecipe> out) {
		this.build(out, CatnipServices.REGISTRIES.getKeyOrThrow(this.result));
	}

	/**
	 * Builds this recipe into a {@link FinishedRecipe}. Use
	 * {@link #build(Consumer)} if the recipe id is the same as the result item id
	 */
	public void build(Consumer<FinishedRecipe> out, String id) {
		ResourceLocation resourcelocation = CatnipServices.REGISTRIES.getKeyOrThrow(this.result);
		if ((new ResourceLocation(id)).equals(resourcelocation)) {
			throw new IllegalStateException("Shaped Recipe " + id + " should remove its 'id' argument");
		} else {
			this.build(out, new ResourceLocation(id));
		}
	}

	/**
	 * Builds this recipe into a {@link FinishedRecipe}.
	 */
	public void build(Consumer<FinishedRecipe> out, ResourceLocation id) {
		validate(id);
		out
			.accept(new MechanicalCraftingRecipeBuilder.Result(id, result, count, pattern, key, acceptMirrored, recipeConditions));
	}

	/**
	 * Makes sure that this recipe is valid.
	 * @param recipeId The id of this recipe, only used for error messages.
	 */
	private void validate(ResourceLocation recipeId) {
		if (pattern.isEmpty()) {
			throw new IllegalStateException("No pattern is defined for shaped recipe " + recipeId + "!");
		} else {
			Set<Character> set = Sets.newHashSet(key.keySet());
			set.remove(' ');

			for (String s : pattern) {
				for (int i = 0; i < s.length(); ++i) {
					char c0 = s.charAt(i);
					if (!key.containsKey(c0) && c0 != ' ')
						throw new IllegalStateException(
							"Pattern in recipe " + recipeId + " uses undefined symbol '" + c0 + "'");
					set.remove(c0);
				}
			}

			if (!set.isEmpty())
				throw new IllegalStateException(
					"Ingredients are defined but not used in pattern for recipe " + recipeId);
		}
	}

	/**
	 * Add a new condition so this recipe is only enabled when the specified mod is loaded.
	 */
	public MechanicalCraftingRecipeBuilder whenModLoaded(String modid) {
		return withCondition(new ModLoadedCondition(modid));
	}

	/**
	 * Add a new condition so this recipe is only enabled when the specified mod is not loaded.
	 */
	public MechanicalCraftingRecipeBuilder whenModMissing(String modid) {
		return withCondition(new NotCondition(new ModLoadedCondition(modid)));
	}

	/**
	 * Add a new condition so this recipe is only enabled when the condition is true.
	 */
	public MechanicalCraftingRecipeBuilder withCondition(ICondition condition) {
		recipeConditions.add(condition);
		return this;
	}

	public class Result implements FinishedRecipe {
		private final ResourceLocation id;
		private final Item result;
		private final int count;
		private final List<String> pattern;
		private final Map<Character, Ingredient> key;
		private final boolean acceptMirrored;
		private List<ICondition> recipeConditions;

		public Result(ResourceLocation recipeId, Item result, int count, List<String> pattern,
			Map<Character, Ingredient> p_i48271_7_, boolean asymmetrical, List<ICondition> recipeConditions) {
			this.id = recipeId;
			this.result = result;
			this.count = count;
			this.pattern = pattern;
			this.key = p_i48271_7_;
			this.acceptMirrored = asymmetrical;
			this.recipeConditions = recipeConditions;
		}

		public void serializeRecipeData(JsonObject o) {
			JsonArray jsonarray = new JsonArray();
			for (String s : this.pattern)
				jsonarray.add(s);

			o.add("pattern", jsonarray);
			JsonObject jsonobject = new JsonObject();
			for (Entry<Character, Ingredient> entry : this.key.entrySet())
				jsonobject.add(String.valueOf(entry.getKey()), entry.getValue()
					.toJson());

			o.add("key", jsonobject);
			JsonObject jsonobject1 = new JsonObject();
			jsonobject1.addProperty("item", CatnipServices.REGISTRIES.getKeyOrThrow(this.result)
				.toString());
			if (this.count > 1)
				jsonobject1.addProperty("count", this.count);

			o.add("result", jsonobject1);
			o.addProperty("acceptMirrored", acceptMirrored);

			if (recipeConditions.isEmpty())
				return;

			JsonArray conds = new JsonArray();
			recipeConditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			o.add("conditions", conds);
		}

		public RecipeSerializer<?> getType() {
			return AllRecipeTypes.MECHANICAL_CRAFTING.getSerializer();
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
