package com.simibubi.create.foundation;

import com.simibubi.create.AllBlockEntityTypes;

import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CreateNBTProcessors {
	public static void register() {
		NBTProcessors.addProcessor(BlockEntityType.LECTERN, data -> {
			if (!data.contains("Book", Tag.TAG_COMPOUND))
				return data;
			CompoundTag book = data.getCompound("Book");

			// Writable books can't have click events, so they're safe to keep
			ResourceLocation writableBookResource = BuiltInRegistries.ITEM.getKey(Items.WRITABLE_BOOK);
			if (writableBookResource != BuiltInRegistries.ITEM.getDefaultKey() && book.getString("id").equals(writableBookResource.toString()))
				return data;

			if (!book.contains("tag", Tag.TAG_COMPOUND))
				return data;
			CompoundTag tag = book.getCompound("tag");

			if (!tag.contains("pages", Tag.TAG_LIST))
				return data;
			ListTag pages = tag.getList("pages", Tag.TAG_STRING);

			for (Tag inbt : pages) {
				if (NBTProcessors.textComponentHasClickEvent(inbt.getAsString()))
					return null;
			}
			return data;
		});

		NBTProcessors.addProcessor(AllBlockEntityTypes.CLIPBOARD.get(), CreateNBTProcessors::clipboardProcessor);

		NBTProcessors.addProcessor(AllBlockEntityTypes.CREATIVE_CRATE.get(), NBTProcessors.itemProcessor("Filter"));
	}

	public static CompoundTag clipboardProcessor(CompoundTag data) {
		if (!data.contains("Item", Tag.TAG_COMPOUND))
			return data;
		CompoundTag item = data.getCompound("Item");

		if (!item.contains("components", Tag.TAG_COMPOUND))
			return data;
		CompoundTag itemComponents = item.getCompound("components");

		if (!itemComponents.contains("create:clipboard_pages", Tag.TAG_LIST))
			return data;
		ListTag pages = itemComponents.getList("create:clipboard_pages", Tag.TAG_LIST);

		for (Tag page : pages) {
			if (!(page instanceof ListTag entries))
				return data;

			for (int i = 0; i < entries.size(); i++) {
				CompoundTag entry = entries.getCompound(i);

				if (NBTProcessors.textComponentHasClickEvent(entry.getCompound("text").getAsString()))
					return null;
			}
		}

		return data;
	}
}
