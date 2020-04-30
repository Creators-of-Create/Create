package com.simibubi.create;

import static com.simibubi.create.foundation.item.AllToolTypes.AXE;
import static com.simibubi.create.foundation.item.AllToolTypes.HOE;
import static com.simibubi.create.foundation.item.AllToolTypes.PICKAXE;
import static com.simibubi.create.foundation.item.AllToolTypes.SHOVEL;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import com.simibubi.create.foundation.item.IHaveCustomItemModel;
import com.simibubi.create.foundation.item.IItemWithColorHandler;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.data.ITaggable;
import com.simibubi.create.modules.IModule;
import com.simibubi.create.modules.contraptions.GogglesItem;
import com.simibubi.create.modules.contraptions.WrenchItem;
import com.simibubi.create.modules.contraptions.components.contraptions.glue.SuperGlueItem;
import com.simibubi.create.modules.contraptions.components.contraptions.mounted.MinecartContraptionItem;
import com.simibubi.create.modules.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.modules.contraptions.relays.gearbox.VerticalGearboxItem;
import com.simibubi.create.modules.curiosities.ChromaticCompoundCubeItem;
import com.simibubi.create.modules.curiosities.RefinedRadianceItem;
import com.simibubi.create.modules.curiosities.ShadowSteelItem;
import com.simibubi.create.modules.curiosities.deforester.DeforesterItem;
import com.simibubi.create.modules.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.modules.curiosities.tools.AllToolTiers;
import com.simibubi.create.modules.curiosities.tools.BlazingToolItem;
import com.simibubi.create.modules.curiosities.tools.RoseQuartzToolItem;
import com.simibubi.create.modules.curiosities.tools.SandPaperItem;
import com.simibubi.create.modules.curiosities.tools.ShadowSteelToolItem;
import com.simibubi.create.modules.curiosities.zapper.blockzapper.BlockzapperItem;
import com.simibubi.create.modules.curiosities.zapper.terrainzapper.TerrainzapperItem;
import com.simibubi.create.modules.gardens.TreeFertilizerItem;
import com.simibubi.create.modules.logistics.item.filter.FilterItem;
import com.simibubi.create.modules.schematics.item.SchematicAndQuillItem;
import com.simibubi.create.modules.schematics.item.SchematicItem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity.Type;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Properties;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD)
public enum AllItems {

	__MATERIALS__(module()),
	COPPER_NUGGET(new TaggedItem().withForgeTags("nuggets/copper")),
	ZINC_NUGGET(new TaggedItem().withForgeTags("nuggets/zinc")),
	BRASS_NUGGET(new TaggedItem().withForgeTags("nuggets/brass")),
	IRON_SHEET(new TaggedItem().withForgeTags("plates/iron")),
	GOLD_SHEET(new TaggedItem().withForgeTags("plates/gold")),
	COPPER_SHEET(new TaggedItem().withForgeTags("plates/copper")),
	BRASS_SHEET(new TaggedItem().withForgeTags("plates/brass")),
	LAPIS_PLATE(new TaggedItem().withForgeTags("plates/lapis")),

	CRUSHED_IRON,
	CRUSHED_GOLD,
	CRUSHED_COPPER,
	CRUSHED_ZINC,
	CRUSHED_BRASS,

	ANDESITE_ALLOY,
	COPPER_INGOT(new TaggedItem().withForgeTags("ingots/copper")),
	ZINC_INGOT(new TaggedItem().withForgeTags("ingots/zinc")),
	BRASS_INGOT(new TaggedItem().withForgeTags("ingots/brass")),

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

	__CONTRAPTIONS__(module()),
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

	__LOGISTICS__(module()),
	FILTER(FilterItem::new),
	PROPERTY_FILTER(FilterItem::new),

	__CURIOSITIES__(module()),
	TREE_FERTILIZER(TreeFertilizerItem::new),
	PLACEMENT_HANDGUN(BlockzapperItem::new),
	TERRAIN_ZAPPER(TerrainzapperItem::new),
	DEFORESTER(DeforesterItem::new),
	SYMMETRY_WAND(SymmetryWandItem::new),
	ZINC_HANDLE,

	BLAZING_PICKAXE(p -> new BlazingToolItem(1, -2.8F, p, PICKAXE)),
	BLAZING_SHOVEL(p -> new BlazingToolItem(1.5F, -3.0F, p, SHOVEL)),
	BLAZING_AXE(p -> new BlazingToolItem(5.0F, -3.0F, p, AXE)),
	BLAZING_SWORD(p -> new SwordItem(AllToolTiers.BLAZING, 3, -2.4F, p)),

	ROSE_QUARTZ_PICKAXE(p -> new RoseQuartzToolItem(1, -2.8F, p, PICKAXE)),
	ROSE_QUARTZ_SHOVEL(p -> new RoseQuartzToolItem(1.5F, -3.0F, p, SHOVEL)),
	ROSE_QUARTZ_AXE(p -> new RoseQuartzToolItem(5.0F, -3.0F, p, AXE)),
	ROSE_QUARTZ_SWORD(p -> new SwordItem(AllToolTiers.ROSE_QUARTZ, 3, -2.4F, p)),

	SHADOW_STEEL_PICKAXE(p -> new ShadowSteelToolItem(2.5F, -2.0F, p, PICKAXE)),
	SHADOW_STEEL_MATTOCK(p -> new ShadowSteelToolItem(2.5F, -1.5F, p, SHOVEL, AXE, HOE)),
	SHADOW_STEEL_SWORD(p -> new SwordItem(AllToolTiers.SHADOW_STEEL, 3, -2.0F, p)),

	__SCHEMATICS__(module()),
	EMPTY_BLUEPRINT(Item::new, stackSize(1)),
	BLUEPRINT_AND_QUILL(SchematicAndQuillItem::new, stackSize(1)),
	BLUEPRINT(SchematicItem::new),

	;

	private static class CategoryTracker {
		static IModule currentModule;
	}

	// Common

	public IModule module;
	private Function<Properties, Properties> specialProperties;
	private TaggedItem taggedItem;
	private Item item;

	AllItems(int moduleMarker) {
		CategoryTracker.currentModule = () -> Lang.asId(name()).replaceAll("__", "");
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
			if (entry.taggedItem == null || entry.taggedItem.getItemSupplier() == null)
				continue;

			entry.item = entry.taggedItem.getItemSupplier().apply(new Properties());
			entry.item =
				entry.taggedItem.getItemSupplier().apply(entry.specialProperties.apply(defaultProperties(entry)));
			entry.item.setRegistryName(Create.ID, Lang.asId(entry.name()));
			registry.register(entry.item);
		}

		AllBlocks.registerItemBlocks(registry);
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

	public static class TaggedItem implements ITaggable<TaggedItem> {

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
		public Set<ResourceLocation> getTagSet(TagType type) {
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
	public static void registerColorHandlers() {
		ItemColors itemColors = Minecraft.getInstance().getItemColors();
		for (AllItems item : values()) {
			if (item.item instanceof IItemWithColorHandler) {
				itemColors.register(((IItemWithColorHandler) item.item).getColorHandler(), item.item);
			}
		}
	}

}
