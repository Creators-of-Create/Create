package com.simibubi.create.foundation.item;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BlockItem;
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
			Item item = def.asItem();
			if (item != Items.AIR)
				def.fillItemGroup(this, items);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		ClientWorld world = mc.world;

		for (RegistryEntry<? extends Item> entry : getItems()) {
			Item item = entry.get();
			if (item instanceof BlockItem)
				continue;
			ItemStack stack = new ItemStack(item);
			IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, world, null);
			if (AllSections.of(stack) != AllSections.KINETICS) {
				if (specialItems)
					continue;
			} else if (model.isGui3d() != specialItems)
				continue;
			item.fillItemGroup(this, items);
		}
	}

	protected Collection<RegistryEntry<Block>> getBlocks() {
		return getSections().stream()
			.flatMap(s -> Create.registrate()
				.getAll(s, Block.class)
				.stream())
			.collect(Collectors.toList());
	}

	protected Collection<RegistryEntry<Item>> getItems() {
		return getSections().stream()
			.flatMap(s -> Create.registrate()
				.getAll(s, Item.class)
				.stream())
			.collect(Collectors.toList());
	}

	protected EnumSet<AllSections> getSections() {
		return EnumSet.allOf(AllSections.class);
	}
}
