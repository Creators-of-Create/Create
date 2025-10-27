package com.simibubi.create.compat.computercraft.events;

import org.jetbrains.annotations.NotNull;


import com.simibubi.create.content.trains.entity.Train;

public class TrainPassEvent implements ComputerEvent {

	public @NotNull Train train;
	public boolean passing;

	public TrainPassEvent(@NotNull Train train, boolean passing) {
		this.train = train;
		this.passing = passing;
	}

}
