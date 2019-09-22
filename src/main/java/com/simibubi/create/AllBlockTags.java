package com.simibubi.create;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public enum AllBlockTags {

	WINDMILL_SAILS,
	FAN_HEATERS,
	
	;

	public Tag<Block> tag;

	private AllBlockTags() {
		this("");
	}

	private AllBlockTags(String path) {
		tag = new BlockTags.Wrapper(
				new ResourceLocation(Create.ID, (path.isEmpty() ? "" : path + "/") + name().toLowerCase()));
	}
	
	public boolean matches(BlockState block) {
		return tag.contains(block.getBlock());
	}

}
