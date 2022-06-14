package com.simibubi.create.foundation.data.recipe;

import static com.simibubi.create.foundation.data.recipe.Mods.IE;
import static com.simibubi.create.foundation.data.recipe.Mods.MEK;
import static com.simibubi.create.foundation.data.recipe.Mods.TH;

import com.simibubi.create.foundation.utility.Lang;

public enum CompatMetals {
	ALUMINUM(IE),
	LEAD(MEK, TH, IE),
	NICKEL(TH, IE),
	OSMIUM(MEK),
	PLATINUM(),
	QUICKSILVER(),
	SILVER(TH, IE),
	TIN(TH, MEK),
	URANIUM(MEK, IE);

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
