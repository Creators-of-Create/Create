package com.simibubi.create.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.simibubi.create.AllHeatConditions;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;

import com.simibubi.create.api.registry.CreateRegistries;

import com.simibubi.create.content.processing.recipe.EmptyHeatCondition;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A HeatCondition is a recipe condition such as the requirement of being heated or superheated.
 * This is demonstrated with the Blaze Burner.
 * <p>
 * This interface provides integration with JEI through {@link #getItemHints()} and {@link #getColor()}.
 * To make your heat source render within Create's categories, add your IDrawable to {@link com.simibubi.create.compat.jei.CreateJEI#HEAT_CONDITION_DRAWABLES}.
 * You should do this from your JEI plugin's onRuntimeAvailable method.
 * </p>
 */
public interface HeatCondition {
	HeatCondition NONE = new EmptyHeatCondition();
	Codec<HeatCondition> CODEC = Util.make(() -> {
		Codec<HeatCondition> byLegacyName = Codec.STRING.flatXmap(string -> switch (string) {
			case "none" -> DataResult.success(HeatCondition.NONE);
			case "heated" -> DataResult.success(AllHeatConditions.HEATED);
			case "superheated" -> DataResult.success(AllHeatConditions.SUPERHEATED);
			default -> DataResult.error(() -> "Not a legacy name");
		}, condition -> DataResult.error(() -> "Cannot encode by legacy name"));

		return Codec.withAlternative(CreateBuiltInRegistries.HEAT_CONDITION.byNameCodec(), byLegacyName);
	});
	StreamCodec<RegistryFriendlyByteBuf, HeatCondition> STREAM_CODEC = ByteBufCodecs.registry(CreateRegistries.HEAT_CONDITION);

	/**
	 * Tests the HeatCondition's criteria against a position.
	 * @param level The level in which the test is located in.
	 * @param testPos The position of the block entity to be heated (e.g. a Basin)
	 * @return Whether the HeatCondition is valid for this position.
	 */
	boolean test(Level level, BlockPos testPos);

	/**
	 * @return The HeatingCondition's translation key.
	 */
	String getTranslationKey();

	/**
	 * Provides items associated with this HeatCondition (e.g. a Blaze Burner)
	 */
	@NotNull
	default List<ItemStack> getItemHints() {
		return List.of();
	}

	/**
	 * The color of the HeatCondition when shown in JEI.
	 */
	default int getColor() {
		return 0xffffff;
	}

	@Internal
	default boolean isEmpty() { return this == NONE; }
}
