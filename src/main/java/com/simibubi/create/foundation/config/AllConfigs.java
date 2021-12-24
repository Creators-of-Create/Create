package com.simibubi.create.foundation.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.BlockStressValues;

import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class AllConfigs {

	private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

	public static CClient CLIENT;
	public static CCommon COMMON;
	public static CServer SERVER;

	public static ConfigBase byType(ModConfig.Type type) {
		return CONFIGS.get(type);
	}

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(side, config);
		return config;
	}

	public static void register() {
		CLIENT = register(CClient::new, ModConfig.Type.CLIENT);
		COMMON = register(CCommon::new, ModConfig.Type.COMMON);
		SERVER = register(CServer::new, ModConfig.Type.SERVER);

		for (Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
			ModLoadingContext.registerConfig(Create.ID, pair.getKey(), pair.getValue().specification);

		BlockStressValues.registerProvider(Create.ID, SERVER.kinetics.stressValues);

		ModConfigEvent.LOADING.register(AllConfigs::onLoad);
		ModConfigEvent.RELOADING.register(AllConfigs::onReload);
	}

	public static void onLoad(ModConfig modConfig) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == modConfig
				.getSpec())
				config.onLoad();
	}

	public static void onReload(ModConfig modConfig) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == modConfig
				.getSpec())
				config.onReload();
	}

}
