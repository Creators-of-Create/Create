package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.ServerGamePacketListenerImplAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class ServerPlayNetHandlerHelper {
	public static int getFloatingTickCount(ServerGamePacketListenerImpl handler) {
		return get(handler).create$floatingTickCount();
	}

	public static void setFloatingTickCount(ServerGamePacketListenerImpl handler, int floatingTickCount) {
		get(handler).create$floatingTickCount(floatingTickCount);
	}

	private static ServerGamePacketListenerImplAccessor get(ServerGamePacketListenerImpl handler) {
		return MixinHelper.cast(handler);
	}

	private ServerPlayNetHandlerHelper() {}
}
