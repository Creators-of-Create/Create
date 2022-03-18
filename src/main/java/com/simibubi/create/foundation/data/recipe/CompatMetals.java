package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.Mods.EID;
import static com.simibubi.create.foundation.data.recipe.Mods.IE;
import static com.simibubi.create.foundation.data.recipe.Mods.INF;
import static com.simibubi.create.foundation.data.recipe.Mods.MEK;
import static com.simibubi.create.foundation.data.recipe.Mods.MW;
import static com.simibubi.create.foundation.data.recipe.Mods.SM;
import static com.simibubi.create.foundation.data.recipe.Mods.TH;

import com.simibubi.create.foundation.utility.Lang;

public enum CompatMetals {
	ALUMINUM(IE, SM),
	LEAD(MEK, TH, MW, IE, SM, EID),
	NICKEL(TH, IE, SM),
	OSMIUM(MEK),
	PLATINUM(SM),
	QUICKSILVER(MW),
	SILVER(TH, MW, IE, SM, INF),
	TIN(TH, MEK, MW, SM),
	URANIUM(MEK, IE, SM);

	private final Mods[] mods;
	private final String name;

	CompatMetals(Mods... mods) {
		this.name = Lang.asId(name());
		this.mods = mods;
	}

	public String getName() {
		return name;
	}

	/**
	 * These mods must provide an ingot and nugget variant of the corresponding metal.
	 */
	public Mods[] getMods() {
		return mods;
	}
}
