package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.foundation.ponder.PonderLocalization;

public class SharedText {

	public static void gatherText() {
		// Add entries used across several ponder scenes (Safe for hotswap)

		add("sneak_and", "Sneak +");
		add("ctrl_and", "Ctrl +");

		add("rpm8", "8 RPM");
		add("rpm16", "16 RPM");
		add("rpm32", "32 RPM");

		add("movement_anchors", "With the help of Chassis or Super Glue, larger structures can be moved.");

	}

	public static String get(String key) {
		return PonderLocalization.getShared(key);
	}

	private static void add(String k, String v) {
		PonderLocalization.registerShared(k, v);
	}

}
