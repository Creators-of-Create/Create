package com.simibubi.create.lib.helper;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.lib.mixin.accessor.GlStateManager$BooleanStateAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

public final class BooleanStateHelper {
	public static boolean getState(GlStateManager.BooleanState state) {
		return get(state).create$state();
	}

	private static GlStateManager$BooleanStateAccessor get(GlStateManager.BooleanState state) {
		return MixinHelper.cast(state);
	}

	private void GlStateManager$BooleanStateHelper() {}
}
