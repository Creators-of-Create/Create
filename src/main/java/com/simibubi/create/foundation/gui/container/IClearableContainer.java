package com.simibubi.create.foundation.gui.container;

import com.simibubi.create.foundation.networking.AllPackets;

public interface IClearableContainer {

	default void sendClearPacket() {
		AllPackets.channel.sendToServer(new ClearContainerPacket());
	}

	public void clearContents();

}
