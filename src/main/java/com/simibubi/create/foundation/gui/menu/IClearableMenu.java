package com.simibubi.create.foundation.gui.menu;

import net.createmod.catnip.platform.CatnipServices;

public interface IClearableMenu {

	default void sendClearPacket() {
		CatnipServices.NETWORK.sendToServer(ClearMenuPacket.INSTANCE);
	}

	void clearContents();

}
