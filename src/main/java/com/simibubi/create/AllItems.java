package com.simibubi.create;

import static com.simibubi.create.AllTags.forgeItemTag;
import static com.simibubi.create.AllTags.AllItemTags.CREATE_INGOTS;
import static com.simibubi.create.AllTags.AllItemTags.CRUSHED_ORES;
import static com.simibubi.create.AllTags.AllItemTags.PLATES;
import static com.simibubi.create.content.AllSections.CURIOSITIES;
import static com.simibubi.create.content.AllSections.KINETICS;
import static com.simibubi.create.content.AllSections.LOGISTICS;
import static com.simibubi.create.content.AllSections.MATERIALS;
import static com.simibubi.create.content.AllSections.SCHEMATICS;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MinecartContraptionItem;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingItem;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyItem;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlockItem;
import com.simibubi.create.content.contraptions.relays.belt.item.BeltConnectorItem;
import com.simibubi.create.content.contraptions.relays.gearbox.VerticalGearboxItem;
import com.simibubi.create.content.contraptions.wrench.WrenchItem;
import com.simibubi.create.content.curiosities.BuildersTeaItem;
import com.simibubi.create.content.curiosities.ChromaticCompoundColor;
import com.simibubi.create.content.curiosities.ChromaticCompoundItem;
import com.simibubi.create.content.curiosities.CombustibleItem;
import com.simibubi.create.content.curiosities.ExperienceNuggetItem;
import com.simibubi.create.content.curiosities.RefinedRadianceItem;
import com.simibubi.create.content.curiosities.ShadowSteelItem;
import com.simibubi.create.content.curiosities.TreeFertilizerItem;
import com.simibubi.create.content.curiosities.armor.CopperArmorItem;
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem;
import com.simibubi.create.content.curiosities.armor.DivingBootsItem;
import com.simibubi.create.content.curiosities.armor.DivingHelmetItem;
import com.simibubi.create.content.curiosities.symmetry.SymmetryWandItem;
import com.simibubi.create.content.curiosities.tools.BlueprintItem;
import com.simibubi.create.content.curiosities.tools.ExtendoGripItem;
import com.simibubi.create.content.curiosities.tools.SandPaperItem;
import com.simibubi.create.content.curiosities.weapons.PotatoCannonItem;
import com.simibubi.create.content.curiosities.zapper.terrainzapper.WorldshaperItem;
import com.simibubi.create.content.logistics.item.LinkedControllerItem;
import com.simibubi.create.content.logistics.item.filter.FilterItem;
import com.simibubi.create.content.schematics.item.SchematicAndQuillItem;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.HiddenIngredientItem;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.tterrag.registrate.util.entry.ItemEntry;

