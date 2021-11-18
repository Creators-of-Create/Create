package com.simibubi.create.foundation.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.simibubi.create.Create;
import com.simibubi.create.lib.config.ConfigType;

import com.simibubi.create.foundation.block.BlockStressValues;

public class AllConfigs {

	private static final Map<ConfigType, ConfigBase> CONFIGS = new EnumMap<>(ConfigType.class);

	public static CClient CLIENT;
	public static CCommon COMMON;
	public static CServer SERVER;

	public static ConfigBase byType(ConfigType type) {
		return CONFIGS.get(type);
	}

	private static <T extends ConfigBase> T register(Supplier<T> factory, ConfigType side) {
		ConfigSpec configSpec = new ConfigSpec();
		T config = factory.get();
		config.registerAll(configSpec);
		config.specification = configSpec;
		CONFIGS.put(side, config);
		return config;
	}

	public static void register() {
		CLIENT = register(CClient::new, ConfigType.CLIENT);
		COMMON = register(CCommon::new, ConfigType.COMMON);
		SERVER = register(CServer::new, ConfigType.SERVER);

		BlockStressValues.registerProvider(Create.ID, SERVER.kinetics.stressValues);
	}

	public static void onLoad() {
		for (ConfigBase config : CONFIGS.values())
			config.onLoad();
	}

	public static void onReload() {
		for (ConfigBase config : CONFIGS.values())
			config.onReload();
	}

}
