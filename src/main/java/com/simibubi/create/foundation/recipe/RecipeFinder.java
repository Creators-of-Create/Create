package com.simibubi.create.foundation.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.Create;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/**
 * Utility for searching through a level's recipe collection.
 * Non-dynamic conditions can be split off into an initial search for caching intermediate results.
 *
 * @author simibubi
 */
public class RecipeFinder {
	private static final Cache<Object, List<RecipeHolder<? extends Recipe<?>>>> CACHED_SEARCHES = CacheBuilder.newBuilder().build();

	public static final ResourceManagerReloadListener LISTENER = resourceManager -> CACHED_SEARCHES.invalidateAll();

	/**
	 * Find all recipes matching the condition predicate.
	 * If this search is made more than once,
	 * using the same object instance as the cacheKey will retrieve the cached result from the first search.
	 *
	 * @param cacheKey (can be null to prevent the caching)
	 * @return A started search to continue with more specific conditions.
	 */
	public static List<RecipeHolder<? extends Recipe<?>>> get(@Nullable Object cacheKey, Level level, Predicate<RecipeHolder<? extends Recipe<?>>> conditions) {
		if (cacheKey == null)
			return startSearch(level, conditions);

		try {
			return CACHED_SEARCHES.get(cacheKey, () -> startSearch(level, conditions));
		} catch (ExecutionException e) {
			Create.LOGGER.error("Encountered a exception while searching for recipes", e);
		}

		return Collections.emptyList();
	}

	private static List<RecipeHolder<? extends Recipe<?>>> startSearch(Level level, Predicate<? super RecipeHolder<? extends Recipe<?>>> conditions) {
		List<RecipeHolder<? extends Recipe<?>>> recipes = new ArrayList<>();
		for (RecipeHolder<? extends Recipe<?>> r : level.getRecipeManager().getRecipes())
			if (conditions.test(r))
				recipes.add(r);
		return recipes;
	}
}
