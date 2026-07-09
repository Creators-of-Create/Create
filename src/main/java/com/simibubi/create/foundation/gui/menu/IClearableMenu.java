package com.simibubi.create.foundation.gui.menu;

import net.createmod.catnip.api.platform.CatnipServices;

public interface IClearableMenu {

	default void sendClearPacket() {
		net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(ClearMenuPacket.INSTANCE);
	}

	void clearContents();

}
