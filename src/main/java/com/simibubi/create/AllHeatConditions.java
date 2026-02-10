package com.simibubi.create;

import com.simibubi.create.api.recipe.HeatCondition;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.processing.recipe.BlazeBurnerHeatCondition;

import net.minecraft.core.Registry;

public class AllHeatConditions {
	public static final BlazeBurnerHeatCondition HEATED = register("heated", BlazeBurnerHeatCondition.HEATED);
	public static final BlazeBurnerHeatCondition SUPERHEATED = register("superheated", BlazeBurnerHeatCondition.SUPERHEATED);

	private static <T extends HeatCondition> T register(String name, T condition) {
		return Registry.register(CreateBuiltInRegistries.HEAT_CONDITION, Create.asResource(name), condition);
	}

	public static void init() {}
}
