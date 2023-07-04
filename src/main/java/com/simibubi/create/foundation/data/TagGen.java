package com.simibubi.create.foundation.data;

import java.util.function.Function;
import java.util.stream.Stream;

import com.simibubi.create.AllTags;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.AllTags.AllEntityTags;
import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.core.Holder;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;

public class TagGen {
	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOrPickaxe() {
		return b -> b.tag(BlockTags.MINEABLE_WITH_AXE)
			.tag(BlockTags.MINEABLE_WITH_PICKAXE);
	}

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> axeOnly() {
		return b -> b.tag(BlockTags.MINEABLE_WITH_AXE);
	}

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, BlockBuilder<T, P>> pickaxeOnly() {
		return b -> b.tag(BlockTags.MINEABLE_WITH_PICKAXE);
	}

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String... path) {
		return b -> {
			for (String p : path)
				b.tag(AllTags.forgeBlockTag(p));
			ItemBuilder<BlockItem, BlockBuilder<T, P>> item = b.item();
			for (String p : path)
				item.tag(AllTags.forgeItemTag(p));
			return item;
		};
	}

	public static <T extends TagAppender<?>> T addOptional(T appender, Mods mod, String id) {
		appender.addOptional(mod.asResource(id));
		return appender;
	}

	public static <T extends TagAppender<?>> T addOptional(T appender, Mods mod, String... ids) {
		for (String id : ids) {
			appender.addOptional(mod.asResource(id));
		}
		return appender;
	}

	public static void datagen() {
		Create.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, TagGen::genBlockTags);
		Create.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, TagGen::genItemTags);
		Create.REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, TagGen::genFluidTags);
		Create.REGISTRATE.addDataGenerator(ProviderType.ENTITY_TAGS, TagGen::genEntityTags);
	}

	private static void genBlockTags(RegistrateTagsProvider<Block> provIn) {
		CreateTagsProvider<Block> prov = new CreateTagsProvider<>(provIn, Block::builtInRegistryHolder);
		
		prov.tag(AllBlockTags.BRITTLE.tag)
			.add(Blocks.BELL, Blocks.COCOA, Blocks.FLOWER_POT)
			.addTag(BlockTags.BEDS)
			.addTag(BlockTags.DOORS);

		prov.tag(AllBlockTags.MOVABLE_EMPTY_COLLIDER.tag)
			.add(Blocks.COBWEB, Blocks.POWDER_SNOW, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK)
			.addTag(BlockTags.FENCE_GATES);

		prov.tag(AllBlockTags.FAN_TRANSPARENT.tag)
			.add(Blocks.IRON_BARS)
			.add(Blocks.MANGROVE_ROOTS)
			.addTag(BlockTags.CAMPFIRES)
			.addTag(BlockTags.FENCES)
			.addTag(BlockTags.LEAVES);

		prov.tag(AllBlockTags.ORE_OVERRIDE_STONE.tag)
			.addTag(BlockTags.STONE_ORE_REPLACEABLES);

		prov.tag(AllBlockTags.PASSIVE_BOILER_HEATERS.tag)
			.add(Blocks.MAGMA_BLOCK, Blocks.LAVA)
			.addTag(BlockTags.CAMPFIRES)
			.addTag(BlockTags.FIRE);

		prov.tag(AllBlockTags.SAFE_NBT.tag)
			.addTag(BlockTags.BANNERS)
			.addTag(BlockTags.SIGNS);

		prov.tag(AllBlockTags.TREE_ATTACHMENTS.tag)
			.add(Blocks.BEE_NEST, Blocks.COCOA, Blocks.MANGROVE_PROPAGULE, Blocks.MOSS_CARPET, Blocks.SHROOMLIGHT, Blocks.VINE);

		prov.tag(AllBlockTags.WINDMILL_SAILS.tag)
			.addTag(BlockTags.WOOL);

		prov.tag(AllBlockTags.WRENCH_PICKUP.tag)
			.add(Blocks.REDSTONE_WIRE, Blocks.REDSTONE_TORCH, Blocks.REPEATER, Blocks.LEVER,
					Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.REDSTONE_WALL_TORCH, Blocks.PISTON, Blocks.STICKY_PISTON,
					Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.DAYLIGHT_DETECTOR, Blocks.TARGET, Blocks.HOPPER)
			.addTag(BlockTags.BUTTONS)
			.addTag(BlockTags.PRESSURE_PLATES)
			.addTag(BlockTags.RAILS);

		prov.tag(AllBlockTags.COPYCAT_ALLOW.tag)
			.add(Blocks.BARREL);
		prov.tag(AllBlockTags.COPYCAT_DENY.tag)
			.addTag(BlockTags.CAULDRONS)
			.addTag(BlockTags.SAPLINGS)
			.addTag(BlockTags.CLIMBABLE);

		// COMPAT

		addOptional(prov.tag(AllBlockTags.NON_MOVABLE.tag), Mods.IE,
				"connector_lv", "connector_lv_relay", "connector_mv", "connector_mv_relay",
				"connector_hv", "connector_hv_relay", "connector_bundled", "connector_structural",
				"connector_redstone", "connector_probe", "breaker_switch");

		// VALIDATE

		for (AllBlockTags tag : AllBlockTags.values()) {
			if (tag.alwaysDatagen) {
				prov.getOrCreateRawBuilder(tag.tag);
			}
		}
	}

	private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
		CreateTagsProvider<Item> prov = new CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);
		
		prov.tag(AllItemTags.SLEEPERS.tag)
			.add(Items.STONE_SLAB, Items.SMOOTH_STONE_SLAB, Items.ANDESITE_SLAB);

		prov.tag(AllItemTags.STRIPPED_LOGS.tag)
			.addTag(AllItemTags.VANILLA_STRIPPED_LOGS.tag)
			.addTag(AllItemTags.MODDED_STRIPPED_LOGS.tag);

		prov.tag(AllItemTags.STRIPPED_WOOD.tag)
			.addTag(AllItemTags.VANILLA_STRIPPED_WOOD.tag)
			.addTag(AllItemTags.MODDED_STRIPPED_WOOD.tag);
		
		prov.tag(AllItemTags.DEPLOYABLE_DRINK.tag)
			.add(Items.MILK_BUCKET, Items.POTION);

		prov.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
			.add(Items.GLASS_BOTTLE, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION,
					Items.HONEY_BOTTLE, Items.CAKE);

		prov.tag(AllItemTags.CONTRAPTION_CONTROLLED.tag)
			.add(Items.BELL, Items.CAMPFIRE, Items.SOUL_CAMPFIRE, Items.DISPENSER, Items.DROPPER);

		prov.tag(AllItemTags.VANILLA_STRIPPED_LOGS.tag)
			.add(Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_BIRCH_LOG, Items.STRIPPED_CRIMSON_STEM,
					Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_MANGROVE_LOG,
					Items.STRIPPED_OAK_LOG, Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_WARPED_STEM);

		prov.tag(AllItemTags.VANILLA_STRIPPED_WOOD.tag)
			.add(Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_BIRCH_WOOD, Items.STRIPPED_CRIMSON_HYPHAE,
					Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_JUNGLE_WOOD, Items.STRIPPED_MANGROVE_WOOD,
					Items.STRIPPED_OAK_WOOD, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_WARPED_HYPHAE);

		prov.tag(ItemTags.BEACON_PAYMENT_ITEMS)
			.addTag(AllItemTags.CREATE_INGOTS.tag);

		prov.tag(Tags.Items.INGOTS)
			.addTag(AllItemTags.CREATE_INGOTS.tag);

		// COMPAT

		genStrippedWood(prov);

		// VALIDATE

		for (AllItemTags tag : AllItemTags.values()) {
			if (tag.alwaysDatagen) {
				prov.getOrCreateRawBuilder(tag.tag);
			}
		}
	}

	private static void genStrippedWood(CreateTagsProvider<Item> prov) {
		TagAppender<Item> logAppender = prov.tag(AllItemTags.MODDED_STRIPPED_LOGS.tag);
		TagAppender<Item> woodAppender = prov.tag(AllItemTags.MODDED_STRIPPED_WOOD.tag);
		StrippedWoodHelper helper = new StrippedWoodHelper(logAppender, woodAppender);

		helper.add(Mods.ARS_N, "blue_archwood", "purple_archwood", "green_archwood", "red_archwood");
		helper.add(Mods.BTN, "livingwood", "dreamwood");
		helper.add(Mods.FA, "cherrywood", "mysterywood");
		helper.add(Mods.HEX, "akashic");
		helper.add(Mods.ID, "menril");
		helper.add(Mods.BYG, "aspen", "baobab", "enchanted", "cherry", "cika", "cypress", "ebony", "ether",
			"fir", "green_enchanted", "holly", "jacaranda", "lament", "mahogany", "mangrove", "maple", "nightshade",
			"palm", "palo_verde", "pine", "rainbow_eucalyptus", "redwood", "skyris", "willow", "witch_hazel",
			"zelkova");
		helper.add(Mods.SG, "netherwood");
		helper.add(Mods.TF, "twilight_oak", "canopy", "mangrove", "dark", "time", "transformation", "mining",
			"sorting");
		helper.add(Mods.TIC, "greenheart", "skyroot", "bloodshroom");
		helper.add(Mods.AP, "twisted");
		helper.add(Mods.Q, "azalea", "blossom");
		helper.add(Mods.ECO, "coconut", "walnut", "azalea");
		helper.add(Mods.BOP, "fir", "redwood", "cherry", "mahogany", "jacaranda", "palm", "willow", "dead",
			"magic", "umbran", "hellbark");
		helper.add(Mods.BSK, "bluebright", "starlit", "frostbright", "lunar", "dusk", "maple", "cherry");

		addOptional(logAppender, Mods.BYG, "stripped_bulbis_stem");
		addOptional(woodAppender, Mods.BYG, "stripped_bulbis_wood");
	}

	private static void genFluidTags(RegistrateTagsProvider<Fluid> provIn) {
		CreateTagsProvider<Fluid> prov = new CreateTagsProvider<>(provIn, Fluid::builtInRegistryHolder);

		prov.tag(AllFluidTags.BOTTOMLESS_ALLOW.tag)
			.add(Fluids.WATER, Fluids.LAVA);

		// VALIDATE

		for (AllFluidTags tag : AllFluidTags.values()) {
			if (tag.alwaysDatagen) {
				prov.getOrCreateRawBuilder(tag.tag);
			}
		}
	}

	private static void genEntityTags(RegistrateTagsProvider<EntityType<?>> provIn) {
		CreateTagsProvider<EntityType<?>> prov = new CreateTagsProvider<>(provIn, EntityType::builtInRegistryHolder);
		
		// VALIDATE

		for (AllEntityTags tag : AllEntityTags.values()) {
			if (tag.alwaysDatagen) {
				prov.getOrCreateRawBuilder(tag.tag);
			}
		}
	}

	private static class StrippedWoodHelper {
		protected final TagAppender<Item> logAppender;
		protected final TagAppender<Item> woodAppender;

		public StrippedWoodHelper(TagAppender<Item> logAppender, TagAppender<Item> woodAppender) {
			this.logAppender = logAppender;
			this.woodAppender = woodAppender;
		}

		public void add(Mods mod, String... woodTypes) {
			for (String type : woodTypes) {
				String strippedPre = mod.strippedIsSuffix ? "" : "stripped_";
				String strippedPost = mod.strippedIsSuffix ? "_stripped" : "";
				addOptional(logAppender, mod, strippedPre + type + "_log" + strippedPost);
				addOptional(woodAppender, mod, strippedPre + type + (mod.omitWoodSuffix ? "" : "_wood") + strippedPost);
			}
		}
	}

	public static class CreateTagsProvider<T> {

		private RegistrateTagsProvider<T> provider;
		private Function<T, ResourceKey<T>> keyExtractor;

		public CreateTagsProvider(RegistrateTagsProvider<T> provider, Function<T, Holder.Reference<T>> refExtractor) {
			this.provider = provider;
			this.keyExtractor = refExtractor.andThen(Holder.Reference::key);
		}

		public CreateTagAppender<T> tag(TagKey<T> tag) {
			TagBuilder tagbuilder = getOrCreateRawBuilder(tag);
			return new CreateTagAppender<>(tagbuilder, keyExtractor, Create.ID);
		}

		public TagBuilder getOrCreateRawBuilder(TagKey<T> tag) {
			return provider.addTag(tag).getInternalBuilder();
		}

	}

	public static class CreateTagAppender<T> extends TagsProvider.TagAppender<T> {

		private Function<T, ResourceKey<T>> keyExtractor;

		public CreateTagAppender(TagBuilder pBuilder, Function<T, ResourceKey<T>> pKeyExtractor, String modId) {
			super(pBuilder, modId);
			this.keyExtractor = pKeyExtractor;
		}

		public CreateTagAppender<T> add(T entry) {
			this.add(this.keyExtractor.apply(entry));
			return this;
		}

		@SafeVarargs
		public final CreateTagAppender<T> add(T... entries) {
			Stream.<T>of(entries)
				.map(this.keyExtractor)
				.forEach(this::add);
			return this;
		}

	}

}
