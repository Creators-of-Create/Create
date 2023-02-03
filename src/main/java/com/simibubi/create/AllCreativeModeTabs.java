package com.simibubi.create;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.components.actors.SeatBlock;
import com.simibubi.create.content.contraptions.components.crank.ValveHandleBlock;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.content.curiosities.toolbox.ToolboxBlock;
import com.simibubi.create.content.palettes.AllPaletteBlocks;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.DisplayItemsGenerator;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD)
public class AllCreativeModeTabs {
	private static CreativeModeTab baseTab;
	private static CreativeModeTab palettesTab;

	@SubscribeEvent
	public static void onCreativeModeTabRegister(CreativeModeTabEvent.Register event) {
		baseTab = event.registerCreativeModeTab(Create.asResource("base"), builder -> {
			builder.title(Component.translatable("itemGroup.create.base"))
				.icon(() -> AllBlocks.COGWHEEL.asStack())
				.displayItems(new RegistrateDisplayItemsGenerator(EnumSet.complementOf(EnumSet.of(AllSections.PALETTES)), true));
		});

		palettesTab = event.registerCreativeModeTab(Create.asResource("palettes"), builder -> {
			builder.title(Component.translatable("itemGroup.create.palettes"))
				.icon(() -> AllPaletteBlocks.ORNATE_IRON_WINDOW.asStack())
				.displayItems(new RegistrateDisplayItemsGenerator(EnumSet.of(AllSections.PALETTES), false));
		});
	}

	public static CreativeModeTab getBaseTab() {
		return baseTab;
	}

	public static CreativeModeTab getPalettesTab() {
		return palettesTab;
	}

	private static class RegistrateDisplayItemsGenerator implements DisplayItemsGenerator {
		private final EnumSet<AllSections> sections;
		private final boolean addItems;

		public RegistrateDisplayItemsGenerator(EnumSet<AllSections> sections, boolean addItems) {
			this.sections = sections;
			this.addItems = addItems;
		}
		private static Predicate<Item> makeExclusionPredicate() {
			Set<Item> exclusions = new ReferenceOpenHashSet<>();

			List<ItemProviderEntry<?>> simpleExclusions = List.of(
					AllItems.INCOMPLETE_PRECISION_MECHANISM,
					AllItems.INCOMPLETE_REINFORCED_SHEET,
					AllItems.INCOMPLETE_TRACK,
					AllItems.CHROMATIC_COMPOUND,
					AllItems.SHADOW_STEEL,
					AllItems.REFINED_RADIANCE,
					AllItems.COPPER_BACKTANK_PLACEABLE,
					AllItems.MINECART_CONTRAPTION,
					AllItems.FURNACE_MINECART_CONTRAPTION,
					AllItems.CHEST_MINECART_CONTRAPTION,
					AllItems.SCHEMATIC,
					AllBlocks.ANDESITE_ENCASED_SHAFT,
					AllBlocks.BRASS_ENCASED_SHAFT,
					AllBlocks.ANDESITE_ENCASED_COGWHEEL,
					AllBlocks.BRASS_ENCASED_COGWHEEL,
					AllBlocks.ANDESITE_ENCASED_LARGE_COGWHEEL,
					AllBlocks.BRASS_ENCASED_LARGE_COGWHEEL,
					AllBlocks.MYSTERIOUS_CUCKOO_CLOCK,
					AllBlocks.SHADOW_STEEL_CASING,
					AllBlocks.REFINED_RADIANCE_CASING
			);

			List<ItemEntry<TagDependentIngredientItem>> tagDependentExclusions = List.of(
					AllItems.CRUSHED_OSMIUM,
					AllItems.CRUSHED_PLATINUM,
					AllItems.CRUSHED_SILVER,
					AllItems.CRUSHED_TIN,
					AllItems.CRUSHED_LEAD,
					AllItems.CRUSHED_QUICKSILVER,
					AllItems.CRUSHED_BAUXITE,
					AllItems.CRUSHED_URANIUM,
					AllItems.CRUSHED_NICKEL
			);

			for (ItemProviderEntry<?> entry : simpleExclusions) {
				exclusions.add(entry.asItem());
			}

			for (ItemEntry<TagDependentIngredientItem> entry : tagDependentExclusions) {
				TagDependentIngredientItem item = entry.get();
				if (item.shouldHide()) {
					exclusions.add(entry.asItem());
				}
			}

			return exclusions::contains;
		}

		private static List<ItemOrdering> makeOrderings() {
			List<ItemOrdering> orderings = new ReferenceArrayList<>();

			Map<ItemProviderEntry<?>, ItemProviderEntry<?>> simpleBeforeOrderings = Map.of(
					AllItems.EMPTY_BLAZE_BURNER, AllBlocks.BLAZE_BURNER,
					AllItems.BELT_CONNECTOR, AllBlocks.CREATIVE_MOTOR,
					AllItems.SCHEDULE, AllBlocks.TRACK_STATION
			);

			Map<ItemProviderEntry<?>, ItemProviderEntry<?>> simpleAfterOrderings = Map.of(
					AllItems.VERTICAL_GEARBOX, AllBlocks.GEARBOX
			);

			simpleBeforeOrderings.forEach((entry, otherEntry) -> {
				orderings.add(ItemOrdering.before(entry.asItem(), otherEntry.asItem()));
			});

			simpleAfterOrderings.forEach((entry, otherEntry) -> {
				orderings.add(ItemOrdering.after(entry.asItem(), otherEntry.asItem()));
			});

			return orderings;
		}

