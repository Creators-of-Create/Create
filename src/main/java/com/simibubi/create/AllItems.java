package com.simibubi.create;

import static com.simibubi.create.AllTags.forgeItemTag;
import static com.simibubi.create.AllTags.AllItemTags.CREATE_INGOTS;
import static com.simibubi.create.AllTags.AllItemTags.CRUSHED_ORES;
import static com.simibubi.create.AllTags.AllItemTags.PLATES;
import static com.simibubi.create.Create.REGISTRATE;
import static com.simibubi.create.content.AllSections.CURIOSITIES;
import static com.simibubi.create.content.AllSections.KINETICS;
import static com.simibubi.create.content.AllSections.LOGISTICS;
import static com.simibubi.create.content.AllSections.MATERIALS;
import static com.simibubi.create.content.AllSections.SCHEMATICS;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.ALUMINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.LEAD;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.NICKEL;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.OSMIUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.PLATINUM;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.QUICKSILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.SILVER;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.TIN;
import static com.simibubi.create.foundation.data.recipe.CompatMetals.URANIUM;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.components.structureMovement.mounted.MinecartContraptionItem;
import com.simibubi.create.content.contraptions.components.structureMovement.train.MinecartCouplingItem;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.content.contraptions.goggles.GogglesModel;
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
import com.simibubi.create.content.curiosities.armor.CopperBacktankItem.CopperBacktankBlockItem;
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
import com.simibubi.create.content.logistics.trains.management.schedule.ScheduleItem;
import com.simibubi.create.content.schematics.item.SchematicAndQuillItem;
import com.simibubi.create.content.schematics.item.SchematicItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.recipe.CompatMetals;
import com.simibubi.create.foundation.item.HiddenIngredientItem;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ForgeItemTagsProvider;

public class AllItems {

	static {
		REGISTRATE.creativeModeTab(() -> Create.BASE_CREATIVE_TAB);
	}

	// Schematics

	static {
		REGISTRATE.startSection(MATERIALS);
	}

	public static final ItemEntry<Item> WHEAT_FLOUR =
		taggedIngredient("wheat_flour", forgeItemTag("flour/wheat"), forgeItemTag("flour")),
		DOUGH = taggedIngredient("dough", forgeItemTag("dough"), forgeItemTag("dough/wheat")),
		CINDER_FLOUR = ingredient("cinder_flour"), ROSE_QUARTZ = ingredient("rose_quartz"),
		POLISHED_ROSE_QUARTZ = ingredient("polished_rose_quartz"), POWDERED_OBSIDIAN = ingredient("powdered_obsidian"),
		STURDY_SHEET = taggedIngredient("sturdy_sheet", forgeItemTag("plates/obsidian"), PLATES.tag),
		PROPELLER = ingredient("propeller"), WHISK = ingredient("whisk"), BRASS_HAND = ingredient("brass_hand"),
		CRAFTER_SLOT_COVER = ingredient("crafter_slot_cover"), ELECTRON_TUBE = ingredient("electron_tube");

	public static final ItemEntry<SequencedAssemblyItem>

	INCOMPLETE_PRECISION_MECHANISM = sequencedIngredient("incomplete_precision_mechanism"),
		INCOMPLETE_REINFORCED_SHEET = sequencedIngredient("unprocessed_obsidian_sheet"),
		INCOMPLETE_TRACK = sequencedIngredient("incomplete_track");

	public static final ItemEntry<Item> PRECISION_MECHANISM = ingredient("precision_mechanism");

	public static final ItemEntry<HiddenIngredientItem> BLAZE_CAKE_BASE =
		REGISTRATE.item("blaze_cake_base", HiddenIngredientItem::new)
			.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
			.register();

	public static final ItemEntry<CombustibleItem> BLAZE_CAKE = REGISTRATE.item("blaze_cake", CombustibleItem::new)
		.tag(AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.tag, AllItemTags.UPRIGHT_ON_BELT.tag)
		.onRegister(i -> i.setBurnTime(6400))
		.register();

	public static final ItemEntry<CombustibleItem> CREATIVE_BLAZE_CAKE =
		REGISTRATE.item("creative_blaze_cake", CombustibleItem::new)
			.properties(p -> p.rarity(Rarity.EPIC))
			.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
			.onRegister(i -> i.setBurnTime(Integer.MAX_VALUE))
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

	public static final ItemEntry<Item> RAW_ZINC =
		taggedIngredient("raw_zinc", forgeItemTag("raw_materials/zinc"), forgeItemTag("raw_materials"));

	public static final ItemEntry<Item> ANDESITE_ALLOY = taggedIngredient("andesite_alloy", CREATE_INGOTS.tag),
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

	public static final ItemEntry<TagDependentIngredientItem> CRUSHED_OSMIUM = compatCrushedOre(OSMIUM),
		CRUSHED_PLATINUM = compatCrushedOre(PLATINUM), CRUSHED_SILVER = compatCrushedOre(SILVER),
		CRUSHED_TIN = compatCrushedOre(TIN), CRUSHED_LEAD = compatCrushedOre(LEAD),
		CRUSHED_QUICKSILVER = compatCrushedOre(QUICKSILVER), CRUSHED_BAUXITE = compatCrushedOre(ALUMINUM),
		CRUSHED_URANIUM = compatCrushedOre(URANIUM), CRUSHED_NICKEL = compatCrushedOre(NICKEL);

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
		.onRegister(CreateRegistrate.itemModel(() -> GogglesModel::new))
		.lang("Engineer's Goggles")
		.register();

