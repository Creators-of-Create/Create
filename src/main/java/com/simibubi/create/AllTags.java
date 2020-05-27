package com.simibubi.create;

import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;

public class AllTags {

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String tagName) {
		return b -> b.tag(forgeBlockTag(tagName))
			.item()
			.tag(forgeItemTag(tagName));
	}

	public static Tag<Block> forgeBlockTag(String name) {
		return forgeTag(BlockTags.getCollection(), name);
	}

	public static Tag<Item> forgeItemTag(String name) {
		return forgeTag(ItemTags.getCollection(), name);
	}

	public static <T> Tag<T> forgeTag(TagCollection<T> collection, String name) {
		return tag(collection, "forge", name);
	}

	public static <T> Tag<T> tag(TagCollection<T> collection, String domain, String name) {
		return collection.getOrCreate(new ResourceLocation(domain, name));
	}

	public static enum AllItemTags {
		;
	}

	public static enum AllBlockTags {
		WINDMILL_SAILS, FAN_HEATERS, WINDOWABLE,;
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

}
