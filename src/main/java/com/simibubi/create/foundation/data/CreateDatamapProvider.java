package com.simibubi.create.foundation.data;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.datamaps.Fillable;
import com.simibubi.create.api.registry.CreateDataMaps;
import com.simibubi.create.foundation.block.CopperRegistries;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;

public class CreateDatamapProvider extends DataMapProvider {
	public CreateDatamapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, lookupProvider);
	}

	@Override
	protected void gather(Provider provider) {
		final Builder<Oxidizable, Block> oxidizables = builder(NeoForgeDataMaps.OXIDIZABLES);
		CopperRegistries.getWeatheringView().forEach((now, after) -> add(oxidizables, now, new Oxidizable(after.value())));

		final Builder<Waxable, Block> waxables = builder(NeoForgeDataMaps.WAXABLES);
		CopperRegistries.getWaxableView().forEach((now, after) -> add(waxables, now, new Waxable(after.value())));

		final Builder<Fillable, Item> fillables = builder(CreateDataMaps.FILLABLE);
		add(fillables, BuiltInRegistries.ITEM.wrapAsHolder(Items.GLASS_BOTTLE), new Fillable(Map.of(
			BuiltInRegistries.FLUID.getResourceKey(AllFluids.TEA.getSource()).orElseThrow(), new Fillable.Entry(250, AllItems.BUILDERS_TEA.getKey())
		)));
	}

	public static <T, I> void add(Builder<T, I> b, Holder<I> now, T after) {
		b.add(now, after, false);
	}
}
