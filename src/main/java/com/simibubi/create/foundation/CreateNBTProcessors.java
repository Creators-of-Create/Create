package com.simibubi.create.foundation;

import java.util.List;

import com.simibubi.create.AllBlockEntityTypes;

import net.createmod.catnip.nbt.NBTHelper;
import net.createmod.catnip.nbt.NBTProcessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CreateNBTProcessors {
	public static void register() {
		// Remove the first layer of commands while preserving the styles.
		// Since recursive commands won't be executed, there's no need to handle them.
		NBTProcessors.addProcessor(BlockEntityType.SIGN, data -> {
			var front_text = data.getCompound("front_text").getList("messages", Tag.TAG_STRING);
			var back_text = data.getCompound("back_text").getList("messages", Tag.TAG_STRING);
			for (int i = 0; i < 4; ++i) {
				tryRemovingCommand(front_text, i);
				tryRemovingCommand(back_text, i);
			}
			return data;
		});
		NBTProcessors.addProcessor(AllBlockEntityTypes.PLACARD.get(), NBTProcessors.itemProcessor("Item"));
	}

	private static void tryRemovingCommand(ListTag front_text, int i) {
		if(NBTProcessors.textComponentHasClickEvent(front_text.get(i).getAsString()))
		{
			var text =
				Component.Serializer.fromJson(front_text.get(i).getAsString());
			if (text != null) {
				text.setStyle(text.getStyle().withClickEvent(null));
				front_text.remove(i);
				front_text.add(i,net.minecraft.nbt.StringTag.valueOf(Component.Serializer.toJson(text)));
			}
		}
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
