package com.simibubi.create.api.data.recipe;

import net.minecraft.resources.Identifier;

public interface DatagenMod {
	default Identifier asResource(String id) {
		return Identifier.fromNamespaceAndPath(getId(), id);
	}

	default String recipeId(String id) {
		return "compat/" + getId() + "/" + id;
	}

	String getId();

	default Identifier ingotOf(String type) {
		return Identifier.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "ingot_" + type : type + "_ingot");
	}

	default Identifier nuggetOf(String type) {
		return Identifier.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "nugget_" + type : type + "_nugget");
	}

	default Identifier oreOf(String type) {
		return Identifier.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "ore_" + type : type + "_ore");
	}

	default Identifier deepslateOreOf(String type) {
		return Identifier.fromNamespaceAndPath(getId(), reversedMetalPrefix() ? "deepslate_ore_" + type : "deepslate_" + type + "_ore");
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
