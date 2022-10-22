package com.simibubi.create;

import static com.simibubi.create.AllTags.NameSpace.FORGE;
import static com.simibubi.create.AllTags.NameSpace.MOD;
import static com.simibubi.create.AllTags.NameSpace.TIC;

import java.util.Collections;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class AllTags {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.creativeModeTab(() -> Create.BASE_CREATIVE_TAB);

	public static <T extends IForgeRegistryEntry<T>> TagKey<T> optionalTag(IForgeRegistry<T> registry,
		ResourceLocation id) {
		return registry.tags()
			.createOptionalTagKey(id, Collections.emptySet());
	}

	public static <T extends IForgeRegistryEntry<T>> TagKey<T> forgeTag(IForgeRegistry<T> registry, String path) {
		return optionalTag(registry, new ResourceLocation("forge", path));
	}

	public static TagKey<Block> forgeBlockTag(String path) {
		return forgeTag(ForgeRegistries.BLOCKS, path);
	}

	public static TagKey<Item> forgeItemTag(String path) {
		return forgeTag(ForgeRegistries.ITEMS, path);
	}

	public static TagKey<Fluid> forgeFluidTag(String path) {
		return forgeTag(ForgeRegistries.FLUIDS, path);
	}

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
				b.tag(forgeBlockTag(p));
			ItemBuilder<BlockItem, BlockBuilder<T, P>> item = b.item();
			for (String p : path)
				item.tag(forgeItemTag(p));
			return item;
		};
	}

	public enum NameSpace {

		MOD(Create.ID, false, true), FORGE("forge"), TIC("tconstruct"), QUARK("quark")

		;

		public final String id;
		public final boolean optionalDefault;
		public final boolean alwaysDatagenDefault;

		NameSpace(String id) {
			this(id, true, false);
		}

		NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
			this.id = id;
			this.optionalDefault = optionalDefault;
			this.alwaysDatagenDefault = alwaysDatagenDefault;
		}

	}

	public enum AllBlockTags {

		BRITTLE,
		CASING,
		FAN_TRANSPARENT,
		NON_MOVABLE,
		ORE_OVERRIDE_STONE,
		PASSIVE_BOILER_HEATERS,
		SAFE_NBT,
		SEATS,
		TOOLBOXES,
		VALVE_HANDLES,
		WINDMILL_SAILS,
		WINDOWABLE,
		WRENCH_PICKUP,
		TREE_ATTACHMENTS,

		RELOCATION_NOT_SUPPORTED(FORGE),
		WG_STONE(FORGE),

		SLIMY_LOGS(TIC),
		NON_DOUBLE_DOOR(NameSpace.QUARK),

		;

		public final TagKey<Block> tag;

		AllBlockTags() {
			this(MOD);
		}

		AllBlockTags(NameSpace namespace) {
			this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		AllBlockTags(NameSpace namespace, String path) {
			this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		AllBlockTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
			this(namespace, null, optional, alwaysDatagen);
		}

		AllBlockTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
			ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
			if (optional) {
				tag = optionalTag(ForgeRegistries.BLOCKS, id);
			} else {
				tag = BlockTags.create(id);
			}
			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag));
			}
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Block block) {
			return block.builtInRegistryHolder()
				.is(tag);
		}

		public boolean matches(BlockState state) {
			return state.is(tag);
		}

		public void add(Block... values) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void addOptional(Mods mod, String... ids) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> {
				TagAppender<Block> builder = prov.tag(tag);
				for (String id : ids)
					builder.addOptional(mod.asResource(id));
			});
		}

		public void includeIn(TagKey<Block> parent) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(parent)
				.addTag(tag));
		}

		public void includeIn(AllBlockTags parent) {
			includeIn(parent.tag);
		}

		public void includeAll(TagKey<Block> child) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

	}

	public enum AllItemTags {

		BLAZE_BURNER_FUEL_REGULAR(MOD, "blaze_burner_fuel/regular"),
		BLAZE_BURNER_FUEL_SPECIAL(MOD, "blaze_burner_fuel/special"),
		CASING,
		CREATE_INGOTS,
		CRUSHED_ORES,
		PRESSURIZED_AIR_SOURCES,
		SANDPAPER,
		SEATS,
		SLEEPERS,
		TOOLBOXES,
		UPRIGHT_ON_BELT,
		VALVE_HANDLES,
		VANILLA_STRIPPED_LOGS,
		VANILLA_STRIPPED_WOOD,
		MODDED_STRIPPED_LOGS,
		MODDED_STRIPPED_WOOD,

		STRIPPED_LOGS(FORGE),
		STRIPPED_WOOD(FORGE),
		BEACON_PAYMENT(FORGE),
		PLATES(FORGE),
		WRENCH(FORGE, "tools/wrench")

		;

		public final TagKey<Item> tag;

		AllItemTags() {
			this(MOD);
		}

		AllItemTags(NameSpace namespace) {
			this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		AllItemTags(NameSpace namespace, String path) {
			this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		AllItemTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
			this(namespace, null, optional, alwaysDatagen);
		}

		AllItemTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
			ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
			if (optional) {
				tag = optionalTag(ForgeRegistries.ITEMS, id);
			} else {
				tag = ItemTags.create(id);
			}
			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag));
			}
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Item item) {
			return item.builtInRegistryHolder()
				.is(tag);
		}

		public boolean matches(ItemStack stack) {
			return stack.is(tag);
		}

		public void add(Item... values) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void addOptional(Mods mod, String... ids) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> {
				TagAppender<Item> builder = prov.tag(tag);
				for (String id : ids)
					builder.addOptional(mod.asResource(id));
			});
		}

		public void includeIn(TagKey<Item> parent) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(parent)
				.addTag(tag));
		}

		public void includeIn(AllItemTags parent) {
			includeIn(parent.tag);
		}

		public void includeAll(TagKey<Item> child) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

	}

	public enum AllFluidTags {

		BOTTOMLESS_ALLOW(MOD, "bottomless/allow"),
		BOTTOMLESS_DENY(MOD, "bottomless/deny"),

		HONEY(FORGE)

		;

		public final TagKey<Fluid> tag;

		AllFluidTags() {
			this(MOD);
		}

		AllFluidTags(NameSpace namespace) {
			this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		AllFluidTags(NameSpace namespace, String path) {
			this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
		}

		AllFluidTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
			this(namespace, null, optional, alwaysDatagen);
		}

		AllFluidTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
			ResourceLocation id = new ResourceLocation(namespace.id, path == null ? Lang.asId(name()) : path);
			if (optional) {
				tag = optionalTag(ForgeRegistries.FLUIDS, id);
			} else {
				tag = FluidTags.create(id);
			}
			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(tag));
			}
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Fluid fluid) {
			return fluid.is(tag);
		}

		public boolean matches(FluidState state) {
			return state.is(tag);
		}

		public void add(Fluid... values) {
			REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void includeIn(TagKey<Fluid> parent) {
			REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(parent)
				.addTag(tag));
		}

		public void includeIn(AllFluidTags parent) {
			includeIn(parent.tag);
		}

		public void includeAll(TagKey<Fluid> child) {
			REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

	}

	public static void register() {
		AllFluidTags.BOTTOMLESS_ALLOW.add(Fluids.WATER, Fluids.LAVA);

		AllItemTags.VANILLA_STRIPPED_LOGS.add(Items.STRIPPED_ACACIA_LOG, Items.STRIPPED_BIRCH_LOG,
			Items.STRIPPED_CRIMSON_STEM, Items.STRIPPED_DARK_OAK_LOG, Items.STRIPPED_JUNGLE_LOG, Items.STRIPPED_OAK_LOG,
			Items.STRIPPED_SPRUCE_LOG, Items.STRIPPED_WARPED_STEM);

		AllItemTags.VANILLA_STRIPPED_LOGS.includeIn(AllItemTags.STRIPPED_LOGS);

		AllItemTags.VANILLA_STRIPPED_WOOD.add(Items.STRIPPED_ACACIA_WOOD, Items.STRIPPED_BIRCH_WOOD,
			Items.STRIPPED_CRIMSON_HYPHAE, Items.STRIPPED_DARK_OAK_WOOD, Items.STRIPPED_JUNGLE_WOOD,
			Items.STRIPPED_OAK_WOOD, Items.STRIPPED_SPRUCE_WOOD, Items.STRIPPED_WARPED_HYPHAE);

		AllItemTags.VANILLA_STRIPPED_WOOD.includeIn(AllItemTags.STRIPPED_WOOD);

		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.BEACON_PAYMENT);
		AllItemTags.CREATE_INGOTS.includeIn(Tags.Items.INGOTS);

		AllItemTags.UPRIGHT_ON_BELT.add(Items.GLASS_BOTTLE, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION,
			Items.HONEY_BOTTLE, Items.CAKE);

		AllItemTags.SLEEPERS.add(Items.STONE_SLAB, Items.SMOOTH_STONE_SLAB, Items.ANDESITE_SLAB);

		AllBlockTags.WINDMILL_SAILS.includeAll(BlockTags.WOOL);

		AllBlockTags.BRITTLE.includeAll(BlockTags.DOORS);
		AllBlockTags.BRITTLE.includeAll(BlockTags.BEDS);
		AllBlockTags.BRITTLE.add(Blocks.FLOWER_POT, Blocks.BELL, Blocks.COCOA);

		AllBlockTags.FAN_TRANSPARENT.includeAll(BlockTags.FENCES);
		AllBlockTags.FAN_TRANSPARENT.includeAll(BlockTags.CAMPFIRES);
		AllBlockTags.FAN_TRANSPARENT.add(Blocks.IRON_BARS);

		AllBlockTags.PASSIVE_BOILER_HEATERS.includeAll(BlockTags.FIRE);
		AllBlockTags.PASSIVE_BOILER_HEATERS.includeAll(BlockTags.CAMPFIRES);
		AllBlockTags.PASSIVE_BOILER_HEATERS.add(Blocks.MAGMA_BLOCK, Blocks.LAVA);

		AllBlockTags.SAFE_NBT.includeAll(BlockTags.SIGNS);
		AllBlockTags.SAFE_NBT.includeAll(BlockTags.BANNERS);

		AllBlockTags.WRENCH_PICKUP.includeAll(BlockTags.RAILS);
		AllBlockTags.WRENCH_PICKUP.includeAll(BlockTags.BUTTONS);
		AllBlockTags.WRENCH_PICKUP.includeAll(BlockTags.PRESSURE_PLATES);
		AllBlockTags.WRENCH_PICKUP.add(Blocks.REDSTONE_WIRE, Blocks.REDSTONE_TORCH, Blocks.REPEATER, Blocks.LEVER,
			Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.REDSTONE_WALL_TORCH, Blocks.PISTON, Blocks.STICKY_PISTON,
			Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.DAYLIGHT_DETECTOR, Blocks.TARGET, Blocks.HOPPER);

		AllBlockTags.TREE_ATTACHMENTS.add(Blocks.BEE_NEST, Blocks.VINE, Blocks.MOSS_CARPET, Blocks.SHROOMLIGHT,
			Blocks.COCOA);

		AllBlockTags.ORE_OVERRIDE_STONE.includeAll(BlockTags.STONE_ORE_REPLACEABLES);

		registerCompat();
	}

	private static void registerCompat() {
		AllBlockTags.NON_MOVABLE.addOptional(Mods.IE, "connector_lv", "connector_lv_relay", "connector_mv",
			"connector_mv_relay", "connector_hv", "connector_hv_relay", "connector_bundled", "connector_structural",
			"connector_redstone", "connector_probe", "breaker_switch");

		strippedWoodCompat(Mods.ARS_N, "blue_archwood", "purple_archwood", "green_archwood", "red_archwood");
		strippedWoodCompat(Mods.BTN, "livingwood", "dreamwood");
		strippedWoodCompat(Mods.FA, "cherrywood", "mysterywood");
		strippedWoodCompat(Mods.HEX, "akashic");
		strippedWoodCompat(Mods.ID, "menril");
		strippedWoodCompat(Mods.BYG, "aspen", "baobab", "enchanted", "cherry", "cika", "cypress", "ebony", "ether",
			"fir", "green_enchanted", "holly", "jacaranda", "lament", "mahogany", "mangrove", "maple", "nightshade",
			"palm", "palo_verde", "pine", "rainbow_eucalyptus", "redwood", "skyris", "willow", "witch_hazel",
			"zelkova");
		strippedWoodCompat(Mods.SG, "netherwood");
		strippedWoodCompat(Mods.TF, "twilight_oak", "canopy", "mangrove", "dark", "time", "transformation", "mining",
			"sorting");
		strippedWoodCompat(Mods.TIC, "greenheart", "skyroot", "bloodshroom");
		strippedWoodCompat(Mods.AP, "twisted");
		strippedWoodCompat(Mods.Q, "azalea", "blossom");
		strippedWoodCompat(Mods.ECO, "coconut", "walnut", "azalea");
		strippedWoodCompat(Mods.BOP, "fir", "redwood", "cherry", "mahogany", "jacaranda", "palm", "willow", "dead",
			"magic", "umbran", "hellbark");
		strippedWoodCompat(Mods.BSK, "bluebright", "starlit", "frostbright", "lunar", "dusk", "maple", "cherry");

		AllItemTags.MODDED_STRIPPED_LOGS.addOptional(Mods.BYG, "stripped_bulbis_stem");
		AllItemTags.MODDED_STRIPPED_WOOD.addOptional(Mods.BYG, "stripped_bulbis_wood");

		AllItemTags.MODDED_STRIPPED_LOGS.includeIn(AllItemTags.STRIPPED_LOGS);
		AllItemTags.MODDED_STRIPPED_WOOD.includeIn(AllItemTags.STRIPPED_WOOD);
	}

	private static void strippedWoodCompat(Mods mod, String... woodtypes) {
		for (int i = 0; i < woodtypes.length; i++) {
			String type = woodtypes[i];
			String strippedPre = mod.strippedIsSuffix ? "" : "stripped_";
			String strippedPost = mod.strippedIsSuffix ? "_stripped" : "";
			AllItemTags.MODDED_STRIPPED_LOGS.addOptional(mod, strippedPre + type + "_log" + strippedPost);
			AllItemTags.MODDED_STRIPPED_WOOD.addOptional(mod,
				strippedPre + type + (mod.omitWoodSuffix ? "" : "_wood") + strippedPost);
		}
	}

}
