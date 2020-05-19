package com.simibubi.create;

import static com.simibubi.create.foundation.utility.data.BlockStateGen.oxidizedBlockstate;
import static com.simibubi.create.foundation.utility.data.ModelGen.oxidizedItemModel;
import static com.simibubi.create.modules.Sections.SCHEMATICS;

import com.simibubi.create.config.StressConfigDefaults;
import com.simibubi.create.foundation.registrate.CreateRegistrate;
import com.simibubi.create.foundation.utility.data.AssetLookup;
import com.simibubi.create.foundation.utility.data.BlockStateGen;
import com.simibubi.create.foundation.world.OxidizingBlock;
import com.simibubi.create.modules.Sections;
import com.simibubi.create.modules.contraptions.components.motor.MotorBlock;
import com.simibubi.create.modules.contraptions.components.motor.MotorGenerator;
import com.simibubi.create.modules.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.modules.contraptions.relays.belt.BeltGenerator;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.modules.contraptions.relays.encased.AdjustablePulleyBlock;
import com.simibubi.create.modules.contraptions.relays.encased.ClutchBlock;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedBeltBlock;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedBeltGenerator;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.modules.contraptions.relays.encased.GearshiftBlock;
import com.simibubi.create.modules.contraptions.relays.gearbox.GearboxBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.DataIngredient;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

public class AllBlocksNew {

	private static final CreateRegistrate REGISTRATE = Create.registrate()
		.itemGroup(() -> Create.baseCreativeTab);

	// Schematics

	static {
		REGISTRATE.startSection(SCHEMATICS);
	}

	public static final BlockEntry<SchematicannonBlock> SCHEMATICANNON =
		REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.initialProperties(() -> Blocks.DISPENSER)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
			.item()
			.model(AssetLookup::customItemModel)
			.build()
			.register();

	public static final BlockEntry<SchematicTableBlock> SCHEMATIC_TABLE =
		REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models()
				.getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();

	// Kinetics

	static {
		REGISTRATE.startSection(Sections.KINETICS);
	}

	public static final BlockEntry<ShaftBlock> SHAFT = REGISTRATE.block("shaft", ShaftBlock::new)
		.initialProperties(SharedProperties::kinetic)
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.simpleItem()
		.register();

