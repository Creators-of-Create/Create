package com.simibubi.create.compat.computercraft.events;

import org.jetbrains.annotations.NotNull;


import com.simibubi.create.content.trains.entity.Train;

public class StationTrainPresenceEvent implements ComputerEvent {

	public enum Type {
		IMMINENT("train_imminent"),
		ARRIVAL("train_arrival"),
		DEPARTURE("train_departure");

		public final String name;

		Type(String name) {
			this.name = name;
		}
	}

	public Type type;
	public @NotNull Train train;

	public StationTrainPresenceEvent(Type type, @NotNull Train train) {
		this.type = type;
		this.train = train;
	}

}
