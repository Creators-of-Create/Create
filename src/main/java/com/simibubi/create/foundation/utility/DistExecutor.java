package com.simibubi.create.foundation.utility;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.createmod.catnip.api.platform.CatnipServices;
import net.neoforged.api.distmarker.Dist;

@ApiStatus.Internal
@Deprecated(forRemoval = true, since = "1.21")
public class DistExecutor {
	/**
	 * This will be removed once there is more time to refactor its uses, this is not considered API,
	 * it is only for internal use and legacy reasons, this WILL be removed shortly, and you should not rely on or copy this code
	 */
	@ApiStatus.Internal
	@Deprecated(forRemoval = true, since = "1.21")
	public static <T> T unsafeCallWhenOn(Dist dist, Supplier<Callable<T>> toRun) {
		if ((dist == Dist.CLIENT && CatnipServices.PLATFORM.getEnv().isClient())
			|| (dist == Dist.DEDICATED_SERVER && CatnipServices.PLATFORM.getEnv().isServer())) {
			try {
				return toRun.get().call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
