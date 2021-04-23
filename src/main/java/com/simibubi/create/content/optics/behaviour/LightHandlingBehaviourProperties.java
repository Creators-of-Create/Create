package com.simibubi.create.content.optics.behaviour;

public class LightHandlingBehaviourProperties {
	int scanRange;
	boolean absorbsLight;
	boolean scansBeacon;

	private LightHandlingBehaviourProperties() {
		scanRange = 128;
		absorbsLight = true;
		scansBeacon = true;
	}

	public static LightHandlingBehaviourProperties create() {
		return new LightHandlingBehaviourProperties();
	}

	public LightHandlingBehaviourProperties withScanRange(int scanRange) {
		this.scanRange = scanRange;
		return this;
	}

	public LightHandlingBehaviourProperties withAbsorbsLight(boolean absorbsLight) {
		this.absorbsLight = absorbsLight;
		return this;
	}

	public LightHandlingBehaviourProperties withScansBeacons(boolean scansBeacon) {
		this.scansBeacon = scansBeacon;
		return this;
	}
}
