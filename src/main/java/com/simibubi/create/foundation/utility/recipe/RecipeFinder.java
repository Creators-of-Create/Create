package com.simibubi.create.foundation.utility.recipe;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
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

	private static Cache<Object, StartedSearch> cachedSearches = CacheBuilder.newBuilder().build();

	public static class StartedSearch {
		List<IRecipe<?>> findings;

		public StartedSearch(List<IRecipe<?>> findings) {
			this.findings = findings;
		}

		public RecipeStream<IRecipe<?>> search() {
			return new RecipeStream<>(findings.stream());
		}

		public static class RecipeStream<R extends IRecipe<?>> {
			Stream<R> stream;

			public RecipeStream(Stream<R> stream) {
				this.stream = stream;
			}

			@SuppressWarnings("unchecked")
			public <X extends IRecipe<?>> RecipeStream<X> assumeType(IRecipeType<X> type) {
				return (RecipeStream<X>) this;
			}
			
			public RecipeStream<R> filter(Predicate<R> condition) {
				stream.filter(condition);
				return this;
			}
			
			public List<R> asList() {
				return stream.collect(Collectors.toList());
			}

		}

	}

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
	public static StartedSearch get(@Nullable Object cacheKey, World world, Predicate<IRecipe<?>> conditions) {
		if (cacheKey == null)
			return startSearch(world, conditions);

		try {
			return cachedSearches.get(cacheKey, () -> startSearch(world, conditions));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return new StartedSearch(Collections.emptyList());
	}

	private static StartedSearch startSearch(World world, Predicate<? super IRecipe<?>> conditions) {
		List<IRecipe<?>> list = world.getRecipeManager().getRecipes().stream().filter(conditions)
				.collect(Collectors.toList());
		return new StartedSearch(list);
	}

}
