package com.simibubi.create;

import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllRegistries {
	public static Supplier<IForgeRegistry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPES;

	public static final class Keys {
		public static final ResourceKey<Registry<ItemAttributeType>> ITEM_ATTRIBUTE_TYPES = key("item_attribute_types");

		private static <T> ResourceKey<Registry<T>> key(String name) {
			return ResourceKey.createRegistryKey(Create.asResource(name));
		}
	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		ITEM_ATTRIBUTE_TYPES = event.create(new RegistryBuilder<ItemAttributeType>()
				.setName(Keys.ITEM_ATTRIBUTE_TYPES.location())
				.disableSaving());
	}
}
