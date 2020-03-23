package com.simibubi.create.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;

public class AllConfigs {

	static List<Pair<ConfigBase, ModConfig.Type>> configs = new ArrayList<>();

	public static CClient CLIENT = register(CClient::new, ModConfig.Type.CLIENT);
	public static CCommon COMMON = register(CCommon::new, ModConfig.Type.COMMON);
	public static CServer SERVER = register(CServer::new, ModConfig.Type.SERVER);

	private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
		Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});

		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		configs.add(Pair.of(config, side));
		return config;
	}

	public static void registerAll() {
		ModLoadingContext ctx = ModLoadingContext.get();
		for (Pair<ConfigBase, Type> pair : configs)
			ctx.registerConfig(pair.getValue(), pair.getKey().specification);
	}

	public static void onLoad(ModConfig.Loading event) {
		for (Pair<ConfigBase, Type> pair : configs)
			if (pair.getKey().specification == event.getConfig().getSpec())
				pair.getKey().onLoad();
	}

	public static void onReload(ModConfig.Reloading event) {
		for (Pair<ConfigBase, Type> pair : configs)
			if (pair.getKey().specification == event.getConfig().getSpec())
				pair.getKey().onReload();
	}
}