import me.alphamode.forgetags.Tags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class AllItems {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.creativeModeTab(() -> Create.BASE_CREATIVE_TAB);

	// Schematics

	static {
		REGISTRATE.startSection(MATERIALS);
	}

	public static final ItemEntry<Item> WHEAT_FLOUR = ingredient("wheat_flour"), DOUGH = ingredient("dough"),
		CINDER_FLOUR = ingredient("cinder_flour"), ROSE_QUARTZ = ingredient("rose_quartz"),
		POLISHED_ROSE_QUARTZ = ingredient("polished_rose_quartz"), PROPELLER = ingredient("propeller"),
		WHISK = ingredient("whisk"), BRASS_HAND = ingredient("brass_hand"),
		CRAFTER_SLOT_COVER = ingredient("crafter_slot_cover"), ELECTRON_TUBE = ingredient("electron_tube");

	public static final ItemEntry<HiddenIngredientItem> POWDERED_OBSIDIAN = hiddenIngredient("powdered_obsidian");

	public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_PRECISION_MECHANISM =
		REGISTRATE.item("incomplete_precision_mechanism", SequencedAssemblyItem::new)
			.register();

	public static final ItemEntry<Item> PRECISION_MECHANISM = ingredient("precision_mechanism");

	public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_COGWHEEL =
			REGISTRATE.item("incomplete_cogwheel", SequencedAssemblyItem::new)
					.model(AssetLookup.existingItemModel())
					.register();

	public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_LARGE_COGWHEEL =
			REGISTRATE.item("incomplete_large_cogwheel", SequencedAssemblyItem::new)
					.model(AssetLookup.existingItemModel())
					.register();

	public static final ItemEntry<HiddenIngredientItem> BLAZE_CAKE_BASE =
		REGISTRATE.item("blaze_cake_base", HiddenIngredientItem::new)
			.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
			.register();

	public static final ItemEntry<CombustibleItem> BLAZE_CAKE = REGISTRATE.item("blaze_cake", CombustibleItem::new)
		.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
		.onRegister(i -> i.setBurnTime(6400))
		.register();

	public static final ItemEntry<CombustibleItem> CREATIVE_BLAZE_CAKE =
		REGISTRATE.item("creative_blaze_cake", CombustibleItem::new)
			.properties(p -> p.rarity(Rarity.EPIC))
			.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
	//		.onRegister(i -> i.setBurnTime(Integer.MAX_VALUE)) // handled in AbstractFurnaceBlockEntityMixin, networking bad
			.register();

	public static final ItemEntry<Item> BAR_OF_CHOCOLATE = REGISTRATE.item("bar_of_chocolate", Item::new)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(6)
			.saturationMod(0.3F)
			.build()))
		.lang("Bar of Chocolate")
		.register();

	public static final ItemEntry<Item> SWEET_ROLL = REGISTRATE.item("sweet_roll", Item::new)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(6)
			.saturationMod(0.8F)
			.build()))
		.register();

	public static final ItemEntry<Item> CHOCOLATE_BERRIES = REGISTRATE.item("chocolate_glazed_berries", Item::new)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(7)
			.saturationMod(0.8F)
			.build()))
		.register();

	public static final ItemEntry<Item> HONEYED_APPLE = REGISTRATE.item("honeyed_apple", Item::new)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(8)
			.saturationMod(0.8F)
			.build()))
		.register();

	public static final ItemEntry<BuildersTeaItem> BUILDERS_TEA = REGISTRATE.item("builders_tea", BuildersTeaItem::new)
		.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
		.properties(p -> p.stacksTo(16))
		.lang("Builder's Tea")
		.register();

	public static final ItemEntry<Item> RAW_ZINC = ingredient("raw_zinc");

	public static final ItemEntry<Item> ANDESITE_ALLOY = ingredient("andesite_alloy"),
		ZINC_INGOT = taggedIngredient("zinc_ingot", forgeItemTag("ingots/zinc"), CREATE_INGOTS.tag),
		BRASS_INGOT = taggedIngredient("brass_ingot", forgeItemTag("ingots/brass"), CREATE_INGOTS.tag);

	public static final ItemEntry<ChromaticCompoundItem> CHROMATIC_COMPOUND =
		REGISTRATE.item("chromatic_compound", ChromaticCompoundItem::new)
			.properties(p -> p.rarity(Rarity.UNCOMMON))
			.model(AssetLookup.existingItemModel())
			.color(() -> ChromaticCompoundColor::new)
			.register();

	public static final ItemEntry<ShadowSteelItem> SHADOW_STEEL = REGISTRATE.item("shadow_steel", ShadowSteelItem::new)
		.properties(p -> p.rarity(Rarity.UNCOMMON))
		.register();

	public static final ItemEntry<RefinedRadianceItem> REFINED_RADIANCE =
		REGISTRATE.item("refined_radiance", RefinedRadianceItem::new)
			.properties(p -> p.rarity(Rarity.UNCOMMON))
			.register();

	public static final ItemEntry<Item> COPPER_NUGGET =
		taggedIngredient("copper_nugget", forgeItemTag("nuggets/copper"), Tags.Items.NUGGETS),
		ZINC_NUGGET = taggedIngredient("zinc_nugget", forgeItemTag("nuggets/zinc"), Tags.Items.NUGGETS),
		BRASS_NUGGET = taggedIngredient("brass_nugget", forgeItemTag("nuggets/brass"), Tags.Items.NUGGETS);

	public static final ItemEntry<ExperienceNuggetItem> EXP_NUGGET =
		REGISTRATE.item("experience_nugget", ExperienceNuggetItem::new)
			.tag(Tags.Items.NUGGETS)
			.properties(p -> p.rarity(Rarity.UNCOMMON))
			.lang("Nugget of Experience")
			.register();

	public static final ItemEntry<Item> COPPER_SHEET =
		taggedIngredient("copper_sheet", forgeItemTag("plates/copper"), PLATES.tag),
		BRASS_SHEET = taggedIngredient("brass_sheet", forgeItemTag("plates/brass"), PLATES.tag),
		IRON_SHEET = taggedIngredient("iron_sheet", forgeItemTag("plates/iron"), PLATES.tag),
		GOLDEN_SHEET = taggedIngredient("golden_sheet", forgeItemTag("plates/gold"), PLATES.tag, ItemTags.PIGLIN_LOVED),

		CRUSHED_IRON = taggedIngredient("crushed_iron_ore", CRUSHED_ORES.tag),
		CRUSHED_GOLD = taggedIngredient("crushed_gold_ore", CRUSHED_ORES.tag, ItemTags.PIGLIN_LOVED),
		CRUSHED_COPPER = taggedIngredient("crushed_copper_ore", CRUSHED_ORES.tag),
		CRUSHED_ZINC = taggedIngredient("crushed_zinc_ore", CRUSHED_ORES.tag);

	public static final ItemEntry<TagDependentIngredientItem> CRUSHED_OSMIUM = compatCrushedOre("osmium"),
		CRUSHED_PLATINUM = compatCrushedOre("platinum"), CRUSHED_SILVER = compatCrushedOre("silver"),
		CRUSHED_TIN = compatCrushedOre("tin"), CRUSHED_LEAD = compatCrushedOre("lead"),
		CRUSHED_QUICKSILVER = compatCrushedOre("quicksilver"), CRUSHED_BAUXITE = compatCrushedOre("aluminum"),
		CRUSHED_URANIUM = compatCrushedOre("uranium"), CRUSHED_NICKEL = compatCrushedOre("nickel");

	// Kinetics

	static {
		REGISTRATE.startSection(KINETICS);
	}

	public static final ItemEntry<BeltConnectorItem> BELT_CONNECTOR =
		REGISTRATE.item("belt_connector", BeltConnectorItem::new)
			.lang("Mechanical Belt")
			.register();

	public static final ItemEntry<VerticalGearboxItem> VERTICAL_GEARBOX =
		REGISTRATE.item("vertical_gearbox", VerticalGearboxItem::new)
			.model(AssetLookup.customBlockItemModel("gearbox", "item_vertical"))
			.register();

	public static final ItemEntry<BlazeBurnerBlockItem> EMPTY_BLAZE_BURNER =
		REGISTRATE.item("empty_blaze_burner", BlazeBurnerBlockItem::empty)
			.model(AssetLookup.customBlockItemModel("blaze_burner", "block"))
			.register();

	public static final ItemEntry<GogglesItem> GOGGLES = REGISTRATE.item("goggles", GogglesItem::new)
		.properties(p -> p.stacksTo(1))
