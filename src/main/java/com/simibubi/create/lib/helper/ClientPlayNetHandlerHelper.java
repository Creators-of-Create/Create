package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.ClientPacketListenerAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.client.multiplayer.ClientPacketListener;

public final class ClientPlayNetHandlerHelper {

	public static int getViewDistance(ClientPacketListener clientPlayNetHandler) {
		return get(clientPlayNetHandler).create$viewDistance();
	}

	private static ClientPacketListenerAccessor get(ClientPacketListener clientPlayNetHandler) {
		return MixinHelper.cast(clientPlayNetHandler);
	}

	private ClientPlayNetHandlerHelper() {}
}
