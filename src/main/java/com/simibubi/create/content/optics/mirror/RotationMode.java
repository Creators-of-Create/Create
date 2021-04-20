package com.simibubi.create.content.optics.mirror;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.INamedIconOptions;
import com.simibubi.create.foundation.utility.Lang;

public enum RotationMode implements INamedIconOptions {

	// FIXME: Add proper icons
	ROTATE_FREE(AllIcons.I_ROTATE_PLACE),
	ROTATE_45(AllIcons.I_ROTATE_PLACE_RETURNED),
	ROTATE_LIMITED(AllIcons.I_ROTATE_NEVER_PLACE);


	private final String translationKey;
	private final AllIcons icon;

	RotationMode(AllIcons icon) {
		this.icon = icon;
		translationKey = "optics.mirror.movement_mode." + Lang.asId(name());
	}

	@Override
	public AllIcons getIcon() {
		return icon;
	}

	@Override
	public String getTranslationKey() {
		return translationKey;
	}

}
