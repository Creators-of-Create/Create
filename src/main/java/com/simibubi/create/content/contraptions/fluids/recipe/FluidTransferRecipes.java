package com.simibubi.create.content.contraptions.fluids.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

public class FluidTransferRecipes {

	public static List<ItemStack> POTION_ITEMS = new ArrayList<>();
	public static List<Item> FILLED_BUCKETS = new ArrayList<>();

	
	
	public static final ReloadListener<Object> LISTENER = new ReloadListener<Object>() {

		@Override
		protected Object prepare(IResourceManager p_212854_1_, IProfiler p_212854_2_) {
			return new Object();
		}

		@Override
		protected void apply(Object p_212853_1_, IResourceManager p_212853_2_, IProfiler p_212853_3_) {
			POTION_ITEMS.clear();
			FILLED_BUCKETS.clear();
		}

	};
}
