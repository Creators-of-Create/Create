package com.simibubi.create.content.logistics.filter;

import com.simibubi.create.content.logistics.filter.AttributeFilterMenu.WhitelistMode;
import com.simibubi.create.content.logistics.filter.FilterItemStack.AttributeFilterItemStack;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.attributes.InTagAttribute;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttributeFilterItem extends FilterItem {
	protected AttributeFilterItem(Properties properties) {
		super(properties);
	}

	@Override
	public List<Component> makeSummary(ItemStack filter) {
		if (!filter.hasTag()) return Collections.emptyList();

		List<Component> list = new ArrayList<>();

		WhitelistMode whitelistMode = WhitelistMode.values()[filter.getOrCreateTag()
			.getInt("WhitelistMode")];
		list.add((whitelistMode == WhitelistMode.WHITELIST_CONJ
			? CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive")
			: whitelistMode == WhitelistMode.WHITELIST_DISJ
			? CreateLang.translateDirect("gui.attribute_filter.allow_list_disjunctive")
			: CreateLang.translateDirect("gui.attribute_filter.deny_list")).withStyle(ChatFormatting.GOLD));

		int count = 0;
		ListTag attributes = filter.getOrCreateTag()
			.getList("MatchedAttributes", Tag.TAG_COMPOUND);
		for (Tag inbt : attributes) {
			CompoundTag compound = (CompoundTag) inbt;
			ItemAttribute attribute = ItemAttribute.loadStatic(compound);
			if (attribute == null)
				continue;
			boolean inverted = compound.getBoolean("Inverted");
			if (count > 3) {
				list.add(Component.literal("- ...")
					.withStyle(ChatFormatting.DARK_GRAY));
				break;
			}
			list.add(Component.literal("- ")
				.append(attribute.format(inverted)));
			count++;
		}

		if (count == 0)
			return Collections.emptyList();

		return list;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return AttributeFilterMenu.create(id, inv, player.getMainHandItem());
	}

	@Override
	public FilterItemStack makeStackWrapper(ItemStack filter) {
		return new AttributeFilterItemStack(filter);
	}

	@Override
	public ItemStack[] getFilterItems(ItemStack itemStack) {
		CompoundTag tag = itemStack.getOrCreateTag();

		WhitelistMode whitelistMode = WhitelistMode.values()[tag.getInt("WhitelistMode")];
		ListTag attributes = tag.getList("MatchedAttributes", net.minecraft.nbt.Tag.TAG_COMPOUND);

		if (whitelistMode == WhitelistMode.WHITELIST_DISJ && attributes.size() == 1) {
			ItemAttribute fromNBT = ItemAttribute.loadStatic((CompoundTag) attributes.get(0));
			if (fromNBT instanceof InTagAttribute inTag) {
				ITagManager<Item> tagManager = ForgeRegistries.ITEMS.tags();
				if (tagManager.isKnownTagName(inTag.tag)) {
					ITag<Item> taggedItems = tagManager.getTag(inTag.tag);
					if (!taggedItems.isEmpty()) {
						ItemStack[] stacks = new ItemStack[taggedItems.size()];
						int i = 0;
						for (Item item : taggedItems) {
							stacks[i] = new ItemStack(item);
							i++;
						}
						return stacks;
					}
				}
			}
		}
		return new ItemStack[0];
	}
}
