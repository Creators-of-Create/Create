package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

public class AddedByAttribute implements ItemAttribute {
	private String modId;

	public AddedByAttribute(String modId) {
		this.modId = modId;
	}

	@Override
	public boolean appliesTo(ItemStack stack, Level world) {
		return modId.equals(stack.getItem()
				.getCreatorModId(stack));
	}

	@Override
	public String getTranslationKey() {
		return "added_by";
	}

	@Override
	public Object[] getTranslationParameters() {
		Optional<? extends ModContainer> modContainerById = ModList.get()
				.getModContainerById(modId);
		String name = modContainerById.map(ModContainer::getModInfo)
				.map(IModInfo::getDisplayName)
				.orElse(StringUtils.capitalize(modId));
		return new Object[]{name};
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putString("id", modId);
	}

	@Override
	public void load(CompoundTag nbt) {
		modId = nbt.getString("id");
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.ADDED_BY.get();
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new AddedByAttribute("dummy");
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			String id = stack.getItem()
					.getCreatorModId(stack);
			return id == null ? Collections.emptyList() : List.of(new AddedByAttribute(id));
		}
	}
}
