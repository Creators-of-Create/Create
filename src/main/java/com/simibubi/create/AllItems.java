package com.simibubi.create;

import com.simibubi.create.foundation.item.IItemWithColorHandler;
import com.simibubi.create.modules.contraptions.relays.belt.BeltItem;
import com.simibubi.create.modules.curiosities.ChromaticCompoundCubeItem;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunItem;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunItemRenderer;
import com.simibubi.create.modules.curiosities.placementHandgun.BuilderGunModel;
import com.simibubi.create.modules.gardens.TreeFertilizerItem;
import com.simibubi.create.modules.schematics.item.SchematicAndQuillItem;
import com.simibubi.create.modules.schematics.item.SchematicItem;
import com.simibubi.create.modules.symmetry.SymmetryWandItem;
import com.simibubi.create.modules.symmetry.client.SymmetryWandItemRenderer;
import com.simibubi.create.modules.symmetry.client.SymmetryWandModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public enum AllItems {

	SYMMETRY_WAND(new SymmetryWandItem(
			standardProperties().setTEISR(() -> () -> renderUsing(AllItemRenderers.SYMMETRY_WAND)))),

	PLACEMENT_HANDGUN(
			new BuilderGunItem(new Properties().setTEISR(() -> () -> renderUsing(AllItemRenderers.BUILDER_GUN)))),

	ANDESITE_ALLOY_CUBE(new Item(standardProperties())),
	BLAZE_BRASS_CUBE(new Item(standardProperties())),
	CHORUS_CHROME_CUBE(new Item(standardProperties().rarity(Rarity.UNCOMMON))),
	SHADOW_STEEL_CUBE(new Item(standardProperties().rarity(Rarity.UNCOMMON))),
	ROSE_QUARTZ(new Item(standardProperties())),
	REFINED_ROSE_QUARTZ(new Item(standardProperties())),
	CHROMATIC_COMPOUND_CUBE(new ChromaticCompoundCubeItem(standardProperties().rarity(Rarity.UNCOMMON))),
	REFINED_RADIANCE_CUBE(new Item(standardProperties().rarity(Rarity.RARE))),

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

	TREE_FERTILIZER(new TreeFertilizerItem(standardProperties())),

	EMPTY_BLUEPRINT(new Item(standardProperties().maxStackSize(1))),
	BLUEPRINT_AND_QUILL(new SchematicAndQuillItem(standardProperties().maxStackSize(1))),
	BLUEPRINT(new SchematicItem(standardProperties())),
	BELT_CONNECTOR(new BeltItem(standardProperties())),

	;

	// Common

	public Item item;

	private AllItems(Item item) {
		this.item = item;
		this.item.setRegistryName(Create.ID, this.name().toLowerCase());
	}

	public static Properties standardProperties() {
		return new Properties().group(Create.creativeTab);
	}

	public static void registerItems(IForgeRegistry<Item> iForgeRegistry) {
		for (AllItems item : values()) {
			iForgeRegistry.register(item.get());
		}
	}

	public Item get() {
		return item;
	}

	public boolean typeOf(ItemStack stack) {
		return stack.getItem() == item;
	}

	// Client

	private enum AllItemRenderers {
		SYMMETRY_WAND, BUILDER_GUN,;
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
		default:
			return null;
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void onModelBake(ModelBakeEvent event) {

		ModelResourceLocation wandLocation = getModelLocation(SYMMETRY_WAND);
		IBakedModel template = event.getModelRegistry().get(wandLocation);
		event.getModelRegistry().put(wandLocation, new SymmetryWandModel(template).loadPartials(event));

		ModelResourceLocation handgunLocation = getModelLocation(PLACEMENT_HANDGUN);
		template = event.getModelRegistry().get(handgunLocation);
		event.getModelRegistry().put(handgunLocation, new BuilderGunModel(template).loadPartials(event));
	}

	protected static ModelResourceLocation getModelLocation(AllItems item) {
		return new ModelResourceLocation(item.item.getRegistryName(), "inventory");
	}

}
