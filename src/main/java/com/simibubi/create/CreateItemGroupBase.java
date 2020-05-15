package com.simibubi.create;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.simibubi.create.modules.Sections;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class CreateItemGroupBase extends ItemGroup {

	public CreateItemGroupBase(String id) {
		super(getGroupCountSafe(), Create.ID + "." + id);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void fill(NonNullList<ItemStack> items) {
		addItems(items, true);
		addBlocks(items);
		addItems(items, false);
	}

	@OnlyIn(Dist.CLIENT)
	public void addBlocks(NonNullList<ItemStack> items) {
		for (RegistryEntry<? extends Block> entry : getBlocks()) {
			Block def = entry.get();
			if (def == null)
				continue;
			Item item = def.asItem();
			if (item != Items.AIR)
				def.fillItemGroup(this, items);
		}
	}

	protected Collection<RegistryEntry<Block>> getBlocks() {
		return getSections().stream()
				.flatMap(s -> Create.registrate().getAll(s, Block.class).stream())
				.collect(Collectors.toList());
	}
	
	protected EnumSet<Sections> getSections() {
		return EnumSet.allOf(Sections.class);
	}

	@OnlyIn(Dist.CLIENT)
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
		ItemRenderer itemRenderer = Minecraft.getInstance()
				.getItemRenderer();

		for (AllItems item : AllItems.values()) {
			if (item.get() == null)
				continue;
			IBakedModel model =
				itemRenderer.getItemModelWithOverrides(item.asStack(), Minecraft.getInstance().world, null);
			if (model.isGui3d() != specialItems)
				continue;

			item.get()
					.fillItemGroup(this, items);
		}
	}
}
