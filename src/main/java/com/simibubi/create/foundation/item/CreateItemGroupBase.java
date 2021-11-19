package com.simibubi.create.foundation.item;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Collectors;

import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.lib.utility.ItemGroupUtil;
import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import static com.simibubi.create.lib.utility.ItemGroupUtil.getGroupCountSafe;

public abstract class CreateItemGroupBase extends CreativeModeTab {

	public CreateItemGroupBase(String id) {
		super(ItemGroupUtil.getGroupCountSafe(), Create.ID + "." + id);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void fillItemList(NonNullList<ItemStack> items) {
		addItems(items, true);
		addBlocks(items);
		addItems(items, false);
	}

	@Environment(EnvType.CLIENT)
	public void addBlocks(NonNullList<ItemStack> items) {
		for (RegistryEntry<? extends Block> entry : getBlocks()) {
			Block def = entry.get();
			Item item = def.asItem();
			if (item != Items.AIR)
				def.fillItemCategory(this, items);
		}
	}

	@Environment(EnvType.CLIENT)
	public void addItems(NonNullList<ItemStack> items, boolean specialItems) {
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();
		ClientLevel world = mc.level;

		for (RegistryEntry<? extends Item> entry : getItems()) {
			Item item = entry.get();
			if (item instanceof BlockItem)
				continue;
			ItemStack stack = new ItemStack(item);
			BakedModel model = itemRenderer.getModel(stack, world, null, 0);
			if (model.isGui3d() != specialItems)
				continue;
			item.fillItemCategory(this, items);
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