//		.onRegister(CreateRegistrate.itemModel(() -> GogglesModel::new))
		.lang("Engineer's Goggles")
		.register();

	public static final ItemEntry<SuperGlueItem> SUPER_GLUE = REGISTRATE.item("super_glue", SuperGlueItem::new)
		.register();

	public static final ItemEntry<MinecartCouplingItem> MINECART_COUPLING =
		REGISTRATE.item("minecart_coupling", MinecartCouplingItem::new)
			.register();

	public static final ItemEntry<BlueprintItem> CRAFTING_BLUEPRINT =
		REGISTRATE.item("crafting_blueprint", BlueprintItem::new)
			.register();

	public static final ItemEntry<? extends CopperArmorItem>

	COPPER_BACKTANK =
		REGISTRATE
			.item("copper_backtank", p -> new CopperBacktankItem(p, new BlockItem(AllBlocks.COPPER_BACKTANK.get(), p)))
			.model(AssetLookup.<CopperBacktankItem>customGenericItemModel("_", "item"))
			.register(),

		DIVING_HELMET = REGISTRATE.item("diving_helmet", DivingHelmetItem::new)
			.register(),

		DIVING_BOOTS = REGISTRATE.item("diving_boots", DivingBootsItem::new)
			.register();

	public static final ItemEntry<SandPaperItem> SAND_PAPER = REGISTRATE.item("sand_paper", SandPaperItem::new)
//		.transform(CreateRegistrate.customRenderedItem(() -> SandPaperItemRenderer::new))
		.tag(AllTags.AllItemTags.SANDPAPER.tag)
		.register();

	public static final ItemEntry<SandPaperItem> RED_SAND_PAPER = REGISTRATE.item("red_sand_paper", SandPaperItem::new)
//		.transform(CreateRegistrate.customRenderedItem(() -> SandPaperItemRenderer::new))
		.tag(AllTags.AllItemTags.SANDPAPER.tag)
		.onRegister(s -> TooltipHelper.referTo(s, SAND_PAPER))
		.register();

	public static final ItemEntry<WrenchItem> WRENCH = REGISTRATE.item("wrench", WrenchItem::new)
		.properties(p -> p.stacksTo(1))
