package com.simibubi.create;

import java.util.function.Function;

import com.simibubi.create.foundation.item.IHaveCustomItemModel;
import com.simibubi.create.foundation.item.IItemWithColorHandler;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.IModule;
import com.simibubi.create.modules.contraptions.GogglesItem;
import com.simibubi.create.modules.contraptions.WrenchItem;
import com.simibubi.create.modules.contraptions.relays.belt.BeltConnectorItem;
import com.simibubi.create.modules.contraptions.relays.gearbox.VerticalGearboxItem;
import com.simibubi.create.modules.curiosities.ChromaticCompoundCubeItem;
import com.simibubi.create.modules.curiosities.RefinedRadianceItem;
import com.simibubi.create.modules.curiosities.ShadowSteelItem;
import com.simibubi.create.modules.curiosities.blockzapper.BlockzapperItem;
import com.simibubi.create.modules.curiosities.deforester.DeforesterItem;
import com.simibubi.create.modules.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.modules.curiosities.tools.SandPaperItem;
import com.simibubi.create.modules.gardens.TreeFertilizerItem;
import com.simibubi.create.modules.logistics.item.filter.FilterItem;
import com.simibubi.create.modules.schematics.item.SchematicAndQuillItem;
import com.simibubi.create.modules.schematics.item.SchematicItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public enum AllItems {

	__MATERIALS__(module()),
	COPPER_NUGGET,
	ZINC_NUGGET,
	BRASS_NUGGET,
	IRON_SHEET,
	GOLD_SHEET,
	COPPER_SHEET,
	BRASS_SHEET,
	LAPIS_PLATE,

	CRUSHED_IRON,
	CRUSHED_GOLD,
	CRUSHED_COPPER,
	CRUSHED_ZINC,
	CRUSHED_BRASS,

	ANDESITE_ALLOY,
	COPPER_INGOT,
	ZINC_INGOT,
	BRASS_INGOT,

	SAND_PAPER(SandPaperItem::new),
	RED_SAND_PAPER(SandPaperItem::new),
	OBSIDIAN_DUST,
	ROSE_QUARTZ,
	POLISHED_ROSE_QUARTZ,
	CHROMATIC_COMPOUND(ChromaticCompoundCubeItem::new, rarity(Rarity.UNCOMMON)),
	SHADOW_STEEL(ShadowSteelItem::new, rarity(Rarity.UNCOMMON)),
	REFINED_RADIANCE(RefinedRadianceItem::new, rarity(Rarity.UNCOMMON)),
	ELECTRON_TUBE,
	INTEGRATED_CIRCUIT,

	__SCHEMATICS__(module()),
	EMPTY_BLUEPRINT(Item::new, stackSize(1)),
	BLUEPRINT_AND_QUILL(SchematicAndQuillItem::new, stackSize(1)),
	BLUEPRINT(SchematicItem::new),

	__CONTRAPTIONS__(module()),
	BELT_CONNECTOR(BeltConnectorItem::new),
	VERTICAL_GEARBOX(VerticalGearboxItem::new),
	FLOUR,
	DOUGH,
	PROPELLER,
	WHISK,
	BRASS_HAND,
	WRENCH(WrenchItem::new),
	GOGGLES(GogglesItem::new),

	__LOGISTICS__(module()),
	FILTER(FilterItem::new),
	PROPERTY_FILTER(FilterItem::new),

	__CURIOSITIES__(module()),
	TREE_FERTILIZER(TreeFertilizerItem::new),
	PLACEMENT_HANDGUN(BlockzapperItem::new),
	DEFORESTER(DeforesterItem::new),
	SYMMETRY_WAND(SymmetryWandItem::new),

	;

	private static class CategoryTracker {
		static IModule currentModule;
	}

	// Common

	public IModule module;
	private Function<Properties, Properties> specialProperties;
	private Function<Properties, Item> itemSupplier;
	private Item item;

	private AllItems(int moduleMarker) {
		CategoryTracker.currentModule = new IModule() {
			@Override
			public String getModuleName() {
				return Lang.asId(name()).replaceAll("__", "");
			}
		};
	}

	private AllItems() {
		this(Item::new);
	}

	private AllItems(Function<Properties, Item> itemSupplier) {
		this(itemSupplier, Function.identity());
	}

	private AllItems(Function<Properties, Item> itemSupplier, Function<Properties, Properties> specialProperties) {
		this.itemSupplier = itemSupplier;
		this.module = CategoryTracker.currentModule;
		this.specialProperties = specialProperties;
	}

	private static Function<Properties, Properties> rarity(Rarity rarity) {
		return p -> p.rarity(rarity);
	}

	private static Function<Properties, Properties> stackSize(int stackSize) {
		return p -> p.maxStackSize(stackSize);
	}

	private static Properties defaultProperties(AllItems item) {
		return includeInItemGroup().setTEISR(() -> item::getRenderer);
	}

	private static int module() {
		return 0;
	}

	public static Properties includeInItemGroup() {
		return new Properties().group(Create.creativeTab);
	}

	public static void register(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		for (AllItems entry : values()) {
			if (entry.itemSupplier == null)
				continue;

			entry.item = entry.itemSupplier.apply(new Properties());
			entry.item = entry.itemSupplier.apply(entry.specialProperties.apply(defaultProperties(entry)));
			entry.item.setRegistryName(Create.ID, Lang.asId(entry.name()));
			registry.register(entry.item);
		}

		AllBlocks.registerItemBlocks(registry);
	}

	public Item get() {
		return item;
	}

	public boolean typeOf(ItemStack stack) {
		return stack.getItem() == item;
	}

	public ItemStack asStack() {
		return new ItemStack(item);
	}

	// Client

	@OnlyIn(Dist.CLIENT)
	public ItemStackTileEntityRenderer getRenderer() {
		if (!(item instanceof IHaveCustomItemModel))
			return null;
		IHaveCustomItemModel specialItem = (IHaveCustomItemModel) item;
		return specialItem.createModel(null).getRenderer();
	}

	@OnlyIn(Dist.CLIENT)
	public static void registerColorHandlers() {
		ItemColors itemColors = Minecraft.getInstance().getItemColors();
		for (AllItems item : values()) {
			if (item.item instanceof IItemWithColorHandler) {
				itemColors.register(((IItemWithColorHandler) item.item).getColorHandler(), item.item);
			}
		}
	}

}
