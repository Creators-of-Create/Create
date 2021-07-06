package com.simibubi.create.foundation.utility.recipe;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.simibubi.create.foundation.utility.ISimpleReloadListener;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/**
 * Utility for searching through a world's recipe collection. Non-dynamic
 * conditions can be split off into an initial search for caching intermediate
 * results.
 * 
 * @author simibubi
 *
 */
public class RecipeFinder {
	
	private static Cache<Object, List<IRecipe<?>>> cachedSearches = CacheBuilder.newBuilder().build();

	/**
	 * Find all IRecipes matching the condition predicate. If this search is made
	 * more than once, using the same object instance as the cacheKey will retrieve
	 * the cached result from the first time.
	 * 
	 * @param cacheKey   (can be null to prevent the caching)
	 * @param world
	 * @param conditions
	 * @return A started search to continue with more specific conditions.
	 */
	public static List<IRecipe<?>> get(@Nullable Object cacheKey, World world, Predicate<IRecipe<?>> conditions) {
		if (cacheKey == null)
			return startSearch(world, conditions);

		try {
			return cachedSearches.get(cacheKey, () -> startSearch(world, conditions));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	private static List<IRecipe<?>> startSearch(World world, Predicate<? super IRecipe<?>> conditions) {
		List<IRecipe<?>> list = world.getRecipeManager().getRecipes().stream().filter(conditions)
				.collect(Collectors.toList());
		return list;
	}

	public static final ISimpleReloadListener LISTENER = (resourceManager, profiler) -> {
		cachedSearches.invalidateAll();
	};

}
