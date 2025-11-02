package com.simibubi.create.foundation.recipe.trie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;

public class RecipeTrie<R extends Recipe<?>> {
	private static final int MAX_CACHE_SIZE = Integer.getInteger("create.recipe_trie.max_cache_size", 512);

	private final IntArrayTrie<R> trie;
	private final Object2IntMap<AbstractVariant> variantToId;
	private final Int2ObjectMap<IntSet> variantToIngredients;
	private final int universalIngredientId;

	private final Cache<Set<AbstractVariant>, IntSet> ingredientCache = CacheBuilder.newBuilder()
		.maximumSize(MAX_CACHE_SIZE)
		.build();

	private RecipeTrie(IntArrayTrie<R> trie, Object2IntMap<AbstractVariant> variantToId, Int2ObjectMap<IntSet> variantToIngredients, int universalIngredientId) {
		this.trie = trie;
		this.variantToId = variantToId;
		this.variantToIngredients = variantToIngredients;
		this.universalIngredientId = universalIngredientId;
	}

	public static @NotNull Set<AbstractVariant> getVariants(@Nullable IItemHandler itemStorage, @Nullable IFluidHandler fluidStorage) {
		Set<AbstractVariant> variants = new HashSet<>();

		if (itemStorage != null) {
			for (int slot = 0; slot < itemStorage.getSlots(); slot++) {
				ItemStack item = itemStorage.getStackInSlot(slot);
				if (item.isEmpty()) continue;

				variants.add(new AbstractVariant.AbstractItem(item.getItem()));
			}
		}

		if (fluidStorage != null) {
			for (int tank = 0; tank < fluidStorage.getTanks(); tank++) {
				FluidStack fluid = fluidStorage.getFluidInTank(tank);
				if (fluid.isEmpty()) continue;

				variants.add(new AbstractVariant.AbstractFluid(fluid.getFluid()));
			}
		}

		return variants;
	}

	private IntSet getAvailableIngredients(@NotNull Set<AbstractVariant> pool) {
		pool.retainAll(variantToId.keySet());

		try {
			return ingredientCache.get(Set.copyOf(pool), () -> {
				IntSet ingredients = new IntOpenHashSet();
				ingredients.add(universalIngredientId);

				for (AbstractVariant variant : pool) {
					int id = variantToId.getInt(variant);
					if (id >= 0) {
						var ingredientIds = variantToIngredients.get(id);
						if (ingredientIds != null) {
							ingredients.addAll(ingredientIds);
						}
					}
				}

				return ingredients;
			});
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Look up all recipes that can be made with (a subset of) the given pool of resources.
	 *
	 * @param pool the set of allowable variants. It will be modified to only contain known variants.
	 * @return all recipes that can be made with the given pool of resources.
	 */
	public @NotNull List<R> lookup(@NotNull Set<AbstractVariant> pool) {
		return trie.lookup(getAvailableIngredients(pool));
	}

	public static <R extends Recipe<?>> Builder<R> builder() {
		return new Builder<>();
	}

	public static class Builder<R extends Recipe<?>> {
		private final IntArrayTrie<R> trie = new IntArrayTrie<>();

		private final Map<Object, AbstractVariant> variantCache = new HashMap<>();
		private final Object2IntOpenHashMap<AbstractVariant> variantToId = new Object2IntOpenHashMap<>();
		private int nextVariantId = 0;

		private final Object2IntMap<AbstractIngredient> ingredientToId = new Object2IntOpenHashMap<>();
		private int nextIngredientId = 0;
		private final int universalIngredientId;

		private final Int2ObjectOpenHashMap<IntSet> variantToIngredients = new Int2ObjectOpenHashMap<>();

		private Builder() {
			variantToId.defaultReturnValue(-1);
			ingredientToId.defaultReturnValue(-1);

			universalIngredientId = getOrAssignId(AbstractIngredient.Universal.INSTANCE);
		}

		private int getOrAssignId(AbstractIngredient ingredient) {
			return ingredientToId.computeIfAbsent(ingredient, $ -> {
				int id = nextIngredientId++;
				for (AbstractVariant variant : ingredient.variants) {
					variantToIngredients.computeIfAbsent(getOrAssignId(variant), $1 -> new IntOpenHashSet()).add(id);
				}
				return id;
			});
		}

		private int getOrAssignId(AbstractVariant variant) {
			return variantToId.computeIfAbsent(variant, $ -> nextVariantId++);
		}

		private AbstractVariant getOrAssignVariant(Item item) {
			AbstractVariant variant = variantCache.computeIfAbsent(item, $ -> new AbstractVariant.AbstractItem(item));
			getOrAssignId(variant);
			return variant;
		}

		private AbstractVariant getOrAssignVariant(Fluid fluid) {
			AbstractVariant variant = variantCache.computeIfAbsent(fluid, $ -> new AbstractVariant.AbstractFluid(fluid));
			getOrAssignId(variant);
			return variant;
		}

		private void insert(AbstractRecipe<? extends R> recipe) {
			int[] key = new int[recipe.ingredients.size()];
			int i = 0;
			for (AbstractIngredient ingredient : recipe.ingredients) {
				key[i++] = getOrAssignId(ingredient);
			}
			Arrays.sort(key);
			trie.insert(key, recipe.recipe);
		}

		/**
		 * Insert a recipe into the trie.
		 * <br/>
		 * Will handle item ingredients for all recipes, and fluid ingredients for {@link BasinRecipe}s.
		 */
		public <R1 extends R> void insert(R1 recipe) {
			insert(createRecipe(recipe));
		}

		private <R1 extends R> AbstractRecipe<R1> createRecipe(R1 recipe) {
			Set<AbstractIngredient> ingredients = new HashSet<>();

			for (Ingredient ingredient : recipe.getIngredients()) {
				if (ingredient.isEmpty()) {
					ingredients.add(AbstractIngredient.Universal.INSTANCE);
					continue;
				}
				if (!ingredient.isSimple()) {
					ingredients.add(AbstractIngredient.Universal.INSTANCE);
					continue;
				}

				Set<AbstractVariant> variants = new HashSet<>();
				for (ItemStack stack : ingredient.getItems()) {
					variants.add(getOrAssignVariant(stack.getItem()));
				}

				ingredients.add(new AbstractIngredient(variants));
			}

			if (recipe instanceof BasinRecipe basinRecipe)
				for (SizedFluidIngredient ingredient : basinRecipe.getFluidIngredients()) {
					if (ingredient.amount() == 0) {
						ingredients.add(AbstractIngredient.Universal.INSTANCE);
						continue;
					}

					Set<AbstractVariant> variants = new HashSet<>();
					for (FluidStack stack : ingredient.getFluids()) {
						variants.add(getOrAssignVariant(stack.getFluid()));
					}

					ingredients.add(new AbstractIngredient(variants));
				}

			return new AbstractRecipe<>(recipe, ingredients);
		}

		public RecipeTrie<R> build() {
			variantToId.trim();
			variantToIngredients.trim();
			Create.LOGGER.info(
				"RecipeTrie of depth {} with {} nodes built with {} variants, {} ingredients, and {} recipes",
				trie.getMaxDepth(), trie.getNodeCount(),
				variantToId.size(), ingredientToId.size(), trie.getValueCount()
			);
			return new RecipeTrie<>(trie, variantToId, variantToIngredients, universalIngredientId);
		}
	}
}
