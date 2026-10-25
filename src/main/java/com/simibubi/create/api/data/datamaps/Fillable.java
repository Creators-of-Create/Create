package com.simibubi.create.api.data.datamaps;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Fillable(Map<ResourceKey<Fluid>, Entry> fillWith) {
	public static final Codec<Fillable> CODEC = RecordCodecBuilder.create(instance ->
		instance.group(
			Codec.unboundedMap(ResourceKey.codec(Registries.FLUID), Entry.CODEC).fieldOf("fill_with").forGetter(Fillable::fillWith)
		).apply(instance, Fillable::new)
	);

	public record Entry(int amount, ResourceKey<Item> result) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
				ExtraCodecs.POSITIVE_INT.fieldOf("amount").forGetter(Entry::amount),
				ResourceKey.codec(Registries.ITEM).fieldOf("result").forGetter(Entry::result)
			).apply(instance, Entry::new)
		);
	}

	@ParametersAreNonnullByDefault
	@MethodsReturnNonnullByDefault
	public static class Merger implements DataMapValueMerger<Item, Fillable> {
		@Override
		public Fillable merge(
			Registry<Item> registry,
			Either<TagKey<Item>, ResourceKey<Item>> first,
			Fillable firstValue,
			Either<TagKey<Item>, ResourceKey<Item>> second,
			Fillable secondValue
		) {
			final var map = new HashMap<>(firstValue.fillWith);
			map.putAll(secondValue.fillWith);
			return new Fillable(map);
		}
	}

	@ParametersAreNonnullByDefault
	@MethodsReturnNonnullByDefault
	public record Remover(List<ResourceKey<Fluid>> fluids) implements DataMapValueRemover<Item, Fillable> {
		public static final Codec<Remover> CODEC = Codec.withAlternative(
			Codec.list(ResourceKey.codec(Registries.FLUID)),
			ResourceKey.codec(Registries.FLUID).xmap(List::of, List::getFirst)
		).xmap(Remover::new, Remover::fluids);

		@Override
		public Optional<Fillable> remove(Fillable fillable, Registry<Item> registry, Either<TagKey<Item>, ResourceKey<Item>> source, Item item) {
			final var map = new HashMap<>(fillable.fillWith);
			for (final var fluid : fluids) map.remove(fluid);
			return Optional.of(new Fillable(map));
		}
	}
}
