package com.simibubi.create.foundation;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraftforge.registries.ForgeRegistries;

public class CreateNBTProcessors {
	public static void register() {
		NBTProcessors.addProcessor(BlockEntityType.LECTERN, data -> {
			if (!data.contains("Book", Tag.TAG_COMPOUND))
				return data;
			CompoundTag book = data.getCompound("Book");

			// Writable books can't have click events, so they're safe to keep
			ResourceLocation writableBookResource = ForgeRegistries.ITEMS.getKey(Items.WRITABLE_BOOK);
			if (writableBookResource != null && book.getString("id").equals(writableBookResource.toString()))
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
		CompoundTag book = data.getCompound("Item");

		if (!book.contains("tag", Tag.TAG_COMPOUND))
			return data;
		CompoundTag itemData = book.getCompound("tag");

		for (List<String> entries : NBTHelper.readCompoundList(itemData.getList("Pages", Tag.TAG_COMPOUND),
			pageTag -> NBTHelper.readCompoundList(pageTag.getList("Entries", Tag.TAG_COMPOUND),
				tag -> tag.getString("Text")))) {
			for (String entry : entries)
				if (NBTProcessors.textComponentHasClickEvent(entry))
					return null;
		}
		return data;
	}
}
