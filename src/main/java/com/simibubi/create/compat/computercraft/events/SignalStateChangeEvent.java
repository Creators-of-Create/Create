package com.simibubi.create.compat.computercraft.events;

import com.simibubi.create.content.trains.signal.SignalBlockEntity;

public class SignalStateChangeEvent implements ComputerEvent {

	public SignalBlockEntity.SignalState state;

	public SignalStateChangeEvent(SignalBlockEntity.SignalState state) {
		this.state = state;
	}

}
