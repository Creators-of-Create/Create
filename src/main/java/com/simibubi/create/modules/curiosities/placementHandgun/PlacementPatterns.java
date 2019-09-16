package com.simibubi.create.modules.curiosities.placementHandgun;

import com.simibubi.create.ScreenResources;

public enum PlacementPatterns {

	Solid(ScreenResources.ICON_PATTERN_SOLID),
	Checkered(ScreenResources.ICON_PATTERN_CHECKERED),
	InverseCheckered(ScreenResources.ICON_PATTERN_CHECKERED_INVERSED),
	Chance25(ScreenResources.ICON_PATTERN_CHANCE_25),
	Chance50(ScreenResources.ICON_PATTERN_CHANCE_50),
	Chance75(ScreenResources.ICON_PATTERN_CHANCE_75);

	public String translationKey;
	public ScreenResources icon;

	private PlacementPatterns(ScreenResources icon) {
		this.translationKey = name().toLowerCase();
		this.icon = icon;
	}

}
