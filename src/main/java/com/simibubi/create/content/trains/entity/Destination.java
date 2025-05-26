package com.simibubi.create.content.trains.entity;

import com.simibubi.create.content.trains.entry.GlobalEntry;
import com.simibubi.create.content.trains.platform.GlobalPlatform;
import com.simibubi.create.content.trains.station.GlobalStation;

public class Destination {
	public GlobalStation STATION;
	public GlobalPlatform PLATFORM;
	public GlobalEntry ENTRY;
	public Train TRAIN;
	public Navigation NAVIGATION;

	public Destination(Train train, Navigation navigation, GlobalStation station, GlobalPlatform platform, GlobalEntry entry) {
		TRAIN = train;
		NAVIGATION = navigation;
		STATION = station;
		PLATFORM = platform;
		ENTRY = entry;
	}

	public boolean reserve() {
		if (STATION == null) {
			return false;
		}

		if (PLATFORM != null) {
			PLATFORM.reserveFor(TRAIN);
			return true;
		}

		STATION.addImminentTrain(TRAIN);
		return true;
	}

	public boolean cancelReservation() {
		if (STATION == null) {
		return false;
	}

		if (PLATFORM != null) {
			PLATFORM.cancelReservation(TRAIN);
			return true;
		}

		STATION.removeImminentTrain(TRAIN);
		return true;
	}
}
