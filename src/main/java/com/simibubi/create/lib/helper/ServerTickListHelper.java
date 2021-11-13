package com.simibubi.create.lib.helper;

import java.util.Set;

import com.simibubi.create.lib.mixin.accessor.ServerTickListAccessor;

import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;

public class ServerTickListHelper {
	public static <T> Set<TickNextTickData<T>> getPendingTickListEntries(ServerTickList<T> list) {
		return ((ServerTickListAccessor<T>) list).getTickNextTickSet();
	}
}
