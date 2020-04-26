package com.simibubi.create;

import com.simibubi.create.modules.schematics.block.CreativeCrateBlock;
import com.simibubi.create.modules.schematics.block.SchematicTableBlock;
import com.simibubi.create.modules.schematics.block.SchematicannonBlock;
import com.tterrag.registrate.util.RegistryEntry;

import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;

public class AllBlocksNew {
	
	private static final CreateRegistrate REGISTRATE = Create.registrate();
	
	static { REGISTRATE.setModule("SCHEMATICS"); }
	
	public static final RegistryEntry<SchematicannonBlock> SCHEMATICANNON = REGISTRATE.block("schematicannon", SchematicannonBlock::new)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getExistingFile(prov.modLoc("block/" + ctx.getName() + "/base"))))
			.item()
				.model((ctx, prov) -> prov.blockItem(ctx.getEntry()::getBlock, "/base"))
				.build()
			.register();
	
	public static final RegistryEntry<CreativeCrateBlock> CREATIVE_CRATE = REGISTRATE.block("creative_crate", CreativeCrateBlock::new)
			.blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), new UncheckedModelFile(ctx.getId())))
			.simpleItem()
			.register();
	
	public static final RegistryEntry<SchematicTableBlock> SCHEMATIC_TABLE = REGISTRATE.block("schematic_table", SchematicTableBlock::new)
			.blockstate((ctx, prov) -> prov.horizontalBlock(ctx.getEntry(), prov.models().getExistingFile(ctx.getId()), 0))
			.simpleItem()
			.register();

	public static void register() {}
}
