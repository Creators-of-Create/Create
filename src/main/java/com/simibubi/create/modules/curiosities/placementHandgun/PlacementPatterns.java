package com.simibubi.create.modules.curiosities.placementHandgun;

import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;

public enum PlacementPatterns {

	Solid(ScreenResources.I_PATTERN_SOLID),
	Checkered(ScreenResources.I_PATTERN_CHECKERED),
	InverseCheckered(ScreenResources.I_PATTERN_CHECKERED_INVERSED),
	Chance25(ScreenResources.I_PATTERN_CHANCE_25),
	Chance50(ScreenResources.I_PATTERN_CHANCE_50),
	Chance75(ScreenResources.I_PATTERN_CHANCE_75);

	public String translationKey;
	public ScreenResources icon;

	private PlacementPatterns(ScreenResources icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

}
