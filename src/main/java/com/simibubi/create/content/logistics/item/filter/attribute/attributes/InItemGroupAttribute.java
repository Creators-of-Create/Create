package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.CreativeModeTabRegistry;

public class InItemGroupAttribute implements ItemAttribute {
	public static final InItemGroupAttribute EMPTY = new InItemGroupAttribute(null);

	private CreativeModeTab group;

	public InItemGroupAttribute(CreativeModeTab group) {
		this.group = group;
	}

	private static boolean tabContainsItem(CreativeModeTab tab, ItemStack stack) {
		return tab.contains(stack) || tab.contains(new ItemStack(stack.getItem()));
	}

	@Override
	public boolean appliesTo(ItemStack stack, Level world) {
		if (group == null)
			return false;

		if (group.getDisplayItems()
				.isEmpty()
				&& group.getSearchTabDisplayItems()
				.isEmpty()) {

			try {
				group.buildContents(new CreativeModeTab.ItemDisplayParameters(world.enabledFeatures(), false,
						world.registryAccess()));
			} catch (RuntimeException | LinkageError e) {
				Create.LOGGER.error("Attribute Filter: Item Group {} crashed while building contents.",
						group.getDisplayName().getString(), e);
				group = null;
				return false;
			}

		}

		return tabContainsItem(group, stack);
	}

	@Override
	public String getTranslationKey() {
		return "in_item_group";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{group == null ? "<none>" : group.getDisplayName().getString()};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.IN_ITEM_GROUP;
	}

	@Override
	public void save(CompoundTag nbt) {
		if (group != null) {
			ResourceLocation groupId = CreativeModeTabRegistry.getName(group);

			if (groupId != null) {
				nbt.putString("group", groupId.toString());
			}
		}
	}

	@Override
	public void load(CompoundTag nbt) {
		if (nbt.contains("group")) {
			group = CreativeModeTabRegistry.getTab(new ResourceLocation(nbt.getString("group")));
		}
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return EMPTY;
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			for (CreativeModeTab tab : CreativeModeTabs.tabs()) {
				if (tab.getType() == CreativeModeTab.Type.CATEGORY && tabContainsItem(tab, stack)) {
					list.add(new InItemGroupAttribute(tab));
				}
			}

			return list;
		}
	}
}
