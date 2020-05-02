package com.simibubi.create;

import static com.simibubi.create.modules.Sections.SCHEMATICS;

import com.simibubi.create.modules.schematics.block.CreativeCrateBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;
import com.tterrag.registrate.util.RegistryEntry;

import net.minecraft.block.Blocks;

public class AllBlocksNew {
	
	private static final CreateRegistrate REGISTRATE = Create.registrate();
	
	// Tools for strucuture movement and replication 
	static { REGISTRATE.startSection(SCHEMATICS); }
	
	public static final RegistryEntry<SchematicannonBlock> SCHEMATICANNON = REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.initialProperties(() -> Blocks.DISPENSER)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + "/base"))))
			.item()
				.model((ctx, prov) -> prov.blockItem(ctx.getEntry()::getBlock, "/item"))
				.build()
			.register();
	
	public static final RegistryEntry<CreativeCrateBlock> CREATIVE_CRATE = REGISTRATE.block("creative_crate", CreativeCrateBlock::new)
			.initialProperties(() -> Blocks.CHEST)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(ctx.getId())))
			.simpleItem()
			.register();
	
	public static final RegistryEntry<SchematicTableBlock> SCHEMATIC_TABLE = REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.initialProperties(() -> Blocks.LECTERN)
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models().getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();

	public static void register() {}
}
