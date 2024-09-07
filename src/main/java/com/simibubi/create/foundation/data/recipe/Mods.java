package com.simibubi.create.foundation.data.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.resources.ResourceLocation;

public enum Mods {

	MEK("mekanism", Builder::reverseMetalPrefix),
	TH("thermal"),
	IE("immersiveengineering", Builder::reverseMetalPrefix),
	FD("farmersdelight"),
	ARS_N("ars_nouveau"),
	BSK("blue_skies"),
	BTN("botania", Builder::omitWoodSuffix),
	FA("forbidden_arcanus", b -> b.omitWoodSuffix()),
	HEX("hexcasting", b -> b.strippedWoodIsSuffix()),
	ID("integrateddynamics", Builder::strippedWoodIsSuffix),
	BYG("byg"),
	SG("silentgear"),
	TIC("tconstruct"),
	AP("architects_palette"),
	Q("quark"),
	BOP("biomesoplenty"),
	TF("twilightforest"),
	ECO("ecologics"),
	IC2("ic2", b -> b.reverseMetalPrefix().addNameReplacements(Map.of("aluminum", "aluminium"))),
	ATM("atmospheric"),
	ATM_2("atmospheric", Builder::omitWoodSuffix),
	AUTUM("autumnity"),
	DRUIDCRAFT("druidcraft"),
	ENDER("endergetic"),
	PVJ("projectvibrantjourneys"),
	UA("upgrade_aquatic"),
	BEF("betterendforge"),
	ENV("environmental"),
	SUP("supplementaries"),
  	AM("alexsmobs"),
	NEA("neapolitan"),
	AE2("ae2"),
	MC("minecraft"),
	BB("buzzier_bees"),
	SILENT_GEMS("silentgems"),
	SF("simplefarming"),
	OREGANIZED("oreganized"),
	GS("galosphere"),
	VH("the_vault"),
	IX("infernalexp"),
	GOOD("goodending"),
	BMK("biomemakeover"),
	NE("nethers_exoticism"),
	RU("regions_unexplored"),
	EO("elementaryores"),
	IF("iceandfire"),
	ENS("exnihilosequentia"),
	AET("aether"),
	HH("hauntedharvest"),
	VMP("vampirism")

	;

	private final String id;

	public boolean reversedMetalPrefix;
	public boolean strippedIsSuffix;
	public boolean omitWoodSuffix;
	public final Map<String, String> nameReplacements = new HashMap<>();

	private Mods(String id) {
		this(id, b -> {
		});
	}

	private Mods(String id, Consumer<Builder> props) {
		props.accept(new Builder());
		this.id = id;
	}

	public ResourceLocation ingotOf(String type) {
		return new ResourceLocation(id, reversedMetalPrefix ? "ingot_" + type : type + "_ingot");
	}

	public ResourceLocation nuggetOf(String type) {
		return new ResourceLocation(id, reversedMetalPrefix ? "nugget_" + type : type + "_nugget");
	}

	public ResourceLocation oreOf(String type) {
		return new ResourceLocation(id, reversedMetalPrefix ? "ore_" + type : type + "_ore");
	}

	public ResourceLocation deepslateOreOf(String type) {
		return new ResourceLocation(id, reversedMetalPrefix ? "deepslate_ore_" + type : "deepslate_" + type + "_ore");
	}

	public ResourceLocation asResource(String id) {
		return new ResourceLocation(this.id, id);
	}

	public String recipeId(String id) {
		return "compat/" + this.id + "/" + id;
	}

	public String getId() {
		return id;
	}

	@SuppressWarnings("UnusedReturnValue")
	class Builder {

		Builder reverseMetalPrefix() {
			reversedMetalPrefix = true;
			return this;
		}

		Builder strippedWoodIsSuffix() {
			strippedIsSuffix = true;
			return this;
		}

		Builder omitWoodSuffix() {
			omitWoodSuffix = true;
			return this;
		}

		Builder addNameReplacements(Map<String, String> map) {
			for (String p : map.keySet())
				nameReplacements.put(p, map.get(p));
			return this;
		}

	}

}