		private static Function<Item, ItemStack> makeStackFunc() {
			Map<Item, Function<Item, ItemStack>> factories = new Reference2ReferenceOpenHashMap<>();

			Map<ItemProviderEntry<?>, Function<Item, ItemStack>> simpleFactories = Map.of(
					AllItems.COPPER_BACKTANK, item -> {
						ItemStack stack = new ItemStack(item);
						stack.getOrCreateTag().putInt("Air", BackTankUtil.maxAirWithoutEnchants());
						return stack;
					}
			);

			simpleFactories.forEach((entry, factory) -> {
				factories.put(entry.asItem(), factory);
			});

			return item -> {
				Function<Item, ItemStack> factory = factories.get(item);
				if (factory != null) {
					return factory.apply(item);
				}
				return new ItemStack(item);
			};
		}

		private static Function<Item, TabVisibility> makeVisibilityFunc() {
			Map<Item, TabVisibility> visibilities = new Reference2ObjectOpenHashMap<>();

			Map<ItemProviderEntry<?>, TabVisibility> simpleVisibilities = Map.of(
					AllItems.BLAZE_CAKE_BASE, TabVisibility.SEARCH_TAB_ONLY
			);

			simpleVisibilities.forEach((entry, factory) -> {
				visibilities.put(entry.asItem(), factory);
			});

			for (BlockEntry<ValveHandleBlock> entry : AllBlocks.DYED_VALVE_HANDLES) {
				visibilities.put(entry.asItem(), TabVisibility.SEARCH_TAB_ONLY);
			}

			for (BlockEntry<SeatBlock> entry : AllBlocks.SEATS) {
				SeatBlock block = entry.get();
				if (block.getColor() != DyeColor.RED) {
					visibilities.put(entry.asItem(), TabVisibility.SEARCH_TAB_ONLY);
				}
			}

			for (BlockEntry<ToolboxBlock> entry : AllBlocks.TOOLBOXES) {
				ToolboxBlock block = entry.get();
				if (block.getColor() != DyeColor.BROWN) {
					visibilities.put(entry.asItem(), TabVisibility.SEARCH_TAB_ONLY);
				}
			}

			return item -> {
				TabVisibility visibility = visibilities.get(item);
				if (visibility != null) {
					return visibility;
				}
				return TabVisibility.PARENT_AND_SEARCH_TABS;
			};
		}

		@Override
		public void accept(FeatureFlagSet features, Output output, boolean isOperator) {
			ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			Predicate<Item> exclusionPredicate = makeExclusionPredicate();
			List<ItemOrdering> orderings = makeOrderings();
			Function<Item, ItemStack> stackFunc = makeStackFunc();
			Function<Item, TabVisibility> visibilityFunc = makeVisibilityFunc();

			if (addItems) {
				outputAll(output, collectItems(itemRenderer, true, exclusionPredicate, orderings), stackFunc, visibilityFunc);
			}

			outputAll(output, collectBlocks(exclusionPredicate, orderings), stackFunc, visibilityFunc);

			if (addItems) {
				outputAll(output, collectItems(itemRenderer, false, exclusionPredicate, orderings), stackFunc, visibilityFunc);
			}
		}

		private List<Item> collectBlocks(Predicate<Item> exclusionPredicate, List<ItemOrdering> orderings) {
			List<Item> items = new ReferenceArrayList<>();
			for (AllSections section : sections) {
				for (RegistryEntry<Block> entry : Create.REGISTRATE.getAll(section, Registries.BLOCK)) {
					Item item = entry.get().asItem();
					if (item != Items.AIR) {
						if (!exclusionPredicate.test(item)) {
							items.add(item);
						}
					}
				}
			}
			items = new LinkedList<>(new ReferenceLinkedOpenHashSet<>(items));
			applyOrderings(items, orderings);
			return items;
		}

		private List<Item> collectItems(ItemRenderer itemRenderer, boolean special, Predicate<Item> exclusionPredicate, List<ItemOrdering> orderings) {
			List<Item> items = new LinkedList<>();
			for (AllSections section : sections) {
				for (RegistryEntry<Item> entry : Create.REGISTRATE.getAll(section, Registries.ITEM)) {
					Item item = entry.get();
					if (!(item instanceof BlockItem)) {
						BakedModel model = itemRenderer.getModel(new ItemStack(item), null, null, 0);
						if (model.isGui3d() == special) {
							if (!exclusionPredicate.test(item)) {
								items.add(item);
							}
						}
					}
				}
			}
			applyOrderings(items, orderings);
			return items;
		}

		private static void applyOrderings(List<Item> items, List<ItemOrdering> orderings) {
			for (ItemOrdering ordering : orderings) {
				int anchorIndex = items.indexOf(ordering.anchor());
				if (anchorIndex != -1) {
					Item item = ordering.item();
					if (items.remove(item)) {
						if (ordering.type() == ItemOrdering.Type.AFTER) {
							items.add(anchorIndex + 1, item);
						} else {
							items.add(anchorIndex, item);
						}
					}
				}
			}
		}

		private static void outputAll(Output output, List<Item> items, Function<Item, ItemStack> stackFunc, Function<Item, TabVisibility> visibilityFunc) {
			for (Item item : items) {
				output.accept(stackFunc.apply(item), visibilityFunc.apply(item));
			}
		}

		private record ItemOrdering(Item item, Item anchor, Type type) {
			public static ItemOrdering before(Item item, Item anchor) {
				return new ItemOrdering(item, anchor, Type.BEFORE);
			}

			public static ItemOrdering after(Item item, Item anchor) {
				return new ItemOrdering(item, anchor, Type.AFTER);
			}

			public enum Type {
				BEFORE,
				AFTER;
			}
		}
	}
}
