package com.simibubi.create.foundation;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.ClipboardContent;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;

import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
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

			WrittenBookContent bookContent = CatnipCodecUtils.decodeOrNull(WrittenBookContent.CODEC, book);
			if (bookContent == null)
				return data;

			for (Filterable<Component> page : bookContent.pages()) {
				if (NBTProcessors.textComponentHasClickEvent(page.get(false)))
					return null;
			}

			return data;
		});

		NBTProcessors.addProcessor(AllBlockEntityTypes.CLIPBOARD.get(), CreateNBTProcessors::clipboardProcessor);

		NBTProcessors.addProcessor(AllBlockEntityTypes.CREATIVE_CRATE.get(), NBTProcessors.itemProcessor("Filter"));
	}

	public static CompoundTag clipboardProcessor(CompoundTag data) {
		DataComponentMap components = CatnipCodecUtils.decodeOrNull(DataComponentMap.CODEC, data.getCompound("components"));
		if (components == null)
			return data;

		ClipboardContent content = components.get(AllDataComponents.CLIPBOARD_CONTENT);
		if (content == null)
			return data;

		for (List<ClipboardEntry> entries : content.pages()) {
			for (ClipboardEntry entry : entries) {
				if (NBTProcessors.textComponentHasClickEvent(entry.text))
					return null;
			}
		}

		return data;
	}
}
