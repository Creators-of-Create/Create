package com.simibubi.create.foundation.recipe;

import com.simibubi.create.Create;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@EventBusSubscriber(bus = Bus.MOD)
public class AllIngredients {
	@SubscribeEvent
	public static void register(RegisterEvent event) {
		if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
			CraftingHelper.register(Create.asResource("block_tag_ingredient"), BlockTagIngredient.Serializer.INSTANCE);
		}
	}
}
