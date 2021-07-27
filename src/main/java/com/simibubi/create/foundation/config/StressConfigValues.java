package com.simibubi.create.foundation.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class StressConfigValues {

	private static final Map<String, IStressValueProvider> PROVIDERS = new HashMap<>();

	public static void registerProvider(String namespace, IStressValueProvider provider) {
		PROVIDERS.put(namespace, provider);
	}

	@Nullable
	public static IStressValueProvider getProvider(String namespace) {
		return PROVIDERS.get(namespace);
	}

	@Nullable
	public static IStressValueProvider getProvider(Block block) {
		ResourceLocation key = block.getRegistryName();
		String namespace = key.getNamespace();
		IStressValueProvider provider = getProvider(namespace);
		return provider;
	}

	public static double getImpact(Block block) {
		IStressValueProvider provider = getProvider(block);
		if (provider != null) {
			return provider.getImpact(block);
		}
		return 0;
	}

	public static double getCapacity(Block block) {
		IStressValueProvider provider = getProvider(block);
		if (provider != null) {
			return provider.getCapacity(block);
		}
		return 0;
	}

	public static boolean hasImpact(Block block) {
		IStressValueProvider provider = getProvider(block);
		if (provider != null) {
			return provider.hasImpact(block);
		}
		return false;
	}

	public static boolean hasCapacity(Block block) {
		IStressValueProvider provider = getProvider(block);
		if (provider != null) {
			return provider.hasCapacity(block);
		}
		return false;
	}

	public interface IStressValueProvider {
		/**
		 * Gets the impact of a block.
		 * 
		 * @param block The block.
		 * @return the impact value of the block, or 0 if it does not have one.
		 */
		double getImpact(Block block);

		/**
		 * Gets the capacity of a block.
		 * 
		 * @param block The block.
		 * @return the capacity value of the block, or 0 if it does not have one.
		 */
		double getCapacity(Block block);

		boolean hasImpact(Block block);

		boolean hasCapacity(Block block);
	}

}
