package com.simibubi.create;

import static com.simibubi.create.AllTags.NameSpace.FORGE;
import static com.simibubi.create.AllTags.NameSpace.MOD;
import static com.simibubi.create.AllTags.NameSpace.TIC;

import java.util.Collections;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.Lang;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import me.alphamode.forgetags.Tags;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
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

public class AllTags {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.creativeModeTab(() -> Create.BASE_CREATIVE_TAB);

	public static <T> TagKey<T> optionalTag(Registry<T> registry, ResourceLocation id) {
		return TagKey.create(registry.key(), id);
	}

	public static <T> TagKey<T> forgeTag(Registry<T> registry, String path) {
		return optionalTag(registry, new ResourceLocation("c", path));
	}

	public static TagKey<Block> forgeBlockTag(String path) {
		return forgeTag(Registry.BLOCK, path);
	}

	public static TagKey<Item> forgeItemTag(String path) {
		return forgeTag(Registry.ITEM, path);
	}

	public static TagKey<Fluid> forgeFluidTag(String path) {
		return forgeTag(Registry.FLUID, path);
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

		MOD(Create.ID, false, true),
		FORGE("c"),
		TIC("tconstruct")

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
		FAN_HEATERS,
		FAN_TRANSPARENT,
		ORE_OVERRIDE_STONE,
		SAFE_NBT,
		SEATS,
		TOOLBOXES,
		VALVE_HANDLES,
		WINDMILL_SAILS,
		WINDOWABLE,
		WRENCH_PICKUP,

		RELOCATION_NOT_SUPPORTED(FORGE),
		WG_STONE(FORGE),

		SLIMY_LOGS(TIC),

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
			tag = optionalTag(Registry.BLOCK, id);
			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag));
			}
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Block block) {
			return block.builtInRegistryHolder().is(tag);
		}

		public boolean matches(BlockState state) {
			return state.is(tag);
		}

		public void add(Block... values) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void includeIn(TagKey<Block> parent) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(parent)
				.addTag(tag));
		}

		public void includeIn(AllBlockTags parent) {
			includeIn(parent.tag);
		}

		public void includeAll(TagKey<Block> child) {
			// Minecraft tags need to be loaded
			if(child.location().getNamespace().equals("minecraft"))
				REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(child));
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

	}

	public enum AllItemTags {

		BLAZE_BURNER_FUEL_REGULAR(MOD, "blaze_burner_fuel/regular"),
		BLAZE_BURNER_FUEL_SPECIAL(MOD, "blaze_burner_fuel/special"),
		CREATE_INGOTS,
		CRUSHED_ORES,
		SANDPAPER,
		SEATS,
		TOOLBOXES,
		UPRIGHT_ON_BELT,
		VALVE_HANDLES,

		BEACON_PAYMENT(FORGE),
		PLATES(FORGE),
		WRENCHES(FORGE)

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
			tag = optionalTag(Registry.ITEM, id);

			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag));
			}
		}

		@SuppressWarnings("deprecation")
		public boolean matches(Item item) {
			return item.builtInRegistryHolder().is(tag);
		}

		public boolean matches(ItemStack stack) {
			return stack.is(tag);
		}

		public void add(Item... values) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
				.add(values));
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
			tag = optionalTag(Registry.FLUID, id);
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

		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.BEACON_PAYMENT);
		AllItemTags.CREATE_INGOTS.includeIn(Tags.Items.INGOTS);

		AllItemTags.UPRIGHT_ON_BELT.add(Items.GLASS_BOTTLE, Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION,
			Items.HONEY_BOTTLE, Items.CAKE);

		AllBlockTags.WINDMILL_SAILS.includeAll(BlockTags.WOOL);

		AllBlockTags.BRITTLE.includeAll(BlockTags.DOORS);
		AllBlockTags.BRITTLE.includeAll(BlockTags.BEDS);
		AllBlockTags.BRITTLE.add(Blocks.FLOWER_POT, Blocks.BELL, Blocks.COCOA);

		AllBlockTags.FAN_TRANSPARENT.includeAll(BlockTags.FENCES);
		AllBlockTags.FAN_TRANSPARENT.add(Blocks.IRON_BARS, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);

		AllBlockTags.FAN_HEATERS.add(Blocks.MAGMA_BLOCK, Blocks.CAMPFIRE, Blocks.LAVA, Blocks.FIRE, Blocks.SOUL_FIRE,
			Blocks.SOUL_CAMPFIRE);
		AllBlockTags.SAFE_NBT.includeAll(BlockTags.SIGNS);

		AllBlockTags.WRENCH_PICKUP.includeAll(BlockTags.RAILS);
		AllBlockTags.WRENCH_PICKUP.includeAll(BlockTags.BUTTONS);
		AllBlockTags.WRENCH_PICKUP.includeAll(BlockTags.PRESSURE_PLATES);
		AllBlockTags.WRENCH_PICKUP.add(Blocks.REDSTONE_WIRE, Blocks.REDSTONE_TORCH, Blocks.REPEATER, Blocks.LEVER,
			Blocks.COMPARATOR, Blocks.OBSERVER, Blocks.REDSTONE_WALL_TORCH, Blocks.PISTON, Blocks.STICKY_PISTON,
			Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.DAYLIGHT_DETECTOR, Blocks.TARGET);

		AllBlockTags.ORE_OVERRIDE_STONE.includeAll(BlockTags.STONE_ORE_REPLACEABLES);
	}

}
