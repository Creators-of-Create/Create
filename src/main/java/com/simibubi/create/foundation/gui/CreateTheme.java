package com.simibubi.create.foundation.gui;

import net.createmod.catnip.utility.theme.Color;
import net.createmod.catnip.utility.theme.Theme;

public class CreateTheme extends Theme {

	public static final CreateTheme CREATE_THEME = addTheme(new CreateTheme());

	@Override
	protected void init() {

		put(Key.STANDARD_TOOLTIP, new Color(0xff_c9974c), new Color(0xff_f1dd79));

	}

	public static void loadClass() {}

	public static class Key {

		public static final Theme.Key STANDARD_TOOLTIP = new Theme.Key();

	}
}
