package com.simibubi.create;

import static com.simibubi.create.AllTags.NameSpace.FORGE;
import static com.simibubi.create.AllTags.NameSpace.MOD;
import static com.simibubi.create.AllTags.NameSpace.TIC;

import java.util.function.Function;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.EmptyNamedTag;
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
import net.minecraftforge.fml.ModList;

public class AllTags {
	private static final CreateRegistrate REGISTRATE = Create.registrate()
			.itemGroup(() -> Create.BASE_CREATIVE_TAB);

	public static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String tagName) {
		return b -> b.tag(forgeBlockTag(tagName))
			.item()
			.tag(forgeItemTag(tagName));
	}

	public static ITag.INamedTag<Block> forgeBlockTag(String name) {
		return forgeTag(BlockTags::bind, name);
	}

	public static ITag.INamedTag<Item> forgeItemTag(String name) {
		return forgeTag(ItemTags::bind, name);
	}

	public static ITag.INamedTag<Fluid> forgeFluidTag(String name) {
		return forgeTag(FluidTags::bind, name);
	}

	public static <T> ITag.INamedTag<T> forgeTag(Function<String, ITag.INamedTag<T>> wrapperFactory, String name) {
		return tag(wrapperFactory, "forge", name);
	}

	public static <T> ITag.INamedTag<T> tag(Function<String, ITag.INamedTag<T>> wrapperFactory, String domain,
		String name) {
		return wrapperFactory.apply(new ResourceLocation(domain, name).toString());
	}

	public static enum NameSpace {

		MOD(Create.ID), FORGE("forge"), MC("minecraft"), TIC("tconstruct")

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
		SANDPAPER(MOD),
		CREATE_INGOTS(MOD),
		BEACON_PAYMENT(FORGE),
		INGOTS(FORGE),
		NUGGETS(FORGE),
		PLATES(FORGE),
		COBBLESTONE(FORGE),
		PRESSURIZED_AIR_SOURCE(MOD)

		;

		public ITag.INamedTag<Item> tag;

		private AllItemTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllItemTags(NameSpace namespace, String path) {
			tag = ItemTags.bind(
				new ResourceLocation(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name())).toString());
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag));
		}

		public boolean matches(ItemStack stack) {
			return tag.contains(stack.getItem());
		}

		public void add(Item... values) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(tag)
				.add(values));
		}

		public void includeIn(AllItemTags parent) {
			REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, prov -> prov.tag(parent.tag)
				.addTag(tag));
		}
	}

	public static enum AllFluidTags {
		NO_INFINITE_DRAINING,
		HONEY(FORGE)

		;

		public ITag.INamedTag<Fluid> tag;

		private AllFluidTags() {
			this(MOD, "");
		}

		private AllFluidTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllFluidTags(NameSpace namespace, String path) {
			tag = FluidTags.createOptional(
				new ResourceLocation(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name())));
		}

		public boolean matches(Fluid fluid) {
			return fluid != null && fluid.is(tag);
		}

		static void loadClass() {}
	}

	public static enum AllBlockTags {
		WINDMILL_SAILS,
		FAN_HEATERS,
		WINDOWABLE,
		BRITTLE,
		SEATS,
		SAILS,
		VALVE_HANDLES,
		FAN_TRANSPARENT,
		SAFE_NBT,
		SLIMY_LOGS(TIC),
		WRENCH_PICKUP,

		;

		public ITag.INamedTag<Block> tag;

		private AllBlockTags() {
			this(MOD, "");
		}

		private AllBlockTags(NameSpace namespace) {
			this(namespace, "");
		}

		private AllBlockTags(NameSpace namespace, String path) {
			ResourceLocation id =
				new ResourceLocation(namespace.id, (path.isEmpty() ? "" : path + "/") + Lang.asId(name()));
			if (ModList.get()
				.isLoaded(namespace.id)) {
				tag = BlockTags.bind(id.toString());
				REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag));
			} else {
				tag = new EmptyNamedTag<>(id);
			}
		}


		public boolean matches(BlockState block) {
			return tag.contains(block.getBlock());
		}

		public void includeIn(AllBlockTags parent) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(parent.tag)
				.addTag(tag));
		}

		public void includeAll(ITag.INamedTag<Block> child) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.addTag(child));
		}

		public void add(Block... values) {
			REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, prov -> prov.tag(tag)
				.add(values));
		}
	}

	public static void register() {
		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.BEACON_PAYMENT);
		AllItemTags.CREATE_INGOTS.includeIn(AllItemTags.INGOTS);

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

		AllFluidTags.loadClass();
	}
}
