package com.simibubi.create;

import static com.simibubi.create.AllTags.AllItemTags.CREATE_INGOTS;
import static com.simibubi.create.AllTags.AllItemTags.CRUSHED_RAW_MATERIALS;
import static com.simibubi.create.AllTags.AllItemTags.PLATES;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.ALUMINUM;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.LEAD;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.NICKEL;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.OSMIUM;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.PLATINUM;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.QUICKSILVER;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.SILVER;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.TIN;
import static com.simibubi.create.foundation.data.recipe.CommonMetal.URANIUM;

import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;
import com.simibubi.create.api.registry.CreateDataMaps;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.minecart.MinecartCouplingItem;
import com.simibubi.create.content.contraptions.mounted.MinecartContraptionItem;
import com.simibubi.create.content.equipment.BuildersTeaItem;
import com.simibubi.create.content.equipment.TreeFertilizerItem;
import com.simibubi.create.content.equipment.armor.AllArmorMaterials;
import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankItem.BacktankBlockItem;
import com.simibubi.create.content.equipment.armor.BaseArmorItem;
import com.simibubi.create.content.equipment.armor.CardboardArmorItem;
import com.simibubi.create.content.equipment.armor.CardboardArmorStealthOverlay;
import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.simibubi.create.content.equipment.armor.TrimmableArmorModelGenerator;
import com.simibubi.create.content.equipment.blueprint.BlueprintItem;
import com.simibubi.create.content.equipment.extendoGrip.ExtendoGripItem;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.equipment.goggles.GogglesModel;
import com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.simibubi.create.content.equipment.sandPaper.SandPaperItem;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.content.equipment.tool.AllToolMaterials;
import com.simibubi.create.content.equipment.tool.CardboardSwordItem;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import com.simibubi.create.content.equipment.zapper.terrainzapper.WorldshaperItem;
import com.simibubi.create.content.kinetics.belt.item.BeltConnectorItem;
import com.simibubi.create.content.kinetics.gearbox.VerticalGearboxItem;
import com.simibubi.create.content.legacy.ChromaticCompoundColor;
import com.simibubi.create.content.legacy.ChromaticCompoundItem;
import com.simibubi.create.content.legacy.RefinedRadianceItem;
import com.simibubi.create.content.legacy.ShadowSteelItem;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.box.PackageStyles.PackageStyle;
import com.simibubi.create.content.logistics.filter.AttributeFilterItem;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.content.logistics.filter.ListFilterItem;
import com.simibubi.create.content.logistics.filter.PackageFilterItem;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem;
import com.simibubi.create.content.materials.ExperienceNuggetItem;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockItem;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import com.simibubi.create.content.schematics.SchematicAndQuillItem;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.trains.schedule.ScheduleItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BuilderTransformers;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TagDependentIngredientItem;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.Tags.Items;

public class AllItems {
	private static final CreateRegistrate REGISTRATE = Create.registrate();

	static {
		REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
	}

	public static final ItemEntry<Item>
		WHEAT_FLOUR = taggedIngredient("wheat_flour", AllItemTags.WHEAT_FLOURS.tag, AllItemTags.FLOURS.tag),
		DOUGH = taggedIngredient("dough", AllItemTags.DOUGHS.tag, AllItemTags.WHEAT_DOUGHS.tag),
		CINDER_FLOUR = ingredient("cinder_flour"), ROSE_QUARTZ = ingredient("rose_quartz"),
		POLISHED_ROSE_QUARTZ = ingredient("polished_rose_quartz"), POWDERED_OBSIDIAN = ingredient("powdered_obsidian"),
		STURDY_SHEET = taggedIngredient("sturdy_sheet", AllItemTags.OBSIDIAN_PLATES.tag, PLATES.tag),
		PROPELLER = ingredient("propeller"), WHISK = ingredient("whisk"), BRASS_HAND = ingredient("brass_hand"),
		CRAFTER_SLOT_COVER = ingredient("crafter_slot_cover"), ELECTRON_TUBE = ingredient("electron_tube"),
		TRANSMITTER = ingredient("transmitter"), PULP = ingredient("pulp");

