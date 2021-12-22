package com.simibubi.create.lib.util;

public enum FluidUnit {
	DROPLETS(1, "generic.unit.droplets"),
	MILIBUCKETS(81, "generic.unit.millibuckets");

	private final int oneBucket;
	private final String langKey;

	FluidUnit(int bucketAmount, String lang) {
		this.oneBucket = bucketAmount;
		this.langKey = lang;
	}

	public int getOneBucketAmount() {
		return oneBucket;
	}

	public String getTranslationKey() {
		return langKey;
	}
}
