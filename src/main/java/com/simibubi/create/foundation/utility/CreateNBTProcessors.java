package com.simibubi.create.foundation.utility;

import com.simibubi.create.AllBlockEntityTypes;

import net.createmod.catnip.utility.NBTProcessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CreateNBTProcessors {

	public static void register() {

		NBTProcessors.addProcessor(BlockEntityType.SIGN, data -> {
			for (int i = 0; i < 4; ++i) {
				if (NBTProcessors.textComponentHasClickEvent(data.getString("Text" + (i + 1))))
					return null;
			}
			return data;
		});

		NBTProcessors.addProcessor(BlockEntityType.LECTERN, data -> {
			if (!data.contains("Book", Tag.TAG_COMPOUND))
				return data;
			CompoundTag book = data.getCompound("Book");

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

		NBTProcessors.addProcessor(AllBlockEntityTypes.CREATIVE_CRATE.get(), NBTProcessors.itemProcessor("Filter"));
		NBTProcessors.addProcessor(AllBlockEntityTypes.PLACARD.get(), NBTProcessors.itemProcessor("Item"));

	}

}
