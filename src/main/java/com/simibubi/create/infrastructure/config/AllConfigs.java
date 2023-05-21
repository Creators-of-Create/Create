package com.simibubi.create.infrastructure.config;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.content.kinetics.BlockStressValues;
import com.simibubi.create.foundation.config.ConfigBase;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class AllConfigs {

	private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

	private static CClient client;
	private static CCommon common;
	private static CServer server;

	public static CClient client() {
		return client;
	}

	public static CCommon common() {
		return common;
	}

	public static CServer server() {
		return server;
	}

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

	public static void register(ModLoadingContext context) {
		client = register(CClient::new, ModConfig.Type.CLIENT);
		common = register(CCommon::new, ModConfig.Type.COMMON);
		server = register(CServer::new, ModConfig.Type.SERVER);

		for (Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
			context.registerConfig(pair.getKey(), pair.getValue().specification);

		BlockStressValues.registerProvider(context.getActiveNamespace(), server().kinetics.stressValues);
	}

	@SubscribeEvent
	public static void onLoad(ModConfigEvent.Loading event) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == event.getConfig()
				.getSpec())
				config.onLoad();
	}

	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading event) {
		for (ConfigBase config : CONFIGS.values())
			if (config.specification == event.getConfig()
				.getSpec())
				config.onReload();
	}

}
