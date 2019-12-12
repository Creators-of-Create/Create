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
import com.simibubi.create.modules.curiosities.deforester.DeforesterItem;
import com.simibubi.create.modules.curiosities.deforester.DeforesterItemRenderer;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunItem;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunItemRenderer;
import com.simibubi.create.modules.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.modules.curiosities.symmetry.client.SymmetryWandItemRenderer;
import com.simibubi.create.modules.gardens.TreeFertilizerItem;
import com.simibubi.create.modules.logistics.item.CardboardBoxItem;
import com.simibubi.create.modules.logistics.item.FilterItem;
import com.simibubi.create.modules.logistics.management.LogisticalDialItem;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerBlock.Type;
import com.simibubi.create.modules.logistics.management.base.LogisticalControllerItem;
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

	__CURIOSITIES__(),
	SYMMETRY_WAND(new SymmetryWandItem(
			standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.SYMMETRY_WAND)))),
	PLACEMENT_HANDGUN(
			new BuilderGunItem(new Properties().setTEISR(() -> () -> renderUsing(AllItemRenderers.BUILDER_GUN)))),
	DEFORESTER(new DeforesterItem(
			standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.DEFORESTER)))),

	__MATERIALS__(),
	IRON_SHEET(ingredient()),
	GOLD_SHEET(ingredient()),
	ANDESITE_ALLOY_CUBE(ingredient()),
	BLAZE_BRASS_CUBE(ingredient()),
	CHORUS_CHROME_CUBE(ingredient(Rarity.UNCOMMON)),
	SHADOW_STEEL_CUBE(new Item(new Properties().rarity(Rarity.UNCOMMON))),
	ROSE_QUARTZ(new Item(new Properties())),
	REFINED_ROSE_QUARTZ(new Item(new Properties())),
	CHROMATIC_COMPOUND_CUBE(new ChromaticCompoundCubeItem(new Properties().rarity(Rarity.UNCOMMON))),
	REFINED_RADIANCE_CUBE(new Item(new Properties().rarity(Rarity.UNCOMMON))),

//	BLAZING_PICKAXE(new BlazingToolItem(1, -2.8F, standardProperties(), PICKAXE)),
//	BLAZING_SHOVEL(new BlazingToolItem(1.5F, -3.0F, standardProperties(), SHOVEL)),
//	BLAZING_AXE(new BlazingToolItem(5.0F, -3.0F, standardProperties(), AXE)),
//	BLAZING_SWORD(new BlazingToolItem(3, -2.4F, standardProperties(), SWORD)),
//	
//	ROSE_QUARTZ_PICKAXE(new RoseQuartzToolItem(1, -2.8F, standardProperties(), PICKAXE)),
//	ROSE_QUARTZ_SHOVEL(new RoseQuartzToolItem(1.5F, -3.0F, standardProperties(), SHOVEL)),
//	ROSE_QUARTZ_AXE(new RoseQuartzToolItem(5.0F, -3.0F, standardProperties(), AXE)),
//	ROSE_QUARTZ_SWORD(new RoseQuartzToolItem(3, -2.4F, standardProperties(), SWORD)),
//
//	SHADOW_STEEL_PICKAXE(new ShadowSteelToolItem(2.5F, -2.0F, standardProperties(), PICKAXE)),
//	SHADOW_STEEL_MATTOCK(new ShadowSteelToolItem(2.5F, -1.5F, standardProperties(), SHOVEL, AXE, HOE)),
//	SHADOW_STEEL_SWORD(new ShadowSteelToolItem(3, -2.0F, standardProperties(), SWORD)),

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
	WRENCH(new WrenchItem(standardItemProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.WRENCH)))),
	GOGGLES(new GogglesItem(standardItemProperties())),

	CRUSHED_IRON(ingredient()),
	CRUSHED_GOLD(ingredient()),

	__LOGISTICS__(),
	CARDBOARD_BOX_1616(new CardboardBoxItem(standardItemProperties())),
	CARDBOARD_BOX_1612(new CardboardBoxItem(standardItemProperties())),
	CARDBOARD_BOX_1416(new CardboardBoxItem(standardItemProperties())),
	CARDBOARD_BOX_1410(new CardboardBoxItem(standardItemProperties())),

	FILTER(new FilterItem(standardItemProperties())),
	LOGISTICAL_DIAL(new LogisticalDialItem(standardItemProperties())),
	LOGISTICAL_CONTROLLER_SUPPLY(new LogisticalControllerItem(standardItemProperties(), Type.SUPPLY)),
	LOGISTICAL_CONTROLLER_REQUEST(new LogisticalControllerItem(standardItemProperties(), Type.REQUEST)),
	LOGISTICAL_CONTROLLER_STORAGE(new LogisticalControllerItem(standardItemProperties(), Type.STORAGE)),
	LOGISTICAL_CONTROLLER_CALCULATION(new LogisticalControllerItem(standardItemProperties(), Type.CALCULATION)),
	LOGISTICAL_CONTROLLER_TRANSACTIONS(new LogisticalControllerItem(standardItemProperties(), Type.TRANSACTIONS)),

	;

	private static class CategoryTracker {
		static IModule currentModule;
	}

	// Common

	public Item item;
	public IModule module;

	private AllItems() {
		CategoryTracker.currentModule = new IModule() {
			@Override
			public String getModuleName() {
				return Lang.asId(name()).replaceAll("__", "");
			}
		};
	}

	private AllItems(Item item) {
		this.item = item;
		this.item.setRegistryName(Create.ID, Lang.asId(name()));
		this.module = CategoryTracker.currentModule;
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
		SYMMETRY_WAND, BUILDER_GUN, WRENCH, DEFORESTER;
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
			return new BuilderGunItemRenderer();
		case WRENCH:
			return new WrenchItemRenderer();
		case DEFORESTER:
			return new DeforesterItemRenderer();
		default:
			return null;
		}
	}

}
