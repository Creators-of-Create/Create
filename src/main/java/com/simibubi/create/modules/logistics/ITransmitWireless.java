package com.simibubi.create.modules.logistics;

import com.simibubi.create.Create;

public interface ITransmitWireless extends IHaveWireless {

	public boolean getSignal();
	public default void notifySignalChange() {
		Create.frequencyHandler.updateNetworkOf(this);
	}
	
}