	public static final ItemEntry<Item> CARDBOARD = REGISTRATE.item("cardboard", Item::new)
		.tag(AllItemTags.CARDBOARD_PLATES.tag, PLATES.tag)
		.burnTime(1000)
		.register();

	public static final ItemEntry<SequencedAssemblyItem>
		INCOMPLETE_PRECISION_MECHANISM = sequencedIngredient("incomplete_precision_mechanism"),
		INCOMPLETE_REINFORCED_SHEET = sequencedIngredient("unprocessed_obsidian_sheet"),
		INCOMPLETE_TRACK = sequencedIngredient("incomplete_track");

	public static final ItemEntry<Item> PRECISION_MECHANISM = ingredient("precision_mechanism");

	public static final ItemEntry<Item> BLAZE_CAKE_BASE = REGISTRATE.item("blaze_cake_base", Item::new)
		.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
		.register();

	public static final ItemEntry<Item> BLAZE_CAKE = REGISTRATE.item("blaze_cake", Item::new)
		.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
		.dataMap(CreateDataMaps.SUPERHEATED_BLAZE_BURNER_FUELS, new BlazeBurnerFuel(3200))
		.burnTime(6400)
		.register();

	public static final ItemEntry<Item> CREATIVE_BLAZE_CAKE =
		REGISTRATE.item("creative_blaze_cake", Item::new)
			.properties(p -> p.rarity(Rarity.EPIC))
			.tag(AllItemTags.UPRIGHT_ON_BELT.tag)
			.burnTime(Integer.MAX_VALUE)
			.register();

