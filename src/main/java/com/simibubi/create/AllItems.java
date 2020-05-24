package com.simibubi.create;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.simibubi.create.content.AllSections;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MinecartContraptionItem;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.content.contraptions.relays.gearbox.VerticalGearboxItem;
import com.simibubi.create.content.contraptions.wrench.WrenchItem;
import com.simibubi.create.content.curiosities.ChromaticCompoundCubeItem;
import com.simibubi.create.content.curiosities.RefinedRadianceItem;
import com.simibubi.create.content.curiosities.ShadowSteelItem;
import com.simibubi.create.content.curiosities.TreeFertilizerItem;
import com.simibubi.create.content.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.content.curiosities.tools.DeforesterItem;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.content.curiosities.zapper.blockzapper.BlockzapperItem;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.TerrainzapperItem;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.schematics.item.SchematicAndQuillItem;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.data.ITaggable;
import com.simibubi.create.foundation.item.IHaveCustomItemModel;
import com.simibubi.create.foundation.item.IItemWithColorHandler;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity.Type;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public enum AllItems {

	_1_(AllSections.MATERIALS),
	
	COPPER_NUGGET((TaggedItem) new TaggedItem().withForgeTags("nuggets/copper")),
	ZINC_NUGGET((TaggedItem) new TaggedItem().withForgeTags("nuggets/zinc")),
	BRASS_NUGGET((TaggedItem) new TaggedItem().withForgeTags("nuggets/brass")),
	IRON_SHEET((TaggedItem) new TaggedItem().withForgeTags("plates/iron")),
	GOLD_SHEET((TaggedItem) new TaggedItem().withForgeTags("plates/gold")),
	COPPER_SHEET((TaggedItem) new TaggedItem().withForgeTags("plates/copper")),
	BRASS_SHEET((TaggedItem) new TaggedItem().withForgeTags("plates/brass")),
	LAPIS_PLATE((TaggedItem) new TaggedItem().withForgeTags("plates/lapis")),

	CRUSHED_IRON,
	CRUSHED_GOLD,
	CRUSHED_COPPER,
	CRUSHED_ZINC,
	CRUSHED_BRASS,

	ANDESITE_ALLOY,
	COPPER_INGOT((TaggedItem) new TaggedItem().withForgeTags("ingots/copper")),
	ZINC_INGOT((TaggedItem) new TaggedItem().withForgeTags("ingots/zinc")),
	BRASS_INGOT((TaggedItem) new TaggedItem().withForgeTags("ingots/brass")),

	FLOUR,
	DOUGH,
	OBSIDIAN_DUST,
	ROSE_QUARTZ,
	POLISHED_ROSE_QUARTZ,
	CHROMATIC_COMPOUND(ChromaticCompoundCubeItem::new, rarity(Rarity.UNCOMMON)),
	SHADOW_STEEL(ShadowSteelItem::new, rarity(Rarity.UNCOMMON)),
	REFINED_RADIANCE(RefinedRadianceItem::new, rarity(Rarity.UNCOMMON)),
	ELECTRON_TUBE,
	INTEGRATED_CIRCUIT,
	
	_2_(AllSections.KINETICS),

	BELT_CONNECTOR(BeltConnectorItem::new),
	VERTICAL_GEARBOX(VerticalGearboxItem::new),
	PROPELLER,
	WHISK,
	BRASS_HAND,
	SLOT_COVER,
	SUPER_GLUE(SuperGlueItem::new),
	SAND_PAPER(SandPaperItem::new),
	RED_SAND_PAPER(SandPaperItem::new),
	WRENCH(WrenchItem::new),
	GOGGLES(GogglesItem::new),
	MINECART_CONTRAPTION(p -> new MinecartContraptionItem(Type.RIDEABLE, p)),
	FURNACE_MINECART_CONTRAPTION(p -> new MinecartContraptionItem(Type.FURNACE, p)),

	_3_(AllSections.LOGISTICS),
	
	FILTER(FilterItem::new),
	PROPERTY_FILTER(FilterItem::new),

	_4_(AllSections.CURIOSITIES),
	
	TREE_FERTILIZER(TreeFertilizerItem::new),
	PLACEMENT_HANDGUN(BlockzapperItem::new),
	TERRAIN_ZAPPER(TerrainzapperItem::new),
	DEFORESTER(DeforesterItem::new),
	SYMMETRY_WAND(SymmetryWandItem::new),
	
	_5_(AllSections.SCHEMATICS),
	
	EMPTY_BLUEPRINT(Item::new, stackSize(1)),
	BLUEPRINT_AND_QUILL(SchematicAndQuillItem::new, stackSize(1)),
	BLUEPRINT(SchematicItem::new),

	;

	private static class SectionTracker {
		static AllSections currentSection;
	}

	// Common

	public AllSections section;
	private Function<Properties, Properties> specialProperties;
	private TaggedItem taggedItem;
	private Item item;

	AllItems(AllSections section) {
		SectionTracker.currentSection = section;
		taggedItem = new TaggedItem(null);
	}

	AllItems(Function<Properties, Item> itemSupplier) {
		this(new TaggedItem(itemSupplier), Function.identity());
	}

	AllItems(Function<Properties, Item> itemSupplier, Function<Properties, Properties> specialProperties) {
		this(new TaggedItem(itemSupplier), specialProperties);
	}

	AllItems() {
		this(new TaggedItem(Item::new));
	}

	AllItems(TaggedItem taggedItemIn) {
		this(taggedItemIn, Function.identity());
	}

	AllItems(TaggedItem taggedItemIn, Function<Properties, Properties> specialProperties) {
		this.taggedItem = taggedItemIn;
		this.section = SectionTracker.currentSection;
		this.specialProperties = specialProperties;
	}

	private static Function<Properties, Properties> rarity(Rarity rarity) {
		return p -> p.rarity(rarity);
	}

	private static Function<Properties, Properties> stackSize(int stackSize) {
		return p -> p.maxStackSize(stackSize);
	}

	private static Properties defaultProperties(AllItems item) {
		return includeInItemGroup().setISTER(() -> item::getRenderer);
	}

	public static Properties includeInItemGroup() {
		return new Properties().group(Create.baseCreativeTab);
	}

	public static void register(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> registry = event.getRegistry();

		for (AllItems entry : values()) {
			if (entry.taggedItem == null || entry.taggedItem.getItemSupplier() == null)
				continue;

			entry.item = entry.taggedItem.getItemSupplier().apply(new Properties());
			entry.item =
				entry.taggedItem.getItemSupplier().apply(entry.specialProperties.apply(defaultProperties(entry)));
			entry.item.setRegistryName(Create.ID, Lang.asId(entry.name()));
			registry.register(entry.item);
		}
	}

	public Item get() {
		return item;
	}

	public TaggedItem getTaggable() {
		return taggedItem;
	}

	public boolean typeOf(ItemStack stack) {
		return stack.getItem() == item;
	}

	public ItemStack asStack() {
		return new ItemStack(item);
	}

	public static class TaggedItem extends ITaggable.Impl {

		private Set<ResourceLocation> tagSetItem = new HashSet<>();
		private Function<Properties, Item> itemSupplier;

		public TaggedItem() {
			this(Item::new);
		}

		public TaggedItem(Function<Properties, Item> itemSupplierIn) {
			this.itemSupplier = itemSupplierIn;
		}

		public Function<Properties, Item> getItemSupplier() {
			return itemSupplier;
		}

		@Override
		public Set<ResourceLocation> getTagSet(TagType<?> type) {
			return tagSetItem;
		}
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
	@Deprecated // Use CreateRegistrate#itemColor when porting AllItems
	public static void registerColorHandlers() {
		ItemColors itemColors = Minecraft.getInstance().getItemColors();
		for (AllItems item : values()) {
			if (item.item instanceof IItemWithColorHandler) {
				itemColors.register(((IItemWithColorHandler) item.item).getColorHandler(), item.item);
			}
		}
	}

}
