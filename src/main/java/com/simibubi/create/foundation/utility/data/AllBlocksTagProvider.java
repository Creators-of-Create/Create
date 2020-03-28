package com.simibubi.create.foundation.utility.data;

import com.simibubi.create.AllBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AllBlocksTagProvider extends BlockTagsProvider {

	static Map<ResourceLocation, BlockTags.Wrapper> createdTags;

	protected AllBlocksTagProvider(DataGenerator generatorIn) {
		super(generatorIn);
	}

	@Override
	protected void registerTags() {
		createdTags = new HashMap<>();

		for (AllBlocks entry :
				AllBlocks.values()) {
			entry.getTaggable().getTagSet(ITaggable.TagType.BLOCK).forEach(resLoc -> {
				if (resLoc.getNamespace().equals("forge") && resLoc.getPath().contains("/"))
					builder(new ResourceLocation(resLoc.getNamespace(), resLoc.getPath().split("/")[0])).add(new Tag<>(resLoc));
				builder(resLoc).add(entry.get());
			});
			if (entry.alsoRegistered == null)
				continue;

			Arrays.stream(entry.alsoRegistered).forEach(
					taggedBlock -> taggedBlock.getTagSet(ITaggable.TagType.BLOCK).forEach(
							resLoc -> builder(resLoc).add(taggedBlock.getBlock())));
		}
	}

	private Tag.Builder<Block> builder(ResourceLocation resLoc) {
		return this.getBuilder(createdTags.computeIfAbsent(resLoc, BlockTags.Wrapper::new));

	}

	@Override
	public String getName() {
		return "Create Block Tags";
	}
}