	public static final ItemEntry<Item> BAR_OF_CHOCOLATE = REGISTRATE.item("bar_of_chocolate", Item::new)
		.tag(Items.FOODS, AllItemTags.FOODS_CHOCOLATE.tag)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(6)
			.saturationModifier(0.3F)
			.build()))
		.lang("Bar of Chocolate")
		.register();

	public static final ItemEntry<Item> SWEET_ROLL = REGISTRATE.item("sweet_roll", Item::new)
		.tag(Items.FOODS)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(6)
			.saturationModifier(0.8F)
			.build()))
		.register();

	public static final ItemEntry<Item> CHOCOLATE_BERRIES = REGISTRATE.item("chocolate_glazed_berries", Item::new)
		.tag(Items.FOODS, Items.FOODS_BERRY)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(7)
			.saturationModifier(0.8F)
			.build()))
		.register();

	public static final ItemEntry<Item> HONEYED_APPLE = REGISTRATE.item("honeyed_apple", Item::new)
		.tag(Items.FOODS, Items.FOODS_FRUIT)
		.properties(p -> p.food(new FoodProperties.Builder().nutrition(8)
			.saturationModifier(0.8F)
			.build()))
		.register();

	public static final ItemEntry<BuildersTeaItem> BUILDERS_TEA = REGISTRATE.item("builders_tea", BuildersTeaItem::new)
		.tag(AllItemTags.UPRIGHT_ON_BELT.tag, Items.FOODS, Items.DRINKS, AllItemTags.DRINKS_TEA.tag)
		.properties(p -> p
			.stacksTo(16)
			.food(new FoodProperties.Builder()
				.nutrition(1)
				.saturationModifier(.6F)
				.alwaysEdible()
				.effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 3 * 60 * 20, 0, false, false, false), 1F)
				.build()
			)
		)
		.lang("Builder's Tea")
		.register();

	public static final ItemEntry<CardboardSwordItem> CARDBOARD_SWORD =
		REGISTRATE.item("cardboard_sword", CardboardSwordItem::new)
			.burnTime(1000)
			.properties(p -> p.stacksTo(1))
			.properties(p -> p.attributes(SwordItem.createAttributes(AllToolMaterials.CARDBOARD, 3, 1)))
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<Item> RAW_ZINC =
		taggedIngredient("raw_zinc", CommonMetal.ZINC.rawOres, Items.RAW_MATERIALS);

	public static final ItemEntry<Item> ANDESITE_ALLOY = taggedIngredient("andesite_alloy", CREATE_INGOTS.tag),
		ZINC_INGOT = taggedIngredient("zinc_ingot", CommonMetal.ZINC.ingots, CREATE_INGOTS.tag),
		BRASS_INGOT = taggedIngredient("brass_ingot", CommonMetal.BRASS.ingots, CREATE_INGOTS.tag);

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
		taggedIngredient("copper_nugget", CommonMetal.COPPER.nuggets, Tags.Items.NUGGETS),
		ZINC_NUGGET = taggedIngredient("zinc_nugget", CommonMetal.ZINC.nuggets, Tags.Items.NUGGETS),
		BRASS_NUGGET = taggedIngredient("brass_nugget", CommonMetal.BRASS.nuggets, Tags.Items.NUGGETS);

	public static final ItemEntry<ExperienceNuggetItem> EXP_NUGGET =
		REGISTRATE.item("experience_nugget", ExperienceNuggetItem::new)
			.tag(Tags.Items.NUGGETS)
			.properties(p -> p.rarity(Rarity.UNCOMMON))
			.lang("Nugget of Experience")
			.register();

	public static final ItemEntry<Item> COPPER_SHEET =
		taggedIngredient("copper_sheet", CommonMetal.COPPER.plates, PLATES.tag),
		BRASS_SHEET = taggedIngredient("brass_sheet", CommonMetal.BRASS.plates, PLATES.tag),
		IRON_SHEET = taggedIngredient("iron_sheet", CommonMetal.IRON.plates, PLATES.tag),
		GOLDEN_SHEET = taggedIngredient("golden_sheet", CommonMetal.GOLD.plates, PLATES.tag, ItemTags.PIGLIN_LOVED),

	CRUSHED_IRON = taggedIngredient("crushed_raw_iron", CRUSHED_RAW_MATERIALS.tag),
		CRUSHED_GOLD = taggedIngredient("crushed_raw_gold", CRUSHED_RAW_MATERIALS.tag, ItemTags.PIGLIN_LOVED),
		CRUSHED_COPPER = taggedIngredient("crushed_raw_copper", CRUSHED_RAW_MATERIALS.tag),
		CRUSHED_ZINC = taggedIngredient("crushed_raw_zinc", CRUSHED_RAW_MATERIALS.tag);

	public static final ItemEntry<TagDependentIngredientItem> CRUSHED_OSMIUM = compatCrushedOre(OSMIUM),
		CRUSHED_PLATINUM = compatCrushedOre(PLATINUM), CRUSHED_SILVER = compatCrushedOre(SILVER),
		CRUSHED_TIN = compatCrushedOre(TIN), CRUSHED_LEAD = compatCrushedOre(LEAD),
		CRUSHED_QUICKSILVER = compatCrushedOre(QUICKSILVER), CRUSHED_BAUXITE = compatCrushedOre(ALUMINUM),
		CRUSHED_URANIUM = compatCrushedOre(URANIUM), CRUSHED_NICKEL = compatCrushedOre(NICKEL);

	// Kinetics

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
		.tag(ItemTags.DURABILITY_ENCHANTABLE)
		.register();

	public static final ItemEntry<MinecartCouplingItem> MINECART_COUPLING =
		REGISTRATE.item("minecart_coupling", MinecartCouplingItem::new)
			.register();

	public static final ItemEntry<BlueprintItem> CRAFTING_BLUEPRINT =
		REGISTRATE.item("crafting_blueprint", BlueprintItem::new)
			.register();

	// wrapped by COPPER_BACKTANK for block placement uses.
	// must be registered as of 1.18.2
	public static final ItemEntry<BacktankBlockItem> COPPER_BACKTANK_PLACEABLE = REGISTRATE
		.item("copper_backtank_placeable",
			p -> new BacktankBlockItem(AllBlocks.COPPER_BACKTANK.get(), AllItems.COPPER_BACKTANK::get, p))
		.model((c, p) -> p.withExistingParent(c.getName(), p.mcLoc("item/barrier")))
		.register();

	// wrapped by NETHERITE_BACKTANK for block placement uses.
	// must be registered as of 1.18.2
	public static final ItemEntry<BacktankBlockItem> NETHERITE_BACKTANK_PLACEABLE = REGISTRATE
		.item("netherite_backtank_placeable",
			p -> new BacktankBlockItem(AllBlocks.NETHERITE_BACKTANK.get(), AllItems.NETHERITE_BACKTANK::get, p))
		.model((c, p) -> p.withExistingParent(c.getName(), p.mcLoc("item/barrier")))
		.register();

	public static final ItemEntry<? extends BacktankItem>

		COPPER_BACKTANK =
		REGISTRATE
			.item("copper_backtank",
				p -> new BacktankItem(AllArmorMaterials.COPPER, p, Create.asResource("copper_diving"),
					COPPER_BACKTANK_PLACEABLE))
			.model(AssetLookup.customGenericItemModel("_", "item"))
			.tag(AllItemTags.PRESSURIZED_AIR_SOURCES.tag)
			.tag(ItemTags.CHEST_ARMOR)
			.register(),

	NETHERITE_BACKTANK = REGISTRATE
		.item("netherite_backtank",
			p -> new BacktankItem.Layered(ArmorMaterials.NETHERITE, p, Create.asResource("netherite_diving"),
				NETHERITE_BACKTANK_PLACEABLE))
		.model(AssetLookup.customGenericItemModel("_", "item"))
		.properties(p -> p.fireResistant())
		.tag(AllItemTags.PRESSURIZED_AIR_SOURCES.tag)
		.tag(ItemTags.CHEST_ARMOR)
		.register();

	public static final ItemEntry<? extends DivingHelmetItem>
		COPPER_DIVING_HELMET = REGISTRATE
		.item("copper_diving_helmet",
			p -> new DivingHelmetItem(AllArmorMaterials.COPPER, p, Create.asResource("copper_diving")))
		.properties(p -> p.durability(Type.HELMET.getDurability(7)))
		.tag(ItemTags.HEAD_ARMOR)
		.register(),

	NETHERITE_DIVING_HELMET = REGISTRATE
		.item("netherite_diving_helmet",
			p -> new DivingHelmetItem(ArmorMaterials.NETHERITE, p, Create.asResource("netherite_diving")))
		.properties(p -> p.fireResistant().durability(Type.HELMET.getDurability(37)))
		.tag(ItemTags.HEAD_ARMOR)
		.register();

	public static final ItemEntry<? extends DivingBootsItem>
		COPPER_DIVING_BOOTS = REGISTRATE
		.item("copper_diving_boots",
			p -> new DivingBootsItem(AllArmorMaterials.COPPER, p, Create.asResource("copper_diving")))
		.properties(p -> p.durability(Type.BOOTS.getDurability(7)))
		.tag(ItemTags.FOOT_ARMOR)
		.register(),

	NETHERITE_DIVING_BOOTS = REGISTRATE
		.item("netherite_diving_boots",
			p -> new DivingBootsItem(ArmorMaterials.NETHERITE, p, Create.asResource("netherite_diving")))
		.properties(p -> p.fireResistant().durability(Type.BOOTS.getDurability(37)))
		.tag(ItemTags.FOOT_ARMOR)
		.register();

	public static final ItemEntry<? extends BaseArmorItem>

		CARDBOARD_HELMET = REGISTRATE.item("cardboard_helmet", p -> new CardboardArmorItem(ArmorItem.Type.HELMET, p))
		.properties(p -> p.durability(Type.HELMET.getDurability(4)))
		.tag(ItemTags.HEAD_ARMOR)
		.burnTime(1000)
		.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "item.create.cardboard_armor"))
		.model(TrimmableArmorModelGenerator::generate)
		.clientExtension(() -> () -> new CardboardArmorStealthOverlay())
		.register(),

	CARDBOARD_CHESTPLATE =
		REGISTRATE.item("cardboard_chestplate", p -> new CardboardArmorItem(ArmorItem.Type.CHESTPLATE, p))
			.properties(p -> p.durability(Type.CHESTPLATE.getDurability(4)))
			.tag(ItemTags.CHEST_ARMOR)
			.burnTime(1000)
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "item.create.cardboard_armor"))
			.model(TrimmableArmorModelGenerator::generate)
			.register(),

	CARDBOARD_LEGGINGS =
		REGISTRATE.item("cardboard_leggings", p -> new CardboardArmorItem(ArmorItem.Type.LEGGINGS, p))
			.properties(p -> p.durability(Type.LEGGINGS.getDurability(4)))
			.tag(ItemTags.LEG_ARMOR)
			.burnTime(1000)
			.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "item.create.cardboard_armor"))
			.model(TrimmableArmorModelGenerator::generate)
			.register(),

	CARDBOARD_BOOTS = REGISTRATE.item("cardboard_boots", p -> new CardboardArmorItem(ArmorItem.Type.BOOTS, p))
		.properties(p -> p.durability(Type.BOOTS.getDurability(4)))
		.tag(ItemTags.FOOT_ARMOR)
		.burnTime(1000)
		.onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "item.create.cardboard_armor"))
		.model(TrimmableArmorModelGenerator::generate)
		.register();

	public static final ItemEntry<SandPaperItem> SAND_PAPER = REGISTRATE.item("sand_paper", SandPaperItem::new)
		.tag(AllTags.AllItemTags.SANDPAPER.tag)
		.register();

	public static final ItemEntry<SandPaperItem> RED_SAND_PAPER = REGISTRATE.item("red_sand_paper", SandPaperItem::new)
		.tag(AllTags.AllItemTags.SANDPAPER.tag)
		.onRegister(s -> ItemDescription.referKey(s, SAND_PAPER))
		.register();

	public static final ItemEntry<WrenchItem> WRENCH = REGISTRATE.item("wrench", WrenchItem::new)
		.properties(p -> p.stacksTo(1))
		.model(AssetLookup.itemModelWithPartials())
		.tag(Items.TOOLS_WRENCH)
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

	public static final ItemEntry<LinkedControllerItem> LINKED_CONTROLLER =
		REGISTRATE.item("linked_controller", LinkedControllerItem::new)
			.properties(p -> p.stacksTo(1))
			.model(AssetLookup.itemModelWithPartials())
			.register();

	public static final ItemEntry<PotatoCannonItem> POTATO_CANNON =
		REGISTRATE.item("potato_cannon", PotatoCannonItem::new)
			.properties(p -> p.durability(100))
			.model(AssetLookup.itemModelWithPartials())
			.tag(Tags.Items.ENCHANTABLES, ItemTags.DURABILITY_ENCHANTABLE, ItemTags.BOW_ENCHANTABLE)
			.register();

	public static final ItemEntry<ExtendoGripItem> EXTENDO_GRIP = REGISTRATE.item("extendo_grip", ExtendoGripItem::new)
		.properties(p -> p.rarity(Rarity.UNCOMMON))
		.tag(ItemTags.DURABILITY_ENCHANTABLE)
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
		boolean rareCreated = false;
		boolean normalCreated = false;
		for (PackageStyle style : PackageStyles.STYLES) {
			ItemBuilder<PackageItem, CreateRegistrate> packageItem = BuilderTransformers.packageItem(style);

			if (rareCreated && style.rare() || normalCreated && !style.rare())
				packageItem.setData(ProviderType.LANG, NonNullBiConsumer.noop());

			rareCreated |= style.rare();
			normalCreated |= !style.rare();
			packageItem.register();
		}
	}

	public static final ItemEntry<ListFilterItem> FILTER = REGISTRATE.item("filter", FilterItem::regular)
		.lang("List Filter")
			.register();

	public static final ItemEntry<AttributeFilterItem> ATTRIBUTE_FILTER = REGISTRATE.item("attribute_filter", FilterItem::attribute)
			.register();

	public static final ItemEntry<PackageFilterItem> PACKAGE_FILTER = REGISTRATE.item("package_filter", FilterItem::address)
		.register();

	public static final ItemEntry<ScheduleItem> SCHEDULE = REGISTRATE.item("schedule", ScheduleItem::new)
		.lang("Train Schedule")
		.register();

	public static final ItemEntry<ShoppingListItem> SHOPPING_LIST =
		REGISTRATE.item("shopping_list", ShoppingListItem::new)
			.properties(p -> p.stacksTo(1))
			.register();

	// Schematics

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

	private static ItemEntry<TagDependentIngredientItem> compatCrushedOre(CommonMetal metal) {
		return REGISTRATE
			.item("crushed_raw_" + metal,
				props -> new TagDependentIngredientItem(props, metal.ores.items()))
			.tag(CRUSHED_RAW_MATERIALS.tag)
			.register();
	}

	// Load this class

	public static void register() {
	}

}
