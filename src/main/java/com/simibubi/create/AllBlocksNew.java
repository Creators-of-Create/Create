package com.simibubi.create;

import static com.simibubi.create.modules.Sections.SCHEMATICS;

import com.simibubi.create.foundation.utility.data.BlockStateGen;
import com.simibubi.create.modules.Sections;
import com.simibubi.create.modules.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.modules.contraptions.relays.elementary.CogwheelBlockItem;
import com.simibubi.create.modules.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.modules.contraptions.relays.encased.EncasedShaftBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;
import com.tterrag.registrate.util.RegistryEntry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class AllBlocksNew {
	
	private static final CreateRegistrate REGISTRATE = Create.registrate();
	
	static { REGISTRATE.startSection(SCHEMATICS); }
	
	public static final RegistryEntry<SchematicannonBlock> SCHEMATICANNON = REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.initialProperties(() -> Blocks.DISPENSER)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), BlockStateGen.partialBaseModel(ctx, prov)))
			.item()
				.model(BlockStateGen::customItemModel)
				.build()
			.register();

	public static final RegistryEntry<SchematicTableBlock> SCHEMATIC_TABLE = REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models().getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();
	
	static { REGISTRATE.startSection(Sections.KINETICS); }
	
	public static final RegistryEntry<ShaftBlock> SHAFT = REGISTRATE.block("shaft", ShaftBlock::new)
			.initialProperties(SharedProperties::kinetic)
			.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.standardModel(c, p)))
			.simpleItem()
			.register();
	
	public static final RegistryEntry<CogWheelBlock> COGWHEEL = REGISTRATE.block("cogwheel", CogWheelBlock::small)
			.initialProperties(SharedProperties::kinetic) 
			.properties(p -> p.sound(SoundType.WOOD))
			.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.standardModel(c, p)))
			.item(CogwheelBlockItem::new).build()
			.register();
	
	public static final RegistryEntry<CogWheelBlock> LARGE_COGWHEEL = REGISTRATE.block("large_cogwheel", CogWheelBlock::large)
			.initialProperties(SharedProperties::kinetic) 
			.properties(p -> p.sound(SoundType.WOOD))
			.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.standardModel(c, p)))
			.item(CogwheelBlockItem::new).build()
			.register();
	
	public static final RegistryEntry<EncasedShaftBlock> ENCASED_SHAFT = REGISTRATE.block("encased_shaft", EncasedShaftBlock::new)
			.initialProperties(SharedProperties::kinetic)
			.blockstate((c, p) -> BlockStateGen.axisKineticBlock(c, p, BlockStateGen.partialBaseModel(c, p)))
			.item()
				.model(BlockStateGen::customItemModel)
				.build()
			.register();
	
	public static void register() {}

	// Ideally these following three methods would be part of the RegistryEntry instances
	
	public static <T extends Block & IForgeRegistryEntry<Block>> boolean equals(RegistryEntry<T> entry, BlockState state) {
		return entry.map(state.getBlock()::equals).get();
	}
	
	public static ItemStack asStack(RegistryEntry<? extends Block> entry) {
		return new ItemStack(entry.get());
	}
	
	public static BlockState getDefault(RegistryEntry<? extends Block> entry) {
		return entry.get().getDefaultState();
	}
	
}
