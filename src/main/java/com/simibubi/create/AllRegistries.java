package com.simibubi.create;

import com.simibubi.create.content.logistics.trains.entity.BogeyStyle;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Create.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AllRegistries {
	static final DeferredRegister<BogeyStyle> DEFERRED_BOGEY_REGISTRY = DeferredRegister
			.create(Keys.BOGEYS, Keys.BOGEYS.location().getNamespace());

	public static final Supplier<IForgeRegistry<BogeyStyle>> BOGEY_REGISTRY = DEFERRED_BOGEY_REGISTRY
			.makeRegistry(BogeyStyle.class, AllRegistries::getBogeyRegistryBuilder);

	public static RegistryBuilder<BogeyStyle> getBogeyRegistryBuilder() {
		return makeRegistry(Keys.BOGEYS, BogeyStyle.class);
	}

	private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(ResourceKey<? extends Registry<T>> key, Class<T> type) {
		return new RegistryBuilder<T>().setName(key.location()).setType(type);
	}

	@SubscribeEvent
	public void onRegistryNewRegistry(final NewRegistryEvent event) {
		event.create(getBogeyRegistryBuilder());
	}

	public static class Keys {
		public static final ResourceKey<Registry<BogeyStyle>> BOGEYS = ResourceKey
				.createRegistryKey(new ResourceLocation(Create.ID, "bogeys"));
	}
}
