package com.simibubi.create.foundation.utility;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;
import java.util.List;

public class RecipeGenericsUtil {
	@SuppressWarnings("unchecked")
	public static <P extends Recipe<?>, C extends P> List<RecipeHolder<P>> cast(List<RecipeHolder<C>> list) {
		return (List<RecipeHolder<P>>) (List<?>) list;
	}

	public static Collection<RecipeHolder<? extends Recipe<?>>> specify(Collection<RecipeHolder<?>> list) {
		return list;
	}
}
