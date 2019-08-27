package com.simibubi.create.modules.logistics;

public interface ITransmitWireless extends IHaveWireless {

	public boolean getSignal();
	public default void notifySignalChange() {
		FrequencyHandler.updateNetworkOf(this);
	}
	
}
