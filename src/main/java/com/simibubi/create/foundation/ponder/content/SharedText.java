package com.simibubi.create.foundation.ponder.content;

import com.simibubi.create.foundation.ponder.PonderLocalization;

public class SharedText {

	public static void gatherText() {
		// Add entries used across several ponder scenes (Safe for hotswap)

		add("when_wrenched", "When Wrenched");
		add("more_shared", "This is Shared stuff");

	}
	
	public static String get(String key) {
		return PonderLocalization.getShared(key);
	}

	private static void add(String k, String v) {
		PonderLocalization.registerShared(k, v);
	}

}
