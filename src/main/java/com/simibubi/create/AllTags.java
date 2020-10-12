package com.simibubi.create;

import static com.simibubi.create.AllTags.NameSpace.FORGE;
import static com.simibubi.create.AllTags.NameSpace.MOD;

import java.util.function.Function;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

public class AllTags {
	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.baseCreativeTab);

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String tagName) {
		return b -> b.tag(forgeBlockTag(tagName))
			.item()
			.tag(forgeItemTag(tagName));
	}

	public static ITag.INamedTag<Block> forgeBlockTag(String name) {
		return forgeTag(BlockTags::makeWrapperTag, name);
	}

	public static ITag.INamedTag<Item> forgeItemTag(String name) {
		return forgeTag(ItemTags::makeWrapperTag, name);
	}

	public static <T> ITag.INamedTag<T> forgeTag(Function<String, ITag.INamedTag<T>> wrapperFactory, String name) {
		return tag(wrapperFactory, "forge", name);
	}

	public static <T> ITag.INamedTag<T> tag(Function<String, ITag.INamedTag<T>> wrapperFactory, String domain, String name) {
		return wrapperFactory.apply(new ResourceLocation(domain, name).toString());
	}

	public static enum NameSpace {

		MOD(Create.ID), FORGE("forge"), MC("minecraft")

		;
		String id;

		private NameSpace(String id) {
			this.id = id;
		}
	}

	public static enum AllItemTags {
		CRUSHED_ORES(MOD),
		SEATS(MOD),
		VALVE_HANDLES(MOD),
		UPRIGHT_ON_BELT(MOD),
		CREATE_INGOTS(MOD),
		BEACON_PAYMENT(FORGE),
		INGOTS(FORGE),
		NUGGETS(FORGE),
		PLATES(FORGE),
		COBBLESTONE(FORGE)

		;

		public ITag.INamedTag<Item> tag;

		private AllItemTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllItemTags(NameSpace namespace, String path) {
			tag = ItemTags.makeWrapperTag(
				new ResourceLocation(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name())).toString());
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.getOrCreateTagBuilder(tag));
		}

		public boolean matches(ItemStack stack) {
			return tag.contains(stack.getItem());
		}

		public void add(Item... values) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.getOrCreateTagBuilder(tag)
				.add(values));
		}

		public void includeIn(AllItemTags parent) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.getOrCreateTagBuilder(parent.tag)
				.addTag(tag));
		}
	}

	public static enum AllBlockTags {
		WINDMILL_SAILS, FAN_HEATERS, WINDOWABLE, NON_MOVABLE, BRITTLE, SEATS, VALVE_HANDLES, FAN_TRANSPARENT

		;

		public ITag.INamedTag<Block> tag;

		private AllBlockTags() {
			this(MOD, "");
		}

		private AllBlockTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllBlockTags(NameSpace namespace, String path) {
			tag = BlockTags.makeWrapperTag(
				new ResourceLocation(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name())).toString());
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.getOrCreateTagBuilder(tag));
		}

		public boolean matches(BlockState block) {
			return tag.contains(block.getBlock());
		}

		public void includeIn(AllBlockTags parent) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.getOrCreateTagBuilder(parent.tag)
				.addTag(tag));
		}

		public void includeAll(ITag.INamedTag<Block> child) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.getOrCreateTagBuilder(tag)
				.addTag(child));
		}
		
		public void add(Block ...values) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.getOrCreateTagBuilder(tag).add(values));
		}
	}

	public static void register() {
		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.BEACON_PAYMENT);
		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.INGOTS);

		AllItemTags.UPRIGHT_ON_BELT.add(Items.GLASS_BOTTLE, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);

		AllBlockTags.WINDMILL_SAILS.includeAll(BlockTags.WOOL);
		
		AllBlockTags.BRITTLE.includeAll(BlockTags.DOORS);
		AllBlockTags.BRITTLE.add(Blocks.FLOWER_POT, Blocks.BELL);

		AllBlockTags.FAN_TRANSPARENT.includeAll(BlockTags.FENCES);
		AllBlockTags.FAN_TRANSPARENT.add(Blocks.IRON_BARS);

		AllBlockTags.FAN_HEATERS.add(Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.LAVA, Blocks.FIRE);
	}
}
