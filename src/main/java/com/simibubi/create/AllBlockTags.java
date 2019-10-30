package com.simibubi.create;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public enum AllBlockTags {

	WINDMILL_SAILS, FAN_HEATERS, WINDOWABLE,

	;

	public Tag<Block> tag;

	private AllBlockTags() {
		this("");
	}

	private AllBlockTags(String path) {
		tag = new BlockTags.Wrapper(
				new ResourceLocation(Create.ID, (path.isEmpty() ? "" : path + "/") + Lang.asId(name())));
	}

	public boolean matches(BlockState block) {
		return tag.contains(block.getBlock());
	}

}