//		.transform(CreateRegistrate.customRenderedItem(() -> WrenchItemRenderer::new))
		.model(AssetLookup.itemModelWithPartials())
		.register();

	public static final ItemEntry<MinecartContraptionItem> MINECART_CONTRAPTION =
		REGISTRATE.item("minecart_contraption", MinecartContraptionItem::rideable)
			.register();

	public static final ItemEntry<MinecartContraptionItem> FURNACE_MINECART_CONTRAPTION =
		REGISTRATE.item("furnace_minecart_contraption", MinecartContraptionItem::furnace)
			.register();

	public static final ItemEntry<MinecartContraptionItem> CHEST_MINECART_CONTRAPTION =
		REGISTRATE.item("chest_minecart_contraption", MinecartContraptionItem::chest)
			.register();

	// Curiosities

	static {
		REGISTRATE.startSection(CURIOSITIES);
	}

	public static final ItemEntry<LinkedControllerItem> LINKED_CONTROLLER =
		REGISTRATE.item("linked_controller", LinkedControllerItem::new)
			.properties(p -> p.stacksTo(1))
//			.transform(CreateRegistrate.customRenderedItem(() -> LinkedControllerItemRenderer::new))
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<PotatoCannonItem> POTATO_CANNON =
		REGISTRATE.item("potato_cannon", PotatoCannonItem::new)
			.properties(p -> p.stacksTo(1))
//			.transform(CreateRegistrate.customRenderedItem(() -> PotatoCannonItemRenderer::new))
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<ExtendoGripItem> EXTENDO_GRIP = REGISTRATE.item("extendo_grip", ExtendoGripItem::new)
//		.transform(CreateRegistrate.customRenderedItem(() -> ExtendoGripItemRenderer::new))
		.model(AssetLookup.itemModelWithPartials())
		.register();

	public static final ItemEntry<SymmetryWandItem> WAND_OF_SYMMETRY =
		REGISTRATE.item("wand_of_symmetry", SymmetryWandItem::new)
//			.transform(CreateRegistrate.customRenderedItem(() -> SymmetryWandItemRenderer::new))
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<WorldshaperItem> WORLDSHAPER =
		REGISTRATE.item("handheld_worldshaper", WorldshaperItem::new)
			.properties(p -> p.rarity(Rarity.EPIC))
//			.transform(CreateRegistrate.customRenderedItem(() -> WorldshaperItemRenderer::new))
			.lang("Creative Worldshaper")
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<TreeFertilizerItem> TREE_FERTILIZER =
		REGISTRATE.item("tree_fertilizer", TreeFertilizerItem::new)
			.register();

	// Logistics

	static {
		REGISTRATE.startSection(LOGISTICS);
	}

	public static final ItemEntry<FilterItem> FILTER = REGISTRATE.item("filter", FilterItem::regular)
		.model(AssetLookup.existingItemModel())
		.register();

	public static final ItemEntry<FilterItem> ATTRIBUTE_FILTER =
		REGISTRATE.item("attribute_filter", FilterItem::attribute)
			.model(AssetLookup.existingItemModel())
			.register();

	// Schematics

	static {
		REGISTRATE.startSection(SCHEMATICS);
	}

	public static final ItemEntry<Item> EMPTY_SCHEMATIC = REGISTRATE.item("empty_schematic", Item::new)
		.properties(p -> p.stacksTo(1))
		.register();

	public static final ItemEntry<SchematicAndQuillItem> SCHEMATIC_AND_QUILL =
		REGISTRATE.item("schematic_and_quill", SchematicAndQuillItem::new)
			.properties(p -> p.stacksTo(1))
			.register();

	public static final ItemEntry<SchematicItem> SCHEMATIC = REGISTRATE.item("schematic", SchematicItem::new)
		.properties(p -> p.stacksTo(1))
		.register();

	// Shortcuts

	private static ItemEntry<Item> ingredient(String name) {
		return REGISTRATE.item(name, Item::new)
			.register();
	}

	private static ItemEntry<HiddenIngredientItem> hiddenIngredient(String name) {
		return REGISTRATE.item(name, HiddenIngredientItem::new)
			.register();
	}

	@SafeVarargs
	private static ItemEntry<Item> taggedIngredient(String name, Tag.Named<Item>... tags) {
		return REGISTRATE.item(name, Item::new)
			.tag(tags)
			.register();
	}

	private static ItemEntry<TagDependentIngredientItem> compatCrushedOre(String metalName) {
		return REGISTRATE
			.item("crushed_" + metalName + "_ore",
				props -> new TagDependentIngredientItem(props, new ResourceLocation("forge", "ores/" + metalName)))
			.tag(CRUSHED_ORES.tag)
			.register();
	}

	// Load this class

	public static void register() {}

}
