package com.simibubi.create.foundation.utility;

import org.jetbrains.annotations.NotNull;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

public final class RegisteredObjects {
	// registry argument for easier porting to 1.19
	@NotNull
	public static <V> ResourceLocation getKeyOrThrow(IForgeRegistry<V> registry, V value) {
		ResourceLocation key = registry.getKey(value);
		if (key == null) {
			throw new IllegalArgumentException("Could not get key for value " + value + "!");
		}
		return key;
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(Block value) {
		return getKeyOrThrow(ForgeRegistries.BLOCKS, value);
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(Item value) {
		return getKeyOrThrow(ForgeRegistries.ITEMS, value);
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(Fluid value) {
		return getKeyOrThrow(ForgeRegistries.FLUIDS, value);
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(EntityType<?> value) {
		return getKeyOrThrow(ForgeRegistries.ENTITY_TYPES, value);
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(BlockEntityType<?> value) {
		return getKeyOrThrow(ForgeRegistries.BLOCK_ENTITY_TYPES, value);
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(Potion value) {
		return getKeyOrThrow(ForgeRegistries.POTIONS, value);
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(ParticleType<?> value) {
		return getKeyOrThrow(ForgeRegistries.PARTICLE_TYPES, value);
	}

	@NotNull
	public static ResourceLocation getKeyOrThrow(RecipeSerializer<?> value) {
		return getKeyOrThrow(ForgeRegistries.RECIPE_SERIALIZERS, value);
	}
}
