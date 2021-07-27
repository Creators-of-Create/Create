package com.simibubi.create.foundation.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

public class AllConfigs {

	static Map<ConfigBase, ModConfig.Type> configs = new HashMap<>();

	public static CClient CLIENT;
	public static CCommon COMMON;
	public static CServer SERVER;

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		configs.put(config, side);
		return config;
	}

	public static void register(ModLoadingContext context) {
		CLIENT = register(CClient::new, ModConfig.Type.CLIENT);
		COMMON = register(CCommon::new, ModConfig.Type.COMMON);
		SERVER = register(CServer::new, ModConfig.Type.SERVER);

		for (Entry<ConfigBase, Type> pair : configs.entrySet())
			context.registerConfig(pair.getValue(), pair.getKey().specification);

		StressConfigValues.registerProvider(context.getActiveNamespace(), SERVER.kinetics.stressValues);
	}

	public static void onLoad(ModConfig.Loading event) {
		for (Entry<ConfigBase, Type> pair : configs.entrySet())
			if (pair.getKey().specification == event.getConfig()
				.getSpec())
				pair.getKey()
					.onLoad();
	}

	public static void onReload(ModConfig.Reloading event) {
		for (Entry<ConfigBase, Type> pair : configs.entrySet())
			if (pair.getKey().specification == event.getConfig()
				.getSpec())
				pair.getKey()
					.onReload();
	}
}
