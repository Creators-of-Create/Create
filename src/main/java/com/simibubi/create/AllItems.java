package com.simibubi.create;

import com.simibubi.create.foundation.item.IItemWithColorHandler;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.modules.IModule;
import com.simibubi.create.modules.contraptions.GogglesItem;
import com.simibubi.create.modules.contraptions.WrenchItem;
import com.simibubi.create.modules.contraptions.WrenchItemRenderer;
import com.simibubi.create.modules.contraptions.relays.belt.BeltConnectorItem;
import com.simibubi.create.modules.contraptions.relays.gearbox.VerticalGearboxItem;
import com.simibubi.create.modules.curiosities.ChromaticCompoundCubeItem;
import com.simibubi.create.modules.curiosities.RefinedRadianceItem;
import com.simibubi.create.modules.curiosities.ShadowSteelItem;
import com.simibubi.create.modules.curiosities.blockzapper.BlockzapperItem;
import com.simibubi.create.modules.curiosities.blockzapper.BlockzapperItemRenderer;
import com.simibubi.create.modules.curiosities.deforester.DeforesterItem;
import com.simibubi.create.modules.curiosities.deforester.DeforesterItemRenderer;
import com.simibubi.create.modules.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.modules.curiosities.symmetry.client.SymmetryWandItemRenderer;
import com.simibubi.create.modules.curiosities.tools.SandPaperItem;
import com.simibubi.create.modules.curiosities.tools.SandPaperItemRenderer;
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

	__MATERIALS__(),
	COPPER_NUGGET(ingredient()),
	ZINC_NUGGET(ingredient()),
	BRASS_NUGGET(ingredient()),
	IRON_SHEET(ingredient()),
	GOLD_SHEET(ingredient()),
	COPPER_SHEET(ingredient()),
	BRASS_SHEET(ingredient()),
	LAPIS_PLATE(ingredient()),

	CRUSHED_IRON(ingredient()),
	CRUSHED_GOLD(ingredient()),
	CRUSHED_COPPER(ingredient()),
	CRUSHED_ZINC(ingredient()),
	CRUSHED_BRASS(ingredient()),

	ANDESITE_ALLOY(ingredient()),
	COPPER_INGOT(ingredient()),
	ZINC_INGOT(ingredient()),
	BRASS_INGOT(ingredient()),
	SAND_PAPER(
			new SandPaperItem(standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.SAND_PAPER)))),
	RED_SAND_PAPER(
			new SandPaperItem(standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.SAND_PAPER)))),
	OBSIDIAN_DUST(ingredient()),
	ROSE_QUARTZ(ingredient()),
	POLISHED_ROSE_QUARTZ(ingredient()),
	CHROMATIC_COMPOUND(new ChromaticCompoundCubeItem(standardItemProperties().rarity(Rarity.UNCOMMON))),
	SHADOW_STEEL(new ShadowSteelItem(standardItemProperties().rarity(Rarity.UNCOMMON))),
	REFINED_RADIANCE(new RefinedRadianceItem(standardItemProperties().rarity(Rarity.UNCOMMON))),
	ELECTRON_TUBE(ingredient()),
	INTEGRATED_CIRCUIT(ingredient()),

	__GARDENS__(),
	TREE_FERTILIZER(new TreeFertilizerItem(standardItemProperties())),

	__SCHEMATICS__(),
	EMPTY_BLUEPRINT(new Item(standardItemProperties().maxStackSize(1))),
	BLUEPRINT_AND_QUILL(new SchematicAndQuillItem(standardItemProperties().maxStackSize(1))),
	BLUEPRINT(new SchematicItem(standardItemProperties())),

	__CONTRAPTIONS__(),
	BELT_CONNECTOR(new BeltConnectorItem(standardItemProperties())),
	VERTICAL_GEARBOX(new VerticalGearboxItem(new Properties())),
	FLOUR(ingredient()),
	DOUGH(ingredient()),
	PROPELLER(ingredient()),
	WHISK(ingredient()),
	BRASS_HAND(ingredient()),
	WRENCH(new WrenchItem(standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.WRENCH))), true),
	GOGGLES(new GogglesItem(standardItemProperties()), true),

	__LOGISTICS__(),
