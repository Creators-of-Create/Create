package com.simibubi.create.foundation.data.recipe;

import java.util.function.Consumer;

import com.simibubi.create.Create;
import com.simibubi.create.api.data.recipe.DatagenMod;

/**
 * A helper class for mods that Create has built in compatibility for.
 * Not considered part of Create's API, addons wishing to add to this should make
 * their own instead, with their own helper methods in the generation classes.
 */
public enum Mods implements DatagenMod {
	VANILLA("minecraft"),
	CREATE(Create.ID),

	MEK("mekanism", b -> b.reverseMetalPrefix()),
	TH("thermal"),
	IE("immersiveengineering", b -> b.reverseMetalPrefix()),
	FD("farmersdelight"),
	ARS_N("ars_nouveau"),
	BSK("blue_skies"),
	BTN("botania", b -> b.omitWoodSuffix()),
	FA("forbidden_arcanus"),
	HEX("hexcasting"),
	ID("integrateddynamics", b -> b.strippedWoodIsSuffix()),
	BWG("biomeswevegone"),
	SG("silentgear"),
	TIC("tconstruct"),
	AP("architects_palette"),
	Q("quark"),
	BOP("biomesoplenty"),
	TF("twilightforest"),
	ECO("ecologics"),
	IC2("ic2", b -> b.reverseMetalPrefix()),
	ATM("atmospheric"),
	ATM_2("atmospheric", b -> b.omitWoodSuffix()),
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
	VMP("vampirism"),
	WSP("windswept"),
	D_AET("deep_aether"),
	A_AET("ancient_aether"),
	AET_R("aether_redux"),
	GOTD("gardens_of_the_dead"),
	UUE("unusualend"),
	UG("undergarden"),
	DD("deeperdarker"),
	ARS_E("ars_elemental", b -> b.omitWoodSuffix()),
	JNE("netherexp")

	;

	private final String id;

	private boolean reversedMetalPrefix;
	private boolean strippedIsSuffix;
	private boolean omitWoodSuffix;

	private Mods(String id) {
		this(id, b -> {
		});
	}

	private Mods(String id, Consumer<Builder> props) {
		props.accept(new Builder());
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean reversedMetalPrefix() {
		return reversedMetalPrefix;
	}

	@Override
	public boolean strippedIsSuffix() {
		return strippedIsSuffix;
	}

	@Override
	public boolean omitWoodSuffix() {
		return omitWoodSuffix;
	}

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

	}

}
