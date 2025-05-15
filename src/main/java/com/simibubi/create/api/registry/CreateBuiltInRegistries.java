package com.simibubi.create.api.registry;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileEntityHitAction;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.mixin.accessor.BuiltInRegistriesAccessor;
import com.simibubi.create.impl.registry.MappedRegistryWithFreezeCallback;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;

/**
 * Static registries added by Create.
 *
 * @see CreateRegistries
 */
public class CreateBuiltInRegistries {
	public static final Registry<ArmInteractionPointType> ARM_INTERACTION_POINT_TYPE = simpleWithFreezeCallback(CreateRegistries.ARM_INTERACTION_POINT_TYPE, ArmInteractionPointType::init);
	public static final Registry<FanProcessingType> FAN_PROCESSING_TYPE = simpleWithFreezeCallback(CreateRegistries.FAN_PROCESSING_TYPE, FanProcessingTypeRegistry::init);
	public static final Registry<ItemAttributeType> ITEM_ATTRIBUTE_TYPE = simple(CreateRegistries.ITEM_ATTRIBUTE_TYPE);
	public static final Registry<DisplaySource> DISPLAY_SOURCE = simple(CreateRegistries.DISPLAY_SOURCE);
	public static final Registry<DisplayTarget> DISPLAY_TARGET = simple(CreateRegistries.DISPLAY_TARGET);
	public static final Registry<MountedItemStorageType<?>> MOUNTED_ITEM_STORAGE_TYPE = withIntrusiveHolders(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE);
	public static final Registry<MountedFluidStorageType<?>> MOUNTED_FLUID_STORAGE_TYPE = simple(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE);
	public static final Registry<ContraptionType> CONTRAPTION_TYPE = withIntrusiveHolders(CreateRegistries.CONTRAPTION_TYPE);
	public static final Registry<Codec<? extends PotatoProjectileRenderMode>> POTATO_PROJECTILE_RENDER_MODE = simple(CreateRegistries.POTATO_PROJECTILE_RENDER_MODE);
	public static final Registry<Codec<? extends PotatoProjectileEntityHitAction>> POTATO_PROJECTILE_ENTITY_HIT_ACTION = simple(CreateRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION);
	public static final Registry<Codec<? extends PotatoProjectileBlockHitAction>> POTATO_PROJECTILE_BLOCK_HIT_ACTION = simple(CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION);

	private static <T> Registry<T> simple(ResourceKey<Registry<T>> key) {
		return register(key, new MappedRegistry<>(key, Lifecycle.stable(), false));
	}

	private static <T> Registry<T> simpleWithFreezeCallback(ResourceKey<Registry<T>> key, Runnable freezeCallback) {
		return register(key, new MappedRegistryWithFreezeCallback<>(key, Lifecycle.stable(), freezeCallback));
	}

	private static <T> Registry<T> withIntrusiveHolders(ResourceKey<Registry<T>> key) {
		return register(key, new MappedRegistry<>(key, Lifecycle.stable(), true));
	}

	@SuppressWarnings("unchecked")
	private static <T> Registry<T> register(ResourceKey<Registry<T>> key, WritableRegistry<T> registry) {
		BuiltInRegistriesAccessor.create$getWRITABLE_REGISTRY().register(
			(ResourceKey<WritableRegistry<?>>) (Object) key, registry, Lifecycle.stable()
		);
		return registry;
	}

	@ApiStatus.Internal
	public static void init() {
		// make sure the class is loaded.
		// this method is called at the tail of BuiltInRegistries, injected by BuiltInRegistriesMixin.
	}
}
