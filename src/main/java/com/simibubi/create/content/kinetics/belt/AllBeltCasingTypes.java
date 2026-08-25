package com.simibubi.create.content.kinetics.belt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.Create;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import net.minecraft.core.Holder;

import net.minecraft.core.Registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class AllBeltCasingTypes {

	public static final BeltCasingType ANDESITE = register("andesite", new BeltCasingType(AllBlocks.ANDESITE_CASING,
		() -> new BeltCasingRenderInfo(
			AllPartialModels.ANDESITE_BELT_COVER_X,
			AllPartialModels.ANDESITE_BELT_COVER_Z,
			AllSpriteShifts.ANDESIDE_BELT_CASING
		)));

	public static final BeltCasingType BRASS = register("brass", new BeltCasingType(AllBlocks.BRASS_CASING,
		() -> new BeltCasingRenderInfo(
			AllPartialModels.BRASS_BELT_COVER_X,
			AllPartialModels.BRASS_BELT_COVER_Z,
			null
		)));

	private static BeltCasingType register(String name, BeltCasingType beltCasingType) {
		return Registry.register(CreateBuiltInRegistries.BELT_CASING_TYPE, Create.asResource(name), beltCasingType);
	}

	@Internal
	public static void init() {
	}

	/**
	 * Migrations for legacy CasingType enum, since new system uses resource keys.
	 * */
	public enum LegacyCasingType {
		NONE(null), ANDESITE(() -> AllBeltCasingTypes.ANDESITE), BRASS(() -> AllBeltCasingTypes.BRASS);

		@Nullable
		private final Supplier<BeltCasingType> casingType;

		LegacyCasingType(@Nullable Supplier<BeltCasingType> casingType) {
			this.casingType = casingType;
		}

		public BeltCasingType getCasingType() {
			return casingType == null ? null : casingType.get();
		}

		public static @Nullable BeltCasingType fromId(String id) {
			try {
				return LegacyCasingType.valueOf(id.toUpperCase()).getCasingType();
			} catch (IllegalArgumentException ignored) {
				return null;
			}
		}
	}

}
