package com.simibubi.create;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.impl.effect.LavaEffectHandler;
import com.simibubi.create.impl.effect.MilkEffectHandler;
import com.simibubi.create.impl.effect.PotionEffectHandler;
import com.simibubi.create.impl.effect.TeaEffectHandler;
import com.simibubi.create.impl.effect.WaterEffectHandler;

import net.minecraft.tags.FluidTags;

import net.neoforged.neoforge.common.Tags;

public class AllOpenPipeEffectHandlers {
	public static void registerDefaults() {
		OpenPipeEffectHandler.REGISTRY.registerProvider(SimpleRegistry.Provider.forFluidTag(FluidTags.WATER, new WaterEffectHandler()));
		OpenPipeEffectHandler.REGISTRY.registerProvider(SimpleRegistry.Provider.forFluidTag(FluidTags.LAVA, new LavaEffectHandler()));
		OpenPipeEffectHandler.REGISTRY.registerProvider(SimpleRegistry.Provider.forFluidTag(Tags.Fluids.MILK, new MilkEffectHandler()));
		OpenPipeEffectHandler.REGISTRY.register(AllFluids.POTION.getSource(), new PotionEffectHandler());
		OpenPipeEffectHandler.REGISTRY.register(AllFluids.TEA.getSource(), new TeaEffectHandler());
	}
}