	public static final BlockEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small)
		.initialProperties(SharedProperties::kinetic)
		.properties(p -> p.sound(SoundType.WOOD))
		.blockstate(BlockStateGen.axisBlockProvider(false))
		.item(CogwheelBlockItem::new)
		.build()
		.register();

	public static final BlockEntry<CogWheelBlock> LARGE_COGWHEEL =
		REGISTRATE.block("large_cogwheel", CogWheelBlock::large)
			.initialProperties(SharedProperties::kinetic)
			.properties(p -> p.sound(SoundType.WOOD))
			.blockstate(BlockStateGen.axisBlockProvider(false))
			.item(CogwheelBlockItem::new)
			.build()
			.register();

	public static final BlockEntry<EncasedShaftBlock> ENCASED_SHAFT =
		REGISTRATE.block("encased_shaft", EncasedShaftBlock::new)
			.initialProperties(SharedProperties::kinetic)
			.blockstate(BlockStateGen.axisBlockProvider(true))
			.item()
			.model(AssetLookup::customItemModel)
			.build()
			.register();

	public static final BlockEntry<GearboxBlock> GEARBOX = REGISTRATE.block("gearbox", GearboxBlock::new)
		.initialProperties(SharedProperties::kinetic)
		.blockstate(BlockStateGen.axisBlockProvider(true))
		.item()
		.model(AssetLookup::customItemModel)
		.build()
		.register();

	public static final BlockEntry<ClutchBlock> CLUTCH = REGISTRATE.block("clutch", ClutchBlock::new)
		.initialProperties(SharedProperties::kinetic)
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.model(AssetLookup::customItemModel)
		.build()
		.register();

	public static final BlockEntry<GearshiftBlock> GEARSHIFT = REGISTRATE.block("gearshift", GearshiftBlock::new)
		.initialProperties(SharedProperties::kinetic)
		.blockstate((c, p) -> BlockStateGen.axisBlock(c, p, AssetLookup.forPowered(c, p)))
		.item()
		.model(AssetLookup::customItemModel)
		.build()
		.register();

	public static final BlockEntry<EncasedBeltBlock> ENCASED_BELT =
		REGISTRATE.block("encased_belt", EncasedBeltBlock::new)
			.initialProperties(SharedProperties::kinetic)
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> p.models()
				.getExistingFile(p.modLoc("block/" + c.getName() + "/" + suffix))).generate(c, p))
			.item()
			.model(AssetLookup::customItemModel)
			.build()
			.register();

	public static final BlockEntry<AdjustablePulleyBlock> ADJUSTABLE_PULLEY =
		REGISTRATE.block("adjustable_pulley", AdjustablePulleyBlock::new)
			.initialProperties(SharedProperties::kinetic)
			.blockstate((c, p) -> new EncasedBeltGenerator((state, suffix) -> {
				String powered = state.get(AdjustablePulleyBlock.POWERED) ? "_powered" : "";
				return p.models()
					.withExistingParent(c.getName() + "_" + suffix + powered, p.modLoc("block/encased_belt/" + suffix))
					.texture("side", p.modLoc("block/" + c.getName() + powered));
			}).generate(c, p))
			.item()
			.model((c, p) -> p.withExistingParent(c.getName(), p.modLoc("block/encased_belt/item"))
				.texture("side", p.modLoc("block/" + c.getName())))
			.build()
			.register();

	public static final BlockEntry<BeltBlock> BELT = REGISTRATE.block("belt", BeltBlock::new)
		.initialProperties(SharedProperties.beltMaterial, MaterialColor.GRAY)
		.transform(StressConfigDefaults.setImpact(1.0))
		.blockstate(new BeltGenerator()::generate)
		.register();

	public static final BlockEntry<MotorBlock> CREATIVE_MOTOR = REGISTRATE.block("creative_motor", MotorBlock::new)
		.initialProperties(SharedProperties::kinetic)
		.blockstate(new MotorGenerator()::generate)
		.transform(StressConfigDefaults.setCapacity(16384.0))
		.item()
		.model(AssetLookup::customItemModel)
		.build()
		.register();

	// Materials

	static {
		REGISTRATE.startSection(Sections.MATERIALS);
	}

	public static final BlockEntry<OxidizingBlock> COPPER_ORE =
		REGISTRATE.block("copper_ore", p -> new OxidizingBlock(p, 1))
			.initialProperties(() -> Blocks.IRON_ORE)
			.transform(oxidizedBlockstate())
			.transform(tagBlockAndItem("ores/copper"))
			.transform(oxidizedItemModel())
			.register();

	public static final BlockEntry<Block> ZINC_ORE = REGISTRATE.block("zinc_ore", Block::new)
		.initialProperties(() -> Blocks.GOLD_BLOCK)
		.properties(p -> p.harvestLevel(2)
			.harvestTool(ToolType.PICKAXE))
		.transform(tagBlockAndItem("ores/zinc"))
		.build()
		.register();

	public static final BlockEntry<OxidizingBlock> COPPER_BLOCK =
		REGISTRATE.block("copper_block", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> Blocks.IRON_BLOCK)
			.transform(tagBlockAndItem("storage_blocks/copper"))
			.transform(oxidizedItemModel())
			.recipe((ctx, prov) -> prov.square(DataIngredient.tag(forgeItemTag("ingots/copper")), ctx, false))
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<OxidizingBlock> COPPER_SHINGLES =
		REGISTRATE.block("copper_shingles", p -> new OxidizingBlock(p, 1 / 32f))
			.initialProperties(() -> Blocks.IRON_BLOCK)
			.item()
			.transform(oxidizedItemModel())
			.recipe((ctx, prov) -> prov.square(DataIngredient.tag(forgeItemTag("plates/copper")), ctx, true))
			.transform(oxidizedBlockstate())
			.register();

	public static final BlockEntry<Block> ZINC_BLOCK = REGISTRATE.block("zinc_block", Block::new)
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.transform(tagBlockAndItem("storage_blocks/zinc"))
		.build()
		.recipe((ctx, prov) -> prov.square(DataIngredient.tag(forgeItemTag("ingots/zinc")), ctx, false))
		.register();

	public static final BlockEntry<Block> BRASS_BLOCK = REGISTRATE.block("brass_block", Block::new)
		.initialProperties(() -> Blocks.IRON_BLOCK)
		.transform(tagBlockAndItem("storage_blocks/brass"))
		.build()
		.recipe((ctx, prov) -> prov.square(DataIngredient.tag(forgeItemTag("ingots/brass")), ctx, false))
		.register();

	// Utility

	private static <T extends Block, P> NonNullFunction<BlockBuilder<T, P>, ItemBuilder<BlockItem, BlockBuilder<T, P>>> tagBlockAndItem(
		String tagName) {
		return b -> b.tag(forgeBlockTag(tagName))
			.item()
			.tag(forgeItemTag(tagName));
	}

	private static Tag<Block> forgeBlockTag(String name) {
		return forgeTag(BlockTags.getCollection(), name);
	}

	private static Tag<Item> forgeItemTag(String name) {
		return forgeTag(ItemTags.getCollection(), name);
	}

	private static <T> Tag<T> forgeTag(TagCollection<T> collection, String name) {
		return tag(collection, "forge", name);
	}

	private static <T> Tag<T> tag(TagCollection<T> collection, String domain, String name) {
		return collection.getOrCreate(new ResourceLocation(domain, name));
	}

	public static void register() {}

}
