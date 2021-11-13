package com.simibubi.create.lib.utility;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MethodGetter {
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * remember, this is a Fabric mod, you need intermediary for obfuscatedName, not SRG
	 */
	public static Method findMethod(Class<?> clas, String methodName, String obfuscatedName, Class<?>... parameterTypes) {
		Method method;
		try {
			// obfuscated
			method = clas.getMethod(obfuscatedName, parameterTypes);

		} catch (NoSuchMethodException e) {
			// un-obfuscated
			try {
				method = clas.getMethod(methodName, parameterTypes);
			} catch (NoSuchMethodException ex) {
				LOGGER.fatal("No method with the provided name or obfuscated name found!");
				throw new RuntimeException(ex);
			}
		}
		return method;
	}
}
