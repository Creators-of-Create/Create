package com.simibubi.create.content.trains.display;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;

import net.createmod.catnip.utility.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class FlapDisplayLayout {

	List<FlapDisplaySection> sections;
	String layoutKey;

	public FlapDisplayLayout(int maxCharCount) {
		loadDefault(maxCharCount);
	}

	public void loadDefault(int maxCharCount) {
		configure("Default", Arrays
			.asList(new FlapDisplaySection(maxCharCount * FlapDisplaySection.MONOSPACE, "alphabet", false, false)));
	}

	public boolean isLayout(String key) {
		return layoutKey.equals(key);
	}

	public void configure(String layoutKey, List<FlapDisplaySection> sections) {
		this.layoutKey = layoutKey;
		this.sections = sections;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		tag.putString("Key", layoutKey);
		tag.put("Sections", NBTHelper.writeCompoundList(sections, FlapDisplaySection::write));
		return tag;
	};

	public void read(CompoundTag tag) {
		String prevKey = layoutKey;
		layoutKey = tag.getString("Key");
		ListTag sectionsTag = tag.getList("Sections", Tag.TAG_COMPOUND);

		if (!prevKey.equals(layoutKey)) {
			sections = NBTHelper.readCompoundList(sectionsTag, FlapDisplaySection::load);
			return;
		}

		MutableInt index = new MutableInt(0);
		NBTHelper.iterateCompoundList(sectionsTag, nbt -> sections.get(index.getAndIncrement())
			.update(nbt));
	}

	public List<FlapDisplaySection> getSections() {
		return sections;
	}

}
