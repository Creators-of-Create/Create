package com.simibubi.create.api.registry;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.serialization.MapCodec;
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
import com.simibubi.create.content.logistics.packagePort.PackagePortTargetType;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import net.neoforged.neoforge.registries.RegistryBuilder;

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
	public static final Registry<PackagePortTargetType> PACKAGE_PORT_TARGET_TYPE = simple(CreateRegistries.PACKAGE_PORT_TARGET_TYPE);
	public static final Registry<MapCodec<? extends PotatoProjectileRenderMode>> POTATO_PROJECTILE_RENDER_MODE = simple(CreateRegistries.POTATO_PROJECTILE_RENDER_MODE);
	public static final Registry<MapCodec<? extends PotatoProjectileEntityHitAction>> POTATO_PROJECTILE_ENTITY_HIT_ACTION = simple(CreateRegistries.POTATO_PROJECTILE_ENTITY_HIT_ACTION);
	public static final Registry<MapCodec<? extends PotatoProjectileBlockHitAction>> POTATO_PROJECTILE_BLOCK_HIT_ACTION = simple(CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION);

	private static <T> Registry<T> simple(ResourceKey<Registry<T>> key) {
		return register(key, false, () -> {});
	}

	private static <T> Registry<T> simpleWithFreezeCallback(ResourceKey<Registry<T>> key, Runnable onBakeCallback) {
		return register(key, false, onBakeCallback);
	}

	private static <T> Registry<T> withIntrusiveHolders(ResourceKey<Registry<T>> key) {
		return register(key, true, () -> {});
	}

	@SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
	private static <T> Registry<T> register(ResourceKey<Registry<T>> key, boolean hasIntrusiveHolders, Runnable onBakeCallback) {
		RegistryBuilder<T> builder = new RegistryBuilder<>(key)
			.sync(true);

		if (hasIntrusiveHolders)
			builder.withIntrusiveHolders();

		builder.onBake(r -> onBakeCallback.run());

		Registry<T> registry = builder.create();
		((WritableRegistry) BuiltInRegistries.REGISTRY)
			.register(key, registry, RegistrationInfo.BUILT_IN);
		return registry;
	}

	@Internal
	public static void init() {
		// make sure the class is loaded.
		// this method is called at the tail of BuiltInRegistries, injected by BuiltInRegistriesMixin.
	}

	private CreateBuiltInRegistries() {
		throw new AssertionError("This class should not be instantiated");
	}
}
