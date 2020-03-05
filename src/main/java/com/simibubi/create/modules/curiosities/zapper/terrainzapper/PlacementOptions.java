package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.utility.Lang;

public enum PlacementOptions {

	Merged(ScreenResources.I_CENTERED),
	Attached(ScreenResources.I_ATTACHED),
	Inserted(ScreenResources.I_INSERTED);

	public String translationKey;
	public ScreenResources icon;

	private PlacementOptions(ScreenResources icon) {
		this.translationKey = Lang.asId(name());
		this.icon = icon;
	}

}
