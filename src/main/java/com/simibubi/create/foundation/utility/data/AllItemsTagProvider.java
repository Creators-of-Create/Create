package com.simibubi.create.foundation.utility.data;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AllItemsTagProvider extends ItemTagsProvider {

	static Map<ResourceLocation, ItemTags.Wrapper> createdTags;

	protected AllItemsTagProvider(DataGenerator generatorIn) {
		super(generatorIn);
	}

	@Override
	protected void registerTags() {
		createdTags = new HashMap<>();

		//first create all tags for AllBlocks as ItemBlocks
		for (AllBlocks entry :
				AllBlocks.values()) {
			entry.getTaggable().getTagSet(ITaggable.TagType.ITEM).forEach(resLoc -> {
				if (resLoc.getNamespace().equals("forge") && resLoc.getPath().contains("/"))
					builder(new ResourceLocation(resLoc.getNamespace(), resLoc.getPath().split("/")[0])).add(new Tag<>(resLoc));
				builder(resLoc).add(entry.get().asItem());
			});
			if (entry.alsoRegistered == null)
				continue;

			Arrays.stream(entry.alsoRegistered).forEach(
					taggedBlock -> taggedBlock.getTagSet(ITaggable.TagType.ITEM).forEach(
							resLoc -> builder(resLoc).add(taggedBlock.getBlock().asItem())));
		}
		//now do the same for AllItems
		for (AllItems entry :
				AllItems.values()){
			entry.getTaggable().getTagSet(ITaggable.TagType.ITEM).forEach(resLoc -> {
				if (resLoc.getNamespace().equals("forge") && resLoc.getPath().contains("/"))
					builder(new ResourceLocation(resLoc.getNamespace(), resLoc.getPath().split("/")[0])).add(new Tag<>(resLoc));
				builder(resLoc).add(entry.get().asItem());
			});
		}

	}

	private Tag.Builder<Item> builder(ResourceLocation resLoc) {
		return this.getBuilder(createdTags.computeIfAbsent(resLoc, ItemTags.Wrapper::new));

	}

	@Override
	public String getName() {
		return "Create Item Tags";
	}
}
