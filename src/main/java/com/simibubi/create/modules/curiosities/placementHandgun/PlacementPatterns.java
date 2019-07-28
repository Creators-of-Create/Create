package com.simibubi.create.modules.curiosities.placementHandgun;

import com.simibubi.create.foundation.gui.ScreenResources;

public enum PlacementPatterns {

	Solid("Solid Material", ScreenResources.ICON_PATTERN_SOLID),
	Checkered("Checkerboard", ScreenResources.ICON_PATTERN_CHECKERED),
	InverseCheckered("Inversed Checkerboard", ScreenResources.ICON_PATTERN_CHECKERED_INVERSED),
	Chance25("25% Roll", ScreenResources.ICON_PATTERN_CHANCE_25),
	Chance50("50% Roll", ScreenResources.ICON_PATTERN_CHANCE_50),
	Chance75("75% Roll", ScreenResources.ICON_PATTERN_CHANCE_75);

	public String displayName;
	public ScreenResources icon;

	private PlacementPatterns(String displayName, ScreenResources icon) {
		this.displayName = displayName;
		this.icon = icon;
	}

}
