package com.simibubi.create.infrastructure.config;

import com.simibubi.create.foundation.config.ConfigBase;

public class CServer extends ConfigBase {

	public final ConfigGroup infrastructure = group(0, "infrastructure", Comments.infrastructure);
	public final ConfigInt tickrateSyncTimer =
		i(20, 5, "tickrateSyncTimer", "[in Ticks]", Comments.tickrateSyncTimer, Comments.tickrateSyncTimer2);

	public final CRecipes recipes = nested(0, CRecipes::new, Comments.recipes);
	public final CKinetics kinetics = nested(0, CKinetics::new, Comments.kinetics);
	public final CFluids fluids = nested(0, CFluids::new, Comments.fluids);
	public final CLogistics logistics = nested(0, CLogistics::new, Comments.logistics);
	public final CSchematics schematics = nested(0, CSchematics::new, Comments.schematics);
	public final CEquipment equipment = nested(0, CEquipment::new, Comments.equipment);
	public final CTrains trains = nested(0, CTrains::new, Comments.trains);

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
		static String equipment = "Equipment and gadgets added by Create";
		static String trains = "Create's builtin Railway systems";
		static String infrastructure = "The Backbone of Create";
		static String tickrateSyncTimer =
			"The amount of time a server waits before sending out tickrate synchronization packets.";
		static String tickrateSyncTimer2 = "These packets help animations to be more accurate when tps is below 20.";
	}

}