//	CARDBOARD_BOX(new CardboardBoxItem(standardItemProperties())),
//	CARDBOARD_BOX_1(new CardboardBoxItem(standardItemProperties())),
//	CARDBOARD_BOX_2(new CardboardBoxItem(standardItemProperties())),
//	CARDBOARD_BOX_3(new CardboardBoxItem(standardItemProperties())),

	FILTER(new FilterItem(standardItemProperties()), true),
	PROPERTY_FILTER(new FilterItem(standardItemProperties()), true),
//	LOGISTICAL_FILTER(new FilterItem(standardItemProperties())),
//	LOGISTICAL_DIAL(new LogisticalDialItem(standardItemProperties())),
//	LOGISTICAL_CONTROLLER_SUPPLY(new LogisticalControllerItem(standardItemProperties(), Type.SUPPLY)),
//	LOGISTICAL_CONTROLLER_REQUEST(new LogisticalControllerItem(standardItemProperties(), Type.REQUEST)),
//	LOGISTICAL_CONTROLLER_STORAGE(new LogisticalControllerItem(standardItemProperties(), Type.STORAGE)),
//	LOGISTICAL_CONTROLLER_CALCULATION(new LogisticalControllerItem(standardItemProperties(), Type.CALCULATION)),
//	LOGISTICAL_CONTROLLER_TRANSACTIONS(new LogisticalControllerItem(standardItemProperties(), Type.TRANSACTIONS)),

	__CURIOSITIES__(),
	PLACEMENT_HANDGUN(
			new BlockzapperItem(new Properties().setTEISR(() -> () -> renderUsing(AllItemRenderers.BUILDER_GUN))),
			true),
	DEFORESTER(
			new DeforesterItem(standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.DEFORESTER))),
			true),
	SYMMETRY_WAND(new SymmetryWandItem(
			standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.SYMMETRY_WAND))), true),

	;

	private static class CategoryTracker {
		static IModule currentModule;
	}

	// Common

	public Item item;
	public IModule module;
	public boolean firstInCreativeTab;

	private AllItems() {
		CategoryTracker.currentModule = new IModule() {
			@Override
			public String getModuleName() {
				return Lang.asId(name()).replaceAll("__", "");
			}
		};
	}

	private AllItems(Item item) {
		this(item, false);
	}

	private AllItems(Item item, boolean firstInCreativeTab) {
		this.item = item;
		this.item.setRegistryName(Create.ID, Lang.asId(name()));
		this.module = CategoryTracker.currentModule;
		this.firstInCreativeTab = firstInCreativeTab;
	}

	public static Properties standardItemProperties() {
		return new Properties().group(Create.creativeTab);
	}

	private static Item ingredient() {
		return ingredient(Rarity.COMMON);
	}

	private static Item ingredient(Rarity rarity) {
		return new Item(standardItemProperties().rarity(rarity));
	}

	public static void register(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		for (AllItems item : values()) {
			if (item.get() == null)
				continue;
			registry.register(item.get());
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

	private enum AllItemRenderers {
		SYMMETRY_WAND, BUILDER_GUN, WRENCH, DEFORESTER, SAND_PAPER;
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

	@OnlyIn(Dist.CLIENT)
	public static ItemStackTileEntityRenderer renderUsing(AllItemRenderers renderer) {
		switch (renderer) {

		case SYMMETRY_WAND:
			return new SymmetryWandItemRenderer();
		case BUILDER_GUN:
			return new BlockzapperItemRenderer();
		case WRENCH:
			return new WrenchItemRenderer();
		case DEFORESTER:
			return new DeforesterItemRenderer();
		case SAND_PAPER:
			return new SandPaperItemRenderer();
		default:
			return null;
		}
	}

}
