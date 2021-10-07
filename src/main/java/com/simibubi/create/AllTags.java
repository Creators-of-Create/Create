package com.simibubi.create;

import static com.simibubi.create.AllTags.NameSpace.FORGE;
import static com.simibubi.create.AllTags.NameSpace.MOD;
import static com.simibubi.create.AllTags.NameSpace.TIC;

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
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

public class AllTags {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.BASE_CREATIVE_TAB);

	public static <T> ITag.INamedTag<T> tag(Function<ResourceLocation, ITag.INamedTag<T>> wrapperFactory, String namespace,
		String path) {
		return wrapperFactory.apply(new ResourceLocation(namespace, path));
	}

	public static <T> ITag.INamedTag<T> forgeTag(Function<ResourceLocation, ITag.INamedTag<T>> wrapperFactory, String path) {
		return tag(wrapperFactory, "forge", path);
	}

	public static ITag.INamedTag<Block> forgeBlockTag(String path) {
		return forgeTag(BlockTags::createOptional, path);
	}

	public static ITag.INamedTag<Item> forgeItemTag(String path) {
		return forgeTag(ItemTags::createOptional, path);
	}

	public static ITag.INamedTag<Fluid> forgeFluidTag(String path) {
		return forgeTag(FluidTags::createOptional, path);
	}

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String path) {
		return b -> b.tag(forgeBlockTag(path))
			.item()
			.tag(forgeItemTag(path));
	}

	public enum NameSpace {

		MOD(Create.ID, false, true),
		FORGE("forge"),
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
		SAFE_NBT,
		SAILS,
		SEATS,
		VALVE_HANDLES,
		WINDMILL_SAILS,
		WINDOWABLE,
		WRENCH_PICKUP,

		WG_STONE(FORGE),

		SLIMY_LOGS(TIC),

		;

		public final ITag.INamedTag<Block> tag;

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
				tag = BlockTags.createOptional(id);
			} else {
				tag = BlockTags.bind(id.toString());
			}
			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag));
			}
		}

		public boolean matches(Block block) {
			return tag.contains(block.getBlock());
		}

		public boolean matches(BlockState state) {
			return matches(state.getBlock());
		}

		public void add(Block... values) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void includeIn(ITag.INamedTag<Block> parent) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(parent)
				.addTag(tag));
		}

		public void includeIn(AllBlockTags parent) {
			includeIn(parent.tag);
		}

		public void includeAll(ITag.INamedTag<Block> child) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

	}

	public enum AllItemTags {

		CREATE_INGOTS,
		CRUSHED_ORES,
		SANDPAPER,
		SEATS,
		UPRIGHT_ON_BELT,
		VALVE_HANDLES,

		BEACON_PAYMENT(FORGE),
		PLATES(FORGE)

		;

		public final ITag.INamedTag<Item> tag;

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
				tag = ItemTags.createOptional(id);
			} else {
				tag = ItemTags.bind(id.toString());
			}
			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag));
			}
		}

		public boolean matches(ItemStack stack) {
			return tag.contains(stack.getItem());
		}

		public void add(Item... values) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void includeIn(ITag.INamedTag<Item> parent) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(parent)
				.addTag(tag));
		}

		public void includeIn(AllItemTags parent) {
			includeIn(parent.tag);
		}

		public void includeAll(ITag.INamedTag<Item> child) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

	}

	public enum AllFluidTags {

		NO_INFINITE_DRAINING(MOD, true, false),

		HONEY(FORGE)

		;

		public final ITag.INamedTag<Fluid> tag;

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
				tag = FluidTags.createOptional(id);
			} else {
				tag = FluidTags.bind(id.toString());
			}
			if (alwaysDatagen) {
				REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(tag));
			}
		}

		public boolean matches(Fluid fluid) {
			return fluid != null && fluid.is(tag);
		}

		public void add(Fluid... values) {
			REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void includeIn(ITag.INamedTag<Fluid> parent) {
			REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(parent)
				.addTag(tag));
		}

		public void includeIn(AllFluidTags parent) {
			includeIn(parent.tag);
		}

		public void includeAll(ITag.INamedTag<Fluid> child) {
			REGISTRATE.addDataGenerator(ProviderType.FLUID_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

		private static void loadClass() {}

	}

	public static void register() {
		AllFluidTags.loadClass();

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
	}

}
