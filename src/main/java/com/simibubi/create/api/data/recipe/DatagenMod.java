package com.simibubi.create.api.data.recipe;

import net.minecraft.resources.ResourceLocation;

public interface DatagenMod {
	default ResourceLocation asResource(String id) {
		return ResourceLocation.fromNamespaceAndPath(getId(), id);
	}

	default String recipeId(String id) {
		return "compat/" + getId() + "/" + id;
	}

	String getId();

	default ResourceLocation ingotOf(String type) {
		return ResourceLocation.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "ingot_" + type : type + "_ingot");
	}

	default ResourceLocation nuggetOf(String type) {
		return ResourceLocation.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "nugget_" + type : type + "_nugget");
	}

	default ResourceLocation oreOf(String type) {
		return ResourceLocation.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "ore_" + type : type + "_ore");
	}

	default ResourceLocation deepslateOreOf(String type) {
		return ResourceLocation.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "deepslate_ore_" + type : "deepslate_" + type + "_ore");
	}

	/**
	 * @return Whether the resource locations of this mod's metal-derived entries have the metal named appended.
	 */
	default boolean reversedMetalPrefix() {
		return false;
	}

	/**
	 * @return Whether the resource locations of this mod's stripped logs/wood have '_stripped' appended to the normal log/wood RL.
	 */
	default boolean strippedIsSuffix() {
		return false;
	}

	/**
	 * @return Whether wood blocks from this mod omit the '_wood' part of their resource locations.
	 */
	default boolean omitWoodSuffix() {
		return false;
	}
}
