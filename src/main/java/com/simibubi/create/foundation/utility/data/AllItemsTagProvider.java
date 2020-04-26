package com.simibubi.create.foundation.utility.data;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllItems;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class AllItemsTagProvider extends ItemTagsProvider {

	static Map<ResourceLocation, ItemTags.Wrapper> createdTags;

	protected AllItemsTagProvider(DataGenerator generatorIn) {
		super(generatorIn);
	}

	@Override
	protected void registerTags() {
		createdTags = new HashMap<>();

		//now do the same for AllItems
		for (AllItems entry :
				AllItems.values()){
			entry.getTaggable().getTagSet(ITaggable.ITEM).forEach(resLoc -> {
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
