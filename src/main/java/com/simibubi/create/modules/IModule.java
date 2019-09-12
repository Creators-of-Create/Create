package com.simibubi.create.modules;

import com.simibubi.create.CreateConfig;

public interface IModule {

	public static boolean isActive(String module) {
		if (module.equals("materials"))
			return true;

		CreateConfig conf = CreateConfig.parameters;
		switch (module) {
		case "contraptions":
			return conf.enableContraptions.get();
		case "palettes":
			return conf.enablePalettes.get();
		case "curiosities":
			return conf.enableCuriosities.get();
		case "logistics":
			return conf.enableLogistics.get();
		case "schematics":
			return conf.enableSchematics.get();
		case "gardens":
			return conf.enableGardens.get();
		default:
			return false;
		}
	}

	public default boolean isEnabled() {
		return isActive(getModuleName());
	}

	public String getModuleName();

}
