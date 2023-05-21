package com.simibubi.create.infrastructure.ponder;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.ponder.PonderLocalization;

import net.minecraft.resources.ResourceLocation;

public class SharedText {

	public static void gatherText() {
		// Add entries used across several ponder scenes (Safe for hotswap)

		add("sneak_and", "Sneak +");
		add("ctrl_and", "Ctrl +");

		add("rpm8", "8 RPM");
		add("rpm16", "16 RPM");
		add("rpm16_source", "Source: 16 RPM");
		add("rpm32", "32 RPM");

		add("movement_anchors", "With the help of Super Glue, larger structures can be moved");
		add("behaviour_modify_value_panel", "This behaviour can be modified using the value panel");
		add("storage_on_contraption", "Inventories attached to the Contraption will pick up their drops automatically");
	}

	public static String get(ResourceLocation key) {
		return PonderLocalization.getShared(key);
	}

	public static void add(ResourceLocation k, String v) {
		PonderLocalization.registerShared(k, v);
	}

	private static void add(String k, String v) {
		add(Create.asResource(k), v);
	}

}
