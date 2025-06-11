package com.simibubi.create.content.processing.recipe;

import com.simibubi.create.Create;
import com.simibubi.create.api.recipe.HeatCondition;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.Registry;

public class AllHeatConditions {
	public static final BlazeBurnerHeatCondition HEATED = register("heated", BlazeBurnerHeatCondition.HEATED);
	public static final BlazeBurnerHeatCondition SUPERHEATED = register("superheated", BlazeBurnerHeatCondition.SUPERHEATED);

	private static <T extends HeatCondition> T register(String name, T type) {
		return Registry.register(CreateBuiltInRegistries.HEAT_CONDITION, Create.asResource(name), type);
	}

	public static void init() {}
}
