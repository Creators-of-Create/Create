package com.simibubi.create.content.curiosities.clipboard;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ClipboardEntry {

	boolean checked;
	MutableComponent text;

	public ClipboardEntry(boolean checked, MutableComponent text) {
		this.checked = checked;
		this.text = text;
	}

	public static List<List<ClipboardEntry>> readAll(ItemStack clipboardItem) {
		CompoundTag tag = clipboardItem.getTag();
		if (tag == null)
			return new ArrayList<>();
		return NBTHelper.readCompoundList(tag.getList("Pages", Tag.TAG_COMPOUND), pageTag -> NBTHelper
			.readCompoundList(pageTag.getList("Entries", Tag.TAG_COMPOUND), ClipboardEntry::readNBT));
	}

	public static void saveAll(List<List<ClipboardEntry>> entries, ItemStack clipboardItem) {
		CompoundTag tag = clipboardItem.getOrCreateTag();
		tag.put("Pages", NBTHelper.writeCompoundList(entries, list -> {
			CompoundTag pageTag = new CompoundTag();
			pageTag.put("Entries", NBTHelper.writeCompoundList(list, ClipboardEntry::writeNBT));
			return pageTag;
		}));
	}

	public CompoundTag writeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("Checked", checked);
		nbt.putString("Text", Component.Serializer.toJson(text));
		return nbt;
	}

	public static ClipboardEntry readNBT(CompoundTag tag) {
		return new ClipboardEntry(tag.getBoolean("Checked"), Component.Serializer.fromJson(tag.getString("Text")));
	}

}
