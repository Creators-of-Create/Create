package com.simibubi.create.foundation.utility;

public class NBTHelper {

	public static <T extends Enum<?>> T readEnum(String name, Class<T> enumClass) {
		T[] enumConstants = enumClass.getEnumConstants();
		if (enumConstants == null)
			throw new IllegalArgumentException("Non-Enum class passed to readEnum(): " + enumClass.getName());
		for (T t : enumConstants) {
			if (t.name().equals(name))
				return t;
		}
		return enumConstants[0];
	}

	public static <T extends Enum<?>> String writeEnum(T enumConstant) {
		return enumConstant.name();
	}

}
