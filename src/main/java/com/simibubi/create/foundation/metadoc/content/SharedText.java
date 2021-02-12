package com.simibubi.create.foundation.metadoc.content;

import com.simibubi.create.foundation.metadoc.MetaDocLocalization;

public class SharedText {

	public static void gatherText() {
		// Add entries used across several metadoc stories (Safe for hotswap)

		add("when_wrenched", "When Wrenched");
		add("more_shared", "This is Shared stuff");

	}
	
	public static String get(String key) {
		return MetaDocLocalization.getShared(key);
	}

	private static void add(String k, String v) {
		MetaDocLocalization.registerShared(k, v);
	}

}
