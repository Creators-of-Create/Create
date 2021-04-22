package com.simibubi.create.content.optics.behaviour;

public class LightHandlingbehaviourProperties {
	int scanRange;
	boolean absorbsLight;
	boolean scansBeacon;

	private LightHandlingbehaviourProperties() {
		scanRange = 128;
		absorbsLight = true;
		scansBeacon = true;
	}

	public static LightHandlingbehaviourProperties create() {
		return new LightHandlingbehaviourProperties();
	}

	public LightHandlingbehaviourProperties withScanRange(int scanRange) {
		this.scanRange = scanRange;
		return this;
	}

	public LightHandlingbehaviourProperties withAbsorbsLight(boolean absorbsLight) {
		this.absorbsLight = absorbsLight;
		return this;
	}

	public LightHandlingbehaviourProperties withScansBeacons(boolean scansBeacon) {
		this.scansBeacon = scansBeacon;
		return this;
	}
}
