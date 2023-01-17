package com.simibubi.create.foundation.gui.menu;

import com.simibubi.create.foundation.networking.AllPackets;

public interface IClearableMenu {

	default void sendClearPacket() {
		AllPackets.channel.sendToServer(new ClearMenuPacket());
	}

	public void clearContents();

}