	public static final ItemEntry<SuperGlueItem> SUPER_GLUE = REGISTRATE.item("super_glue", SuperGlueItem::new)
		.properties(p -> p.stacksTo(1)
			.durability(99))
		.register();

	public static final ItemEntry<MinecartCouplingItem> MINECART_COUPLING =
		REGISTRATE.item("minecart_coupling", MinecartCouplingItem::new)
			.register();

	public static final ItemEntry<BlueprintItem> CRAFTING_BLUEPRINT =
		REGISTRATE.item("crafting_blueprint", BlueprintItem::new)
			.register();

	// wrapped by COPPER_BACKTANK for block placement uses.
	// must be registered as of 1.18.2
	public static final ItemEntry<CopperBacktankBlockItem> COPPER_BACKTANK_PLACEABLE = REGISTRATE
		.item("copper_backtank_placeable", p -> new CopperBacktankBlockItem(AllBlocks.COPPER_BACKTANK.get(), p))
		.model((c, p) -> p.withExistingParent(c.getName(), p.mcLoc("item/barrier")))
		.register();

	public static final ItemEntry<? extends CopperArmorItem>

	COPPER_BACKTANK = REGISTRATE.item("copper_backtank", p -> new CopperBacktankItem(p, COPPER_BACKTANK_PLACEABLE))
		.model(AssetLookup.customGenericItemModel("_", "item"))
		.tag(AllItemTags.PRESSURIZED_AIR_SOURCES.tag)
			.tag(forgeItemTag("armors/chestplates"))
		.register(),

		DIVING_HELMET = REGISTRATE.item("diving_helmet", DivingHelmetItem::new)
				.tag(forgeItemTag("armors/helmets"))
			.register(),

		DIVING_BOOTS = REGISTRATE.item("diving_boots", DivingBootsItem::new)
				.tag(forgeItemTag("armors/boots"))
			.register();

	public static final ItemEntry<SandPaperItem> SAND_PAPER = REGISTRATE.item("sand_paper", SandPaperItem::new)
		.tag(AllTags.AllItemTags.SANDPAPER.tag)
		.register();

	public static final ItemEntry<SandPaperItem> RED_SAND_PAPER = REGISTRATE.item("red_sand_paper", SandPaperItem::new)
		.tag(AllTags.AllItemTags.SANDPAPER.tag)
		.onRegister(s -> TooltipHelper.referTo(s, SAND_PAPER))
		.register();

	public static final ItemEntry<WrenchItem> WRENCH = REGISTRATE.item("wrench", WrenchItem::new)
		.properties(p -> p.stacksTo(1))
		.model(AssetLookup.itemModelWithPartials())
		.tag(AllItemTags.WRENCH.tag)
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
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<PotatoCannonItem> POTATO_CANNON =
		REGISTRATE.item("potato_cannon", PotatoCannonItem::new)
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<ExtendoGripItem> EXTENDO_GRIP = REGISTRATE.item("extendo_grip", ExtendoGripItem::new)
		.properties(p -> p.rarity(Rarity.UNCOMMON))
		.model(AssetLookup.itemModelWithPartials())
		.register();

	public static final ItemEntry<SymmetryWandItem> WAND_OF_SYMMETRY =
		REGISTRATE.item("wand_of_symmetry", SymmetryWandItem::new)
			.properties(p -> p.stacksTo(1)
				.rarity(Rarity.UNCOMMON))
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<WorldshaperItem> WORLDSHAPER =
		REGISTRATE.item("handheld_worldshaper", WorldshaperItem::new)
			.properties(p -> p.rarity(Rarity.EPIC))
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

	public static final ItemEntry<ScheduleItem> SCHEDULE = REGISTRATE.item("schedule", ScheduleItem::new)
		.lang("Train Schedule")
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

	private static ItemEntry<SequencedAssemblyItem> sequencedIngredient(String name) {
		return REGISTRATE.item(name, SequencedAssemblyItem::new)
			.register();
	}

//	private static ItemEntry<HiddenIngredientItem> hiddenIngredient(String name) {
//		return REGISTRATE.item(name, HiddenIngredientItem::new)
//			.register();
//	}

	@SafeVarargs
	private static ItemEntry<Item> taggedIngredient(String name, TagKey<Item>... tags) {
		return REGISTRATE.item(name, Item::new)
			.tag(tags)
			.register();
	}

	private static ItemEntry<TagDependentIngredientItem> compatCrushedOre(CompatMetals metal) {
		String metalName = metal.getName();
		return REGISTRATE
			.item("crushed_" + metalName + "_ore",
				props -> new TagDependentIngredientItem(props, AllTags.forgeItemTag("ores/" + metalName)))
			.tag(CRUSHED_ORES.tag)
			.register();
	}

	// Load this class

	public static void register() {}

}
