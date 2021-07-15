package com.simibubi.create.foundation.config;
import com.simibubi.create.foundation.config.ConfigBase.ConfigGroup;
import com.simibubi.create.foundation.config.ConfigBase.ConfigInt;


public class CServer extends ConfigBase {

	public ConfigGroup infrastructure = group(0, "infrastructure", Comments.infrastructure);
	public ConfigInt tickrateSyncTimer =
		i(20, 5, "tickrateSyncTimer", "[in Ticks]", Comments.tickrateSyncTimer, Comments.tickrateSyncTimer2);

	public CRecipes recipes = nested(0, CRecipes::new, Comments.recipes);
	public CKinetics kinetics = nested(0, CKinetics::new, Comments.kinetics);
	public CFluids fluids = nested(0, CFluids::new, Comments.fluids);
	public CLogistics logistics = nested(0, CLogistics::new, Comments.logistics);
	public CSchematics schematics = nested(0, CSchematics::new, Comments.schematics);
	public CCuriosities curiosities = nested(0, CCuriosities::new, Comments.curiosities);

	@Override
	public String getName() {
		return "server";
	}

	private static class Comments {
		static String recipes = "Packmakers' control panel for internal recipe compat";
		static String schematics = "Everything related to Schematic tools";
		static String kinetics = "Parameters and abilities of Create's kinetic mechanisms";
		static String fluids = "Create's liquid manipulation tools";
		static String logistics = "Tweaks for logistical components";
		static String curiosities = "Gadgets and other Shenanigans added by Create";
		static String infrastructure = "The Backbone of Create";
		static String tickrateSyncTimer =
			"The amount of time a server waits before sending out tickrate synchronization packets.";
		static String tickrateSyncTimer2 = "These packets help animations to be more accurate when tps is below 20.";
	}

}
